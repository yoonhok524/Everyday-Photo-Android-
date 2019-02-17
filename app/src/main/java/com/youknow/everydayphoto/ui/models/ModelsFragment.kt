package com.youknow.everydayphoto.ui.models


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.impl.ModelRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.data.source.remote.RemoteModelDataSourceImpl
import kotlinx.android.synthetic.main.fragment_models.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton


private const val EDIT_MODEL = 0
private const val DELETE_MODEL = 1

class ModelsFragment : Fragment(), ModelsContract.View, AnkoLogger, ModelsAdapter.ModelClickListener {

    private val mPresenter: ModelsContract.Presenter by lazy {
        ModelsPresenter(
            this,
            ModelRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.modelDao(),
                RemoteModelDataSourceImpl(),
                GooglePhotoService.create()
            )
        )
    }

    private val mAdapter: ModelsAdapter by lazy {
        ModelsAdapter(context = context!!, modelCallback = this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_models, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvModels.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onStart() {
        super.onStart()
        mPresenter.getModels()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    override fun onModelsLoaded(models: List<Model>) {
        info("[EP] onModelsLoaded - size: ${models.size}")
        tvEmptyModels.visibility = if (models.isNullOrEmpty()) View.VISIBLE else View.GONE

        mAdapter.models.clear()
        mAdapter.models.addAll(models)
        mAdapter.notifyDataSetChanged()
    }

    override fun onModelClicked(model: Model) {
        findNavController().navigate(ModelsFragmentDirections.actionModelsFragmentToGalleryFragment(model))
    }

    override fun onClickCreateNewModel() {
        findNavController().navigate(R.id.action_modelsFragment_to_addModelFragment)
    }

    override fun onMoreClicked(model: Model) {
        val moreMenu = listOf(getString(R.string.edit), getString(R.string.delete))
        selector(getString(R.string.menu), moreMenu) { _, i ->
            when (i) {
                EDIT_MODEL -> findNavController().navigate(
                    ModelsFragmentDirections.actionModelsFragmentToEditModelFragment(
                        model
                    )
                )
                DELETE_MODEL -> showDeleteConfirmDialog(model)
            }
        }
    }

    override fun onModelDeleted(model: Model) {
        toast(getString(R.string.delete_model_success, model.name))
        val position = mAdapter.models.indexOfFirst { it?.name == model.name }
        mAdapter.models.removeAt(position)
        mAdapter.notifyItemRemoved(position)
    }

    private fun showDeleteConfirmDialog(model: Model) {
        alert(R.string.msg_delete_model_confirm, R.string.delete_model) {
            yesButton { mPresenter.deleteModel(model) }
            noButton {}
        }.show()
    }

}
