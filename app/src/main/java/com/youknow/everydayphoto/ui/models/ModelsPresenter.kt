package com.youknow.everydayphoto.ui.models

import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.ModelRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class ModelsPresenter(
    private val view: ModelsContract.View,
    private val modelRepository: ModelRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : ModelsContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun getModels() {
        info("[EP] getModels")
        disposable.add(
            modelRepository.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.onModelsLoaded(it)
                }, {
                    error("[EP] getLast10Photos - failed: ${it.message}", it)
                })
        )
    }

    override fun deleteModel(model: Model) {
        info("[EP] deleteModel - $model")
        disposable.add(
            Observable.fromCallable { modelRepository.delete(model) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    info("[EP] deleteModel - done")
                    view.onModelDeleted(model)
                }, { e ->
                    error("[EP] deleteModel - failed: ${e.message}", e)
                })
        )
    }

}