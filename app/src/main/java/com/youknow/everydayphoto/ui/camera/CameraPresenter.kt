package com.youknow.everydayphoto.ui.camera

import android.hardware.Camera
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.PhotoRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import retrofit2.HttpException

class CameraPresenter(
    private val view: CameraContract.View,
    private val photoRepository: PhotoRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : CameraContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun saveImage(model: Model, data: ByteArray, camera: Camera) {
        view.showWaitDialog(true)
        disposable.add(
            photoRepository.savePhoto(model, data, camera.parameters.pictureSize.width, camera.parameters.pictureSize.height)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    info("[EP] saveImage - db photo insert success")
                    view.showWaitDialog(false)
                    view.moveToBack()
                }, { e ->
                    error("[EP] saveImage - db photo insert failed: ${e.message}", e)
                    view.showWaitDialog(false)
                    if (e is HttpException && e.code() == 401) {
                        view.onAuthError()
                    }
                })
        )
    }

}