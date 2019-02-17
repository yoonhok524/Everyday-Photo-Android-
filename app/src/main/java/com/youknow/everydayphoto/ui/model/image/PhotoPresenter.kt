package com.youknow.everydayphoto.ui.model.image

import com.youknow.everydayphoto.data.source.local.PhotoDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

class PhotoPresenter(
    private val view: PhotoContract.View,
    private val photoDao: PhotoDao,
    private val disposable: CompositeDisposable = CompositeDisposable()
): PhotoContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun getPhotos(albumId: String) {
        disposable.add(
            photoDao.getAllByModel(albumId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.onPhotosLoaded(it)
                }, {
                    // TODO: Error handle
                    error("[EP] getLast10Photos ($albumId) - failed: ${it.message}", it)
                })
        )
    }
}