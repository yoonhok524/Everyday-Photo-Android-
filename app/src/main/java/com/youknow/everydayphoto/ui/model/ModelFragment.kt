package com.youknow.everydayphoto.ui.model

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.youknow.everydayphoto.GlideApp
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.KEY_ALBUM_ID
import com.youknow.everydayphoto.common.KEY_MODEL_NAME
import com.youknow.everydayphoto.common.toDays
import com.youknow.everydayphoto.common.toYyyyMmm
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.data.repository.impl.PhotoRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import com.youknow.everydayphoto.services.MakeVideoService
import kotlinx.android.synthetic.main.fragment_model.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File


class ModelFragment : Fragment(), ModelContract.View, AnkoLogger,
    PhotosAdapter.PhotoClickListener, VideosAdapter.VideoClickListener {

    private val mPresenter: ModelContract.Presenter by lazy {
        ModelPresenter(
            this,
            context!!,
            PhotoRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.photoDao(),
                GooglePhotoService.create(),
                ExternalFileFactory()
            )
        )
    }

    private val mPhotoAdapter: PhotosAdapter by lazy {
        PhotosAdapter(context!!, modelName = args.model.name, glide = GlideApp.with(this), photoClickListener = this)
    }

    private val mVideoAdapter: VideosAdapter by lazy {
        VideosAdapter(context!!, videoClickListener = this)
    }

    private val args: ModelFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_model, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = args.model

        tvName.text = model.name
        tvDesc.text = model.desc
        tvCreatedAt.text = context?.getString(R.string.created_at_args, model.createdAt.toYyyyMmm())
        tvDays.text = model.createdAt.toDays()

        rvPhotos.apply {
            adapter = mPhotoAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        tvMorePhotos.setOnClickListener { moveToPhotosUi() }

        rvVideos.apply {
            adapter = mVideoAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        mPresenter.getNumberOfPhotos(model.albumId)
        mPresenter.getLast10Photos(model.albumId)
        mPresenter.getVideos(model)
    }

    override fun onNumberOfPhotoLoaded(numberOfPhotos: String) {
        tvPhotos.text = numberOfPhotos
    }

    override fun onPhotosLoaded(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            tvMorePhotos.visibility = View.VISIBLE
        }

        mPhotoAdapter.apply {
            this.photos.clear()
            this.photos.addAll(photos)
            notifyDataSetChanged()
        }
    }

    override fun onPhotoClicked(photo: Photo) {
        findNavController().navigate(ModelFragmentDirections.actionModelFragmentToImageFragment(args.model, photo))
    }

    override fun moveToCameraUi() {
        findNavController().navigate(
            ModelFragmentDirections.actionModelFragmentToCameraFragment(
                args.model
            )
        )
    }

    private fun moveToPhotosUi() {
        findNavController().navigate(ModelFragmentDirections.actionModelFragmentToPhotosFragment(args.model))
    }

    override fun makeNewVideo() {
        info("[EP] makeVideo - ${args.model.name}")
        val intent = Intent(context, MakeVideoService::class.java)
            .putExtra(KEY_ALBUM_ID, args.model.albumId)
            .putExtra(KEY_MODEL_NAME, args.model.name)

        if (Build.VERSION.SDK_INT >= 26) {
            context?.startForegroundService(intent)
        } else {
            context?.startService(intent)
        }
    }

    override fun onVideoClicked(video: File) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(video.path))
                .setDataAndType(Uri.parse(video.path), "video/mp4")
        )
    }

    override fun onVideosLoaded(videos: List<File>) {
        mVideoAdapter.apply {
            this.videos.clear()
            this.videos.addAll(videos)
            notifyDataSetChanged()
        }
    }
}
