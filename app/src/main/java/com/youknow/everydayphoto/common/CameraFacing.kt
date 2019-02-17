package com.youknow.everydayphoto.common

import com.youknow.everydayphoto.R

enum class CameraFacing(val value: Int, val resId: Int) {
    Back(0, R.string.back), Front(1, R.string.front);
}