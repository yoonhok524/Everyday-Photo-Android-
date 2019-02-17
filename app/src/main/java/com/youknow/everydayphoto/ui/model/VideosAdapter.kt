package com.youknow.everydayphoto.ui.model

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.TYPE_HEADER
import com.youknow.everydayphoto.common.TYPE_ITEM
import com.youknow.everydayphoto.factory.file.FileFactory
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import kotlinx.android.synthetic.main.item_video.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File


class VideosAdapter(
    val context: Context,
    val videos: MutableList<File> = mutableListOf(),
    private val fileFactory: FileFactory = ExternalFileFactory(),
    private val videoClickListener: VideoClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AnkoLogger {

    interface VideoClickListener {
        fun makeNewVideo()
        fun onVideoClicked(video: File)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_HEADER -> HeaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video_header,
                parent,
                false
            )
        )
        else -> VideoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = videos.size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> bindHeader(holder as HeaderHolder)
            else -> bindVideo(holder as VideoHolder, videos[position - 1])
        }
    }

    private fun bindHeader(holder: HeaderHolder) {
        holder.itemView.setOnClickListener {
            videoClickListener.makeNewVideo()
        }
    }

    private fun bindVideo(holder: VideoHolder, videoFile: File) {
        info("[EP] onBindViewHolder - ${videoFile.name}")

        holder.itemView.tvVideoFileName.text = videoFile.name
        holder.itemView.setOnClickListener {
            videoClickListener.onVideoClicked(videoFile)
        }
    }

    class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}