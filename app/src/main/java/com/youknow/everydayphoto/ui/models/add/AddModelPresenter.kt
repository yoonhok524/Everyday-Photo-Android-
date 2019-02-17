package com.youknow.everydayphoto.ui.models.add

import android.database.sqlite.SQLiteConstraintException
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import com.youknow.everydayphoto.data.repository.ModelRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

class AddModelPresenter(
    private val view: AddModelContract.View,
    private val modelRepository: ModelRepository,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : AddModelContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun saveModel(name: String, orientation: Orientation, cameraFacing: CameraFacing) {
        if (name.isNullOrEmpty()) {
            view.onError(R.string.err_msg_name_empty)
            return
        }

        view.enableProgressDialog(true)

        disposable.add(
            modelRepository.createModel(name, orientation.value, cameraFacing.value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ model ->
                    view.enableProgressDialog(false)
                    view.onModelCreated(model)
                }, {
                    view.enableProgressDialog(false)
                    error("[EP] saveModel - failed: ${it.message}", it)
                    if (it is SQLiteConstraintException) {
                        view.onError(R.string.err_msg_name_already_exists)
                    }
                })
        )

    }

}