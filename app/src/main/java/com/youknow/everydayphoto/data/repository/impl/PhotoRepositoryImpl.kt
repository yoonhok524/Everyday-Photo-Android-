package com.youknow.everydayphoto.data.repository.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import com.youknow.everydayphoto.common.AuthHelper
import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import com.youknow.everydayphoto.common.toLongTime
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.data.repository.PhotoRepository
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.googlephoto.model.*
import com.youknow.everydayphoto.data.source.local.PhotoDao
import com.youknow.everydayphoto.factory.file.FileFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.*

class PhotoRepositoryImpl(
    private val photoDao: PhotoDao,
    private val googlePhotoService: GooglePhotoService,
    private val fileFactory: FileFactory
) : PhotoRepository, AnkoLogger {

    private val httpClient: OkHttpClient by lazy { OkHttpClient() }

    override fun savePhoto(model: Model, data: ByteArray, width: Int, height: Int): Completable {
        val photoFile = writeData(model, data, width, height, 1920f, 1080f)

        val buf = savePhotoIntoLocal(photoFile)
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), buf)

        info("[EP] savePhoto - token: ${AuthHelper.token!!}")
        return googlePhotoService.upload(AuthHelper.token!!, photoFile.name, requestBody)
            .map { it.string() }
            .doOnNext { info("[EP] savePhoto - upload to Google Photo done: $it") }
            .concatMap { uploadToken ->
                googlePhotoService.batchCreate(
                    AuthHelper.token!!,
                    BatchCreateRequest(
                        model.albumId,
                        listOf(NewMediaItem(simpleMediaItem = SimpleMediaItem(uploadToken)))
                    )
                ).map { resp -> resp.newMediaItemResults.first().mediaItem }
            }.concatMap { mediaItem ->
                googlePhotoService.getMediaItem(AuthHelper.token!!, mediaItem.id)
            }.concatMapCompletable {
                info("[EP] savePhoto - getMediaItem: $it")
                val photo = Photo(id = it.id, fileName = photoFile.name, albumId = model.albumId)

                Completable.fromCallable {
                    info("[EP] savePhoto - photo insert")
                    photoDao.insert(photo)
                }
            }
    }

    override fun getPhotos(albumId: String): Observable<List<Photo>> = photoDao.getAllByModel(albumId).toObservable()

    override fun getNumberOfPhotos(albumId: String): Observable<String> =
        googlePhotoService.getAlbum(AuthHelper.token!!, albumId)
            .map { it.mediaItemsCount ?: "0" }

    override fun getPhotosByModelWithLimit(albumId: String, limit: Int): Single<List<Photo>> {
        return photoDao.getAllByModelWithLimit(albumId, 10)
    }

    override fun syncPhotos(model: Model, nextPageToken: String?): Observable<String> {
        info("[EP] syncPhotos - start: ${model.name}")
        return searchMediaItems(model.albumId, nextPageToken)
            .flatMap {
                Observable.fromIterable(it.mediaItems)
            }
            .map {
                it to Photo(it.id, it.mediaMetadata.creationTime.toLongTime(), model.albumId, it.filename)
            }
            .flatMap { (mediaItem, photo) ->
                photoDao.insert(photo)
                info("[EP] syncPhotos - insertPhoto: ${photo.fileName}")
                downloadMediaItem(model.name, mediaItem)
            }
    }

    override fun makeVideo(albumId: String, modelName: String): Completable {
        return Completable.create { emitter ->
            getPhotos(albumId)
                .subscribeOn(Schedulers.io())
                .subscribe ({ photos ->
                    NIOUtils.writableFileChannel(fileFactory.getVideoFileName(modelName)).use { fileChannel ->
                            AndroidSequenceEncoder(fileChannel, Rational.R(1, 1)).let { encoder ->
                                info("[EP] makeVideo - encode start")
                                photos.reversed()
                                    .map {
                                        fileFactory.getFilePathName(modelName, it.fileName)
                                    }.filter {
                                        File(it).exists()
                                    }.map { path ->
                                        encoder.encodeImage(BitmapFactory.decodeFile(path))
                                    }

                                encoder.finish()
                            }
                        }

                    emitter.onComplete()
                }, {
                    emitter.onError(it)
                })
        }
    }

    override fun getVideos(model: Model): List<File> {
        return fileFactory.getVideoFiles(model)
    }

    private fun downloadMediaItem(modelName: String, mediaItem: MediaItem): Observable<String> {
        info("[EP] downloadMediaItem - $modelName, ${mediaItem.filename}")
        return Observable.fromCallable {
            val request = Request.Builder()
                .url(mediaItem.baseUrl)
                .build()

            info("[EP] downloadMediaItem - send request")
            val bytes = httpClient.newCall(request)
                .execute()
                .body()
                ?.bytes()

            info("[EP] downloadMediaItem - file write")
            fileFactory.createPhotoFile(modelName, mediaItem.filename).let { photoFile ->
                FileOutputStream(photoFile).use { fos ->
                    fos.write(bytes)
                    info("[EP] downloadMediaItem - done (${photoFile.path})")
                    mediaItem.filename
                }
            }
        }
    }

    private fun searchMediaItems(albumId: String, nextPageToken: String?): Observable<SearchMediaItemsResponse> =
        googlePhotoService.searchMediaItems(AuthHelper.token!!, SearchMediaItemsRequest(albumId, 1, nextPageToken))
            .filter { it.mediaItems != null }
            .subscribeOn(Schedulers.io())
            .concatMap {
                info("[EP] syncPhotos - nextPageToken: ${it.nextPageToken}")
                if (it.nextPageToken == null) {
                    Observable.just(it)
                } else {
                    Observable.just(it)
                        .concatWith(searchMediaItems(albumId, it.nextPageToken))
                }
            }

    private fun savePhotoIntoLocal(photoFile: File) = BufferedInputStream(FileInputStream(photoFile)).use { bis ->
        val buf = ByteArray(bis.available())
        while (bis.read(buf) !== -1);

        info("[EP] savePhotoIntoLocal - done: ${photoFile.name}")
        buf
    }

    private fun writeData(
        model: Model,
        data: ByteArray,
        width: Int,
        height: Int,
        baseWidth: Float,
        baseHeight: Float
    ): File {
        return fileFactory.createPhotoFile(model.name).let { photoFile ->
            FileOutputStream(photoFile).use { fos ->
                fos.write(convertData(baseWidth, baseHeight, width, height, model, data))
                info("[EP] saveImage - file write success: ${photoFile.path}")
                photoFile
            }
        }
    }

    private fun convertData(
        baseWidth: Float,
        baseHeight: Float,
        width: Int,
        height: Int,
        model: Model,
        data: ByteArray
    ): ByteArray {
        val bitmap = createBitmap(model, data, baseWidth, baseHeight, width, height)

        ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            return it.toByteArray()
        }
    }

    private fun createBitmap(
        model: Model,
        data: ByteArray,
        baseWidth: Float,
        baseHeight: Float,
        width: Int,
        height: Int
    ): Bitmap {
        val scaleWidth = baseWidth / width
        val scaleHeight = baseHeight / height

        info("[EP] $width, $height / $scaleWidth, $scaleHeight")

        val matrix = getImageMatrix(model, scaleWidth, scaleHeight)
        val bitmap = BitmapFactory.decodeByteArray(
            data,
            0,
            data.size,
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            })

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    private fun getImageMatrix(
        model: Model,
        scaleWidth: Float,
        scaleHeight: Float
    ): Matrix {
        return if (model.cameraFacing == CameraFacing.Back.value) {
            val orientation = when (model.orientation) {
                Orientation.Portrait.value -> 90F
                else -> 0F
            }
            Matrix().apply {
                postScale(scaleWidth, scaleHeight)
                postRotate(orientation)
            }
        } else {
            val matrixMirrorY = Matrix()
            matrixMirrorY.setValues(floatArrayOf(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f))

            Matrix().apply {
                postScale(scaleWidth, scaleHeight)
                postConcat(matrixMirrorY)
                preRotate(270F)
            }
        }
    }
}