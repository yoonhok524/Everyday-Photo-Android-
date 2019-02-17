package com.youknow.everydayphoto.ui.splash


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.PERMISSIONS_REQUEST
import com.youknow.everydayphoto.ui.main.ExitListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.yesButton


class SplashFragment : Fragment(), SplashContract.View, AnkoLogger {

    private lateinit var mExitListener: ExitListener

    private val mPresenter: SplashContract.Presenter by lazy {
        SplashPresenter(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is ExitListener) {
            mExitListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasDevicePermissions()) {
            checkAuthentication()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        info("[EP] requestCode: $requestCode")
        if (requestCode != PERMISSIONS_REQUEST) {
            exit()
            return
        }

        if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
            alert(getString(R.string.quit_app_permission_deny)) {
                yesButton {
                    exit()
                }
            }.show()
        } else {
            checkAuthentication()
        }
    }

    private fun hasDevicePermissions(): Boolean {
        val requestPermissionList = mutableListOf<String>()
        generateRequestPermission(requestPermissionList, Manifest.permission.CAMERA)
        generateRequestPermission(requestPermissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return if (requestPermissionList.isNotEmpty()) {
            requestPermissions(requestPermissionList.toTypedArray(), PERMISSIONS_REQUEST)
            false
        } else {
            true
        }
    }

    private fun isSignIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.let {
            info("[EP] isSignIn - already signIn")
            it
        } != null
    }

    private fun generateRequestPermission(requestPermissionList: MutableList<String>, permission: String) {
        if (ContextCompat.checkSelfPermission(context!!, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionList.add(permission)
        }
    }

    private fun checkAuthentication() {
        if (isSignIn()) {
            mPresenter.initAccessToken(context!!)
        } else {
            moveToSignInUi()
        }
    }

    private fun exit() {
        mExitListener.exit()
    }

    override fun onTokenInitialized(token: String) {
        moveToModelsUi()
    }

    private fun moveToModelsUi() {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToModelsFragment())
    }

    private fun moveToSignInUi() {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())
    }

}
