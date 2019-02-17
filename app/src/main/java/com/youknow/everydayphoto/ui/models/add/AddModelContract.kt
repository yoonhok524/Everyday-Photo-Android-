package com.youknow.everydayphoto.ui.models.add

import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import com.youknow.everydayphoto.data.model.Model

interface AddModelContract {
    interface View {
        fun onModelCreated(model: Model)
        fun onError(strResId: Int)
        fun enableProgressDialog(enables: Boolean)
    }

    interface Presenter {
        fun unsubscribe()
        fun saveModel(name: String, orientation: Orientation, cameraFacing: CameraFacing)

    }
}