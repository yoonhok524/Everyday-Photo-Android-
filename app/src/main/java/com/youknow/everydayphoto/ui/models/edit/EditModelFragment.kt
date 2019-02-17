package com.youknow.everydayphoto.ui.models.edit


import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.impl.ModelRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.data.source.remote.RemoteModelDataSourceImpl
import kotlinx.android.synthetic.main.fragment_edit_model.*
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textResource


class EditModelFragment : Fragment(), EditModelContract.View {

    private val mPresenter: EditModelContract.Presenter by lazy {
        EditModelPresenter(
            this,
            ModelRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.modelDao(),
                RemoteModelDataSourceImpl(),
                GooglePhotoService.create()
            )
        )
    }

    private lateinit var mModel: Model

    private val args: EditModelFragmentArgs by navArgs()

    private val mProgressDialog: ProgressDialog by lazy {
        indeterminateProgressDialog(message = R.string.please_wait, title = R.string.updateing_model)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_model, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mModel = args.model

        etName.setText(mModel.name)
        etDesc.setText(mModel.desc)

        ivDone.setOnClickListener { updateModel() }
    }

    override fun onError(strResId: Int) {
        ivDone.isEnabled = true
        tvErrorMsg.textResource = strResId
        tvErrorMsg.visibility = View.VISIBLE
    }

    override fun enableProgressDialog(enables: Boolean) {
        if (enables) {
            mProgressDialog.show()
        } else {
            mProgressDialog.dismiss()
        }
    }

    override fun onModelUpdated(model: Model) {
        toast(getString(R.string.model_updated, model.name))
        findNavController().navigateUp()
    }

    private fun updateModel() {
        ivDone.isEnabled = false
        mPresenter.updateModel(mModel, etName.text.toString(), etDesc.text.toString())
    }
}
