package com.youknow.everydayphoto.ui.camera

import android.hardware.Camera
import com.youknow.everydayphoto.data.model.Model

interface CameraContract {
    interface View {
        fun moveToBack()
        fun onAuthError()
        fun showWaitDialog(enables: Boolean)
    }

    interface Presenter {
        fun unsubscribe()
        fun saveImage(model: Model, data: ByteArray, camera: Camera)
    }
}