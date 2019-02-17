package com.youknow.everydayphoto.ui.models.edit

import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.ModelRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

class EditModelPresenter(
    private val view: EditModelContract.View,
    private val modelRepository: ModelRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : EditModelContract.Presenter, AnkoLogger {

    override fun updateModel(model: Model, name: String, desc: String) {
        if (name.isEmpty()) {
            view.onError(R.string.err_msg_name_empty)
            return
        }

        model.name = name
        model.desc = desc

        disposable.add(
            modelRepository.update(model)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ m ->
                    view.enableProgressDialog(false)
                    view.onModelUpdated(m)
                }, {
                    view.enableProgressDialog(false)
                    error("[EP] updateModel - failed: ${it.message}", it)
                })
        )
    }

}