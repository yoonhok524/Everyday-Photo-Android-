package com.youknow.everydayphoto.ui.model

import android.content.Context
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.PhotoRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error


class ModelPresenter(
    private val view: ModelContract.View,
    private val context: Context,
    private val photoRepository: PhotoRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ModelContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun getNumberOfPhotos(albumId: String) {
        disposable.add(
            photoRepository.getNumberOfPhotos(albumId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.onNumberOfPhotoLoaded(it)
                }, {
                    error("[EP] getAlbum - failed: ${it.message}", it)
                })
        )
    }

    override fun getLast10Photos(albumId: String) {
        disposable.add(
            photoRepository.getPhotosByModelWithLimit(albumId, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it?.isNotEmpty()!!) {
                        view.onPhotosLoaded(it)
                    }
                }, {
                    error("[EP] getLast10Photos - failed: ${it.message}", it)
                })
        )
    }

    override fun getVideos(model: Model) {
        view.onVideosLoaded(photoRepository.getVideos(model))
    }

}