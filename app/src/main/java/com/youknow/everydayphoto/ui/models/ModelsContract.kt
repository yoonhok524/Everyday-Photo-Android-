package com.youknow.everydayphoto.ui.models

import com.youknow.everydayphoto.data.model.Model

interface ModelsContract {
    interface View {
        fun onModelsLoaded(models: List<Model>)
        fun onModelDeleted(model: Model)

    }

    interface Presenter {
        fun unsubscribe()
        fun getModels()
        fun deleteModel(model: Model)
    }
}