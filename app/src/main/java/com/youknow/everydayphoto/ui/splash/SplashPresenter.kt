package com.youknow.everydayphoto.ui.splash

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.youknow.everydayphoto.auth.SCOPE_PHOTO_AUTH
import com.youknow.everydayphoto.common.AuthHelper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info


class SplashPresenter(
    private val view: SplashContract.View,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : SplashContract.Presenter, AnkoLogger {

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun initAccessToken(context: Context) {
        disposable.add(
            Observable.fromCallable {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                info("[EP] initAccessToken - ${account?.email}")
                if (account == null || account.account == null) {
                    error("[EP] account is null - auth is required")
                }
                GoogleAuthUtil.getToken(context, account!!.account, "oauth2: $SCOPE_PHOTO_AUTH")
            }.subscribeOn(Schedulers.io())
                .subscribe({
                    info("[EP] initAccessToken - token: $it")
                    AuthHelper.token = it
                    view.onTokenInitialized(it)
                }, {
                    error("[EP] initAccessToken - failed: ${it.message}", it)
                })
        )
    }

}