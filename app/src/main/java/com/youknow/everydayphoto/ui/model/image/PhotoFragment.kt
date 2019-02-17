package com.youknow.everydayphoto.ui.model.image

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youknow.everydayphoto.GlideApp
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.KEY_MODEL_NAME
import com.youknow.everydayphoto.common.KEY_PHOTO
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.factory.file.FileFactory
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import kotlinx.android.synthetic.main.fragment_photo.*

class PhotoFragment : Fragment() {

    private val fileFactory: FileFactory by lazy { ExternalFileFactory() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val modelName = it.getString(KEY_MODEL_NAME)!!
            val photo = it.getParcelable<Photo>(KEY_PHOTO)!!

            val pathName = fileFactory.getFilePathName(modelName, photo.fileName)
            GlideApp.with(this).load(pathName).into(ivPhoto)
        }
    }
}