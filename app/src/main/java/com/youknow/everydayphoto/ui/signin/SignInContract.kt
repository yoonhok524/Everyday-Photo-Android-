package com.youknow.everydayphoto.ui.signin

import android.content.Intent

interface SignInContract {
    interface View {
        fun finishWithSuccess()
        fun finishWithError()

    }

    interface Presenter {
        fun unsubscribe()
        fun handleSignInResult(data: Intent?)
    }
}