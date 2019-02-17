package com.youknow.everydayphoto.ui.signin

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.youknow.everydayphoto.auth.SCOPE_PHOTO_AUTH
import com.youknow.everydayphoto.common.AuthHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import android.support.v4.content.ContextCompat.startForegroundService
import android.os.Build
import com.youknow.everydayphoto.common.KEY_UID
import com.youknow.everydayphoto.services.SyncService


class SignInPresenter(
    private val view: SignInContract.View,
    private val context: Context,
    private val disposable: CompositeDisposable = CompositeDisposable()
) : SignInContract.Presenter, AnkoLogger {

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun unsubscribe() {
        disposable.clear()
    }

    override fun handleSignInResult(data: Intent?) {
        val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            completedTask.getResult(ApiException::class.java)?.let {
                firebaseAuth(it)
            }
        } catch (e: ApiException) {
            error("[EP] handleSignInResult - failed: ${e.statusCode}", e)
            view.finishWithError()
        }
    }

    private fun firebaseAuth(googleAccount: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mFirebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    googleAccount.account?.let {
                        initToken(it)
                    }
                } else {
                    error(
                        "[EP] firebaseAuth / signInWithCredential - failed: ${task.exception?.message}",
                        task.exception
                    )
                    view.finishWithError()
                }
            }
    }

    private fun initToken(account: Account) {
        disposable.add(
            Observable.fromCallable {
                GoogleAuthUtil.getToken(context, account, "oauth2: $SCOPE_PHOTO_AUTH")
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    info("[EP] initToken - token: $it")
                    AuthHelper.token = it

                    syncData()
                }, {
                    error("[EP] initToken - failed: ${it.message}", it)
                    view.finishWithError()
                })
        )
    }

    private fun syncData() {
        val intent = Intent(context, SyncService::class.java).putExtra(KEY_UID, mFirebaseAuth.currentUser!!.uid)
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        view.finishWithSuccess()
    }
}