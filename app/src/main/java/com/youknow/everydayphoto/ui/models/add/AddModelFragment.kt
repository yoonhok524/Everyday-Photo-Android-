package com.youknow.everydayphoto.ui.models.add


import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.impl.ModelRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.data.source.remote.RemoteModelDataSourceImpl
import kotlinx.android.synthetic.main.fragment_add_model.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textResource


class AddModelFragment : Fragment(), AddModelContract.View, AnkoLogger {

    private val mPresenter: AddModelContract.Presenter by lazy {
        AddModelPresenter(
            this,
            ModelRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.modelDao(),
                RemoteModelDataSourceImpl(),
                GooglePhotoService.create()
            )
        )
    }

    private val mOrientationList = listOf(Orientation.Landscape, Orientation.Portrait)

    private val mCameraFacingList = listOf(CameraFacing.Back, CameraFacing.Front)

    private val mProgressDialog: ProgressDialog by lazy {
        indeterminateProgressDialog(message = R.string.please_wait, title = R.string.creating_model)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_model, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spnOrientation.adapter = ArrayAdapter(
            context!!,
            R.layout.support_simple_spinner_dropdown_item,
            mOrientationList.map { getString(it.resId) }.toList()
        )
        spnCameraFacing.adapter = ArrayAdapter(
            context!!,
            R.layout.support_simple_spinner_dropdown_item,
            mCameraFacingList.map { getString(it.resId) }.toList()
        )

        ivClose.setOnClickListener {
            findNavController().navigateUp()
        }

        ivDone.setOnClickListener {
            ivDone.isEnabled = false
            mPresenter.saveModel(
                etName.text.toString(),
                mOrientationList[spnOrientation.selectedItemPosition],
                mCameraFacingList[spnCameraFacing.selectedItemPosition]
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    override fun onModelCreated(model: Model) {
        toast(getString(R.string.model_created, model.name))
        findNavController().navigateUp()
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
}
