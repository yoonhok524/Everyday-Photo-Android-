package com.youknow.everydayphoto.ui.model

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youknow.everydayphoto.GlideRequests
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.TYPE_HEADER
import com.youknow.everydayphoto.common.TYPE_ITEM
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.model.Photo
import com.youknow.everydayphoto.factory.file.FileFactory
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import kotlinx.android.synthetic.main.item_photo.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class PhotosAdapter(
    val context: Context,
    val photos: MutableList<Photo> = mutableListOf(),
    private val modelName: String,
    private val fileFactory: FileFactory = ExternalFileFactory(),
    private val glide: GlideRequests,
    private val photoClickListener: PhotoClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AnkoLogger {

    interface PhotoClickListener {
        fun moveToCameraUi()
        fun onPhotoClicked(photo: Photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_HEADER -> HeaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_photo_header,
                parent,
                false
            )
        )
        else -> PhotoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_photo,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = photos.size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> bindHeader(holder as HeaderHolder)
            else -> bindPhoto(holder as PhotoHolder, photos[position - 1])
        }
    }

    private fun bindHeader(holder: HeaderHolder) {
        holder.itemView.setOnClickListener {
            photoClickListener.moveToCameraUi()
        }
    }

    private fun bindPhoto(holder: PhotoHolder, photo: Photo) {
        info("[EP] onBindViewHolder - $photo")

        holder.itemView.ivPhoto.let {
            it.background = context.getDrawable(R.drawable.bg_gray_rounded_border) as GradientDrawable
            it.clipToOutline = true
            val pathName = fileFactory.getFilePathName(modelName, photo.fileName)
            glide.load(pathName).thumbnail(0.2f).centerCrop().into(it)
        }

        holder.itemView.setOnClickListener {
            photoClickListener.onPhotoClicked(photo)
        }
    }

    class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}