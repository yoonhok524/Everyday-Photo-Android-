package com.youknow.everydayphoto.ui.models.edit

import com.youknow.everydayphoto.data.model.Model

interface EditModelContract {
    interface View {
        fun onError(strResId: Int)
        fun enableProgressDialog(enables: Boolean)
        fun onModelUpdated(model: Model)

    }

    interface Presenter {
        fun updateModel(model: Model, name: String, desc: String)

    }
}