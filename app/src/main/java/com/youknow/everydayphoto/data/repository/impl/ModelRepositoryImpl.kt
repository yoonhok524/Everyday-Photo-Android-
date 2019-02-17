package com.youknow.everydayphoto.data.repository.impl

import com.youknow.everydayphoto.common.AuthHelper
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.ModelRepository
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.googlephoto.model.Album
import com.youknow.everydayphoto.data.source.googlephoto.model.AlbumRequest
import com.youknow.everydayphoto.data.source.local.ModelDao
import com.youknow.everydayphoto.data.source.remote.RemoteModelDataSourceImpl
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class ModelRepositoryImpl(
    private val modelDao: ModelDao,
    private val remoteModelDao: RemoteModelDataSourceImpl = RemoteModelDataSourceImpl(),
    private val googlePhotoService: GooglePhotoService
) : ModelRepository, AnkoLogger {

    override fun save(model: Model): Observable<Model> {
        info("[EP] save - $model")
        return Observable.fromCallable {
            modelDao.insert(model)
            remoteModelDao.save(model)
            model
        }

    }

    override fun update(model: Model): Observable<Model> {
        return Observable.fromCallable {
            modelDao.update(model)
            remoteModelDao.save(model)
            model
        }
    }

    override fun getAll(): Single<List<Model>> {
        return modelDao.getAll()
    }

    override fun delete(vararg models: Model) {
        modelDao.delete(*models)
        remoteModelDao.delete(*models)
    }

    override fun createModel(name: String, orientation: Int, cameraFacing: Int): Observable<Model> {
        return googlePhotoService.createAlbums(AuthHelper.token!!, AlbumRequest(Album(title = name)))
            .concatMap { album ->
                val model = Model(
                    albumId = album.id!!,
                    name = name,
                    orientation = orientation,
                    cameraFacing = cameraFacing
                )
                save(model)
            }

    }

    override fun syncModels(): Observable<Model> {
        info("[EP] syncModels - start")
        return remoteModelDao.getModels()
            .concatMap {
                Observable.fromCallable {
                    info("[EP] syncModels - db insert: ${it.size}")
                    modelDao.insert(*it.toTypedArray())
                }.subscribeOn(Schedulers.io())
                    .subscribe()

                Observable.fromIterable(it)
            }
    }

}