package com.youknow.everydayphoto.ui.model.image

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.KEY_MODEL_NAME
import com.youknow.everydayphoto.common.KEY_PHOTO
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.data.source.local.AppDatabase
import kotlinx.android.synthetic.main.fragment_image.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class ImageFragment : Fragment(), PhotoContract.View, AnkoLogger {

    private val mPresenter: PhotoContract.Presenter by lazy {
        PhotoPresenter(
            this,
            AppDatabase.getAppDataBase(context!!)!!.photoDao()
        )
    }

    private val args: ImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter.getPhotos(args.model.albumId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    override fun onPhotosLoaded(photos: List<Photo>) {
        info("[EP] onPhotosLoaded - photos.size: ${photos.size}")
        pagerPhotos.adapter = ScreenSlidePagerAdapter(activity?.supportFragmentManager!!, photos)
        pagerPhotos.currentItem = photos.indexOf(args.photo)
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager, val photos: List<Photo>) :
        FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = photos.size

        override fun getItem(position: Int): Fragment {
            return PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_MODEL_NAME, args.model.name)
                    putParcelable(KEY_PHOTO, photos[position])
                }
            }
        }
    }
}
