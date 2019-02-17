package com.youknow.everydayphoto.ui.signin


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.auth.SCOPE_PHOTO_AUTH
import com.youknow.everydayphoto.common.REQUEST_CODE_GOOGLE_SIGN_IN
import com.youknow.everydayphoto.ui.main.ExitListener
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class SignInFragment : Fragment(), SignInContract.View, AnkoLogger {

    private lateinit var mExitListener: ExitListener

    private val mPresenter: SignInContract.Presenter by lazy {
        SignInPresenter(this, context!!)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is ExitListener) {
            mExitListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSignIn.setOnClickListener {
            startGoogleSignIn()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    private fun startGoogleSignIn() {
        info("[EP] start sign in!")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(SCOPE_PHOTO_AUTH))
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        info("[EP] requestCode: $requestCode, resultCode: $resultCode")
        if (resultCode == Activity.RESULT_CANCELED) {
            finishWithError()
            return
        }

        when (requestCode) {
            REQUEST_CODE_GOOGLE_SIGN_IN -> mPresenter.handleSignInResult(data)
        }
    }

    override fun finishWithSuccess() {
        findNavController().navigate(R.id.action_signInFragment_to_modelsFragment)
    }

    override fun finishWithError() {
        mExitListener.exit()
    }

}
