package com.youknow.everydayphoto.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.youknow.everydayphoto.common.KEY_ALBUM_ID
import com.youknow.everydayphoto.common.KEY_MODEL_NAME
import com.youknow.everydayphoto.data.repository.PhotoRepository
import com.youknow.everydayphoto.data.repository.impl.PhotoRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.factory.file.FileFactory
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class MakeVideoService(
    private val disposable: CompositeDisposable = CompositeDisposable()
) : Service(), AnkoLogger {

    private val fileFactory: FileFactory by lazy { ExternalFileFactory() }

    private val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(
            AppDatabase.getAppDataBase(this)!!.photoDao(),
            GooglePhotoService.create(),
            fileFactory
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        info("[EP] onDestroy")
        disposable.clear()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val albumId = intent?.getStringExtra(KEY_ALBUM_ID)!!
        val modelName = intent.getStringExtra(KEY_MODEL_NAME)!!

        info("[EP] makeVideo - start, modelName: $modelName, albumId: $albumId")

        disposable.add(
            photoRepository.makeVideo(albumId, modelName)
                .subscribe({
                    info("[EP] maeVideo - success")
                    stopSelf()
                }, {
                    error("[EP] maeVideo - failed: ${it.message}", it)
                    stopSelf()
                })
        )

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}