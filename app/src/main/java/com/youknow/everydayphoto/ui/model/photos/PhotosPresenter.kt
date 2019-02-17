package com.youknow.everydayphoto.ui.model.photos

import com.youknow.everydayphoto.data.repository.PhotoRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class PhotosPresenter(
    private val view: PhotosContract.View,
    private val photoRepository: PhotoRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : PhotosContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun getPhotos(albumId: String) {
        disposable.add(
            photoRepository.getPhotos(albumId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    info("[EP] getPhotos - albumId: $albumId, size: ${it.size}")
                    view.onPhotosLoaded(it)
                }, {
                    error("[EP] getPhotos - failed: ${it.message}", it)
                })
        )
    }

}