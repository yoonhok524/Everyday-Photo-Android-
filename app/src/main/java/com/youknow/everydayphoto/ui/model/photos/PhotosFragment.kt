package com.youknow.everydayphoto.ui.model.photos


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.youknow.everydayphoto.GlideApp
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.data.repository.impl.PhotoRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import com.youknow.everydayphoto.ui.model.PhotosAdapter
import kotlinx.android.synthetic.main.fragment_photos.*
import org.jetbrains.anko.AnkoLogger


class PhotosFragment : Fragment(), PhotosContract.View, AnkoLogger, PhotosAdapter.PhotoClickListener {

    private val mPresenter: PhotosContract.Presenter by lazy {
        PhotosPresenter(
            this, PhotoRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.photoDao(),
                GooglePhotoService.create(),
                ExternalFileFactory()
            )
        )
    }

    private val mAdapter: PhotosAdapter by lazy {
        PhotosAdapter(context!!, modelName = args.model.name, glide = GlideApp.with(this), photoClickListener = this)
    }

    private val args: PhotosFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPhotos.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        mPresenter.getPhotos(args.model.albumId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    override fun onPhotosLoaded(photoList: List<Photo>) {
        mAdapter.apply {
            photos.clear()
            photos.addAll(photoList)
            notifyDataSetChanged()
        }
    }

    override fun moveToCameraUi() {

    }

    override fun onPhotoClicked(photo: Photo) {
        findNavController().navigate(PhotosFragmentDirections.actionPhotosFragmentToImageFragment(args.model, photo))
    }

}
