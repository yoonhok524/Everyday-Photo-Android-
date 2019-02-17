package com.youknow.everydayphoto.ui.models

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youknow.everydayphoto.GlideApp
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.TYPE_HEADER
import com.youknow.everydayphoto.common.TYPE_ITEM
import com.youknow.everydayphoto.common.toDays
import com.youknow.everydayphoto.common.toYyyyMmm
import com.youknow.everydayphoto.data.model.Model
import kotlinx.android.synthetic.main.item_model.view.*


class ModelsAdapter(
    val models: MutableList<Model?> = mutableListOf(),
    private val context: Context,
    private val modelCallback: ModelClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ModelClickListener {
        fun onClickCreateNewModel()
        fun onModelClicked(model: Model)
        fun onMoreClicked(model: Model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_HEADER -> HeaderHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_model_header,
                parent,
                false
            )
        )
        else -> ModelHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_model, parent, false))
    }

    override fun getItemCount() = models.size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_HEADER
        else -> TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> bindHeader(holder as HeaderHolder)
            else -> bindModel(holder as ModelHolder, models[position - 1]!!)
        }
    }

    private fun bindHeader(holder: HeaderHolder) {
        holder.itemView.setOnClickListener {
            modelCallback.onClickCreateNewModel()
        }
    }

    private fun bindModel(holder: ModelHolder, model: Model) {
        holder.itemView.let { v ->
            if (model.profileUrl.isNotEmpty()) {
                GlideApp.with(context).load(model.profileUrl).into(v.ivProfile)
            }

            v.tvName.text = model.name
            v.tvCreatedAt.text = model.createdAt.toYyyyMmm()
            v.tvDays.text = context.getString(R.string.days_args, model.createdAt.toDays())
//            v.tvPhotos.text = context.getString(R.string.photos_args, model.numOfPhotos)

            v.ivMore.setOnClickListener { modelCallback.onMoreClicked(model) }
            v.setOnClickListener { modelCallback.onModelClicked(model) }
        }
    }

    class ModelHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}