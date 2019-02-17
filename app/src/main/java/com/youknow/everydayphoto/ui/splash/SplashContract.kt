package com.youknow.everydayphoto.ui.splash

import android.content.Context
import com.youknow.everydayphoto.data.model.Model

interface SplashContract {
    interface View {
        fun onTokenInitialized(token: String)

    }

    interface Presenter {
        fun unsubscribe()
        fun initAccessToken(context: Context)

    }
}