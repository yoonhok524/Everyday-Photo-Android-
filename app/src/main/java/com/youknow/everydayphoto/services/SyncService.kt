package com.youknow.everydayphoto.services

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.youknow.everydayphoto.common.KEY_UID
import com.youknow.everydayphoto.data.repository.ModelRepository
import com.youknow.everydayphoto.data.repository.PhotoRepository
import com.youknow.everydayphoto.data.repository.impl.ModelRepositoryImpl
import com.youknow.everydayphoto.data.repository.impl.PhotoRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.data.source.remote.RemoteModelDataSourceImpl
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import com.youknow.everydayphoto.ui.main.MainActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info


class SyncService(
    private val disposable: CompositeDisposable = CompositeDisposable()
) : Service(), AnkoLogger {

    private val modelRepository: ModelRepository by lazy {
        ModelRepositoryImpl(
            AppDatabase.getAppDataBase(this)!!.modelDao(),
            RemoteModelDataSourceImpl(),
            GooglePhotoService.create()
        )
    }

    private val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(
            AppDatabase.getAppDataBase(this)!!.photoDao(),
            GooglePhotoService.create(),
            ExternalFileFactory()
        )
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    // TODO Update
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            val channelId = "noti_channel"
            val channelName = "Everyday Photo Synchronizing..."
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(this, channelId)
        } else {
            NotificationCompat.Builder(this)
        }

        builder.setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle("Everyday Photo - Synchronizing")
            .setContentText("Wait please...")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        startForeground(1, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        info("[EP] onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uid = intent?.getStringExtra(KEY_UID)!!
        info("[EP] onStartCommand - uid: $uid")

        disposable.add(
            modelRepository.syncModels()
                .concatMap { model ->
                    // TODO: Model이 없는 경우?
                    // TODO: Model이 있는 경우 sync start를 알려주는 Toast
                    photoRepository.syncPhotos(model)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    info("[EP] onStartCommand - sync done: $it")
                    // TODO: Update progress
                }, {
                    error("[EP] onStartCommand - failed: ${it.message}", it)
                }, {
                    info("[EP] onStartCommand - sync completed")
                    stopForeground(true)
                    stopSelf()
                })
        )

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}