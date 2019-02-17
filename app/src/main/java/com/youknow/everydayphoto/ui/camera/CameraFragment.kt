package com.youknow.everydayphoto.ui.camera


import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.youknow.everydayphoto.R
import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.repository.impl.PhotoRepositoryImpl
import com.youknow.everydayphoto.data.source.googlephoto.GooglePhotoService
import com.youknow.everydayphoto.data.source.local.AppDatabase
import com.youknow.everydayphoto.factory.file.external.ExternalFileFactory
import kotlinx.android.synthetic.main.fragment_camera.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.indeterminateProgressDialog


class CameraFragment : Fragment(), CameraContract.View, SurfaceHolder.Callback, AnkoLogger {

    private val mPresenter: CameraContract.Presenter by lazy {
        CameraPresenter(
            this,
            PhotoRepositoryImpl(
                AppDatabase.getAppDataBase(context!!)!!.photoDao(),
                GooglePhotoService.create(),
                ExternalFileFactory()
            )
        )
    }

    private val mProgressDialog: ProgressDialog by lazy {
        indeterminateProgressDialog(message = R.string.please_wait, title = R.string.save_and_upload)
    }

    private val args: CameraFragmentArgs by navArgs()

    private var mCamera: Camera? = null
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mModel: Model
    private var mCameraFacing: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        info("[EP] onCreateView")
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mModel = args.model
        mCameraFacing = mModel.cameraFacing

        initOrientation()
        initClickListener()
    }


    override fun onStart() {
        super.onStart()
        initCameraAndPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.unsubscribe()
    }

    override fun moveToBack() {
        findNavController().popBackStack()
    }

    override fun onAuthError() {
        // TODO: refresh token
    }

    private fun initOrientation() {
        info("[EP] initOrientation")
        activity?.requestedOrientation = when (mModel.orientation) {
            Orientation.Portrait.value -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun initCameraAndPreview() {
        info("[EP] initCameraAndPreview")
        checkCameraHardware()

        surfaceView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y

                val touchRect = Rect(
                    (x - 50).toInt(),
                    (y - 50).toInt(),
                    (x + 50).toInt(),
                    (y + 50).toInt()
                )

                val targetFocusRect = Rect(
                    touchRect.left * 2000 / surfaceView.width - 1000,
                    touchRect.top * 2000 / surfaceView.height - 1000,
                    touchRect.right * 2000 / surfaceView.width - 1000,
                    touchRect.bottom * 2000 / surfaceView.height - 1000
                )

                doTouchFocus(targetFocusRect)
            }

            false
        }

        mSurfaceHolder = surfaceView.holder.apply {
            addCallback(this@CameraFragment)
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        if (mCamera == null) {
            mCamera = getCamera(mModel.cameraFacing)
        }
    }

    private fun doTouchFocus(targetFocusRect: Rect) {
        val focusList = listOf(Camera.Area(targetFocusRect, 1000))

        mCamera?.let {
            it.parameters.focusAreas = focusList
            it.parameters.meteringAreas = focusList
            it.autoFocus { b, camera ->
                if (b) {
                    camera.cancelAutoFocus()
                }
            }
        }
    }

    private fun initClickListener() {
        chkBoxGuideLine.setOnClickListener { switchGuideLine() }
        btnCameraFacing.setOnClickListener { switchCameraFacing() }

        btnCapture.setOnClickListener { takePicture() }
        btnModels.setOnClickListener {
            findNavController().navigate(R.id.action_cameraFragment_to_modelsFragment)
        }
    }

    private fun takePicture() {
        btnCapture.isEnabled = false
        mCamera?.takePicture(null, null) { data, camera ->
            mPresenter.saveImage(mModel, data, camera)
            camera.stopPreview()
        }
    }

    private fun switchGuideLine() {
        grpGuideLine.visibility = when (chkBoxGuideLine.isChecked) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun switchCameraFacing() {
        mCamera?.let { c ->
            c.stopPreview()
            c.release()

            mCameraFacing = when (mCameraFacing) {
                CameraFacing.Back.value -> CameraFacing.Front.value
                else -> CameraFacing.Back.value
            }

            mCamera = getCamera(mCameraFacing)
            mCamera?.setPreviewDisplay(mSurfaceHolder)
            mCamera?.startPreview()
        }
    }

    private fun checkCameraHardware() {
        if (!activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA)!!) {
            // TODO: Exit
//            finish()
        }
    }

    private fun getCamera(cameraFacing: Int): Camera? = try {
        when (cameraFacing) {
            CameraFacing.Back.value -> Camera.CameraInfo.CAMERA_FACING_BACK
            else -> Camera.CameraInfo.CAMERA_FACING_FRONT
        }.let {
            Camera.open(it).apply {
                if (mModel.orientation == Orientation.Portrait.value) {
                    setDisplayOrientation(90)
                }

                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }
        }
    } catch (e: Exception) {
        error("[EP] create getCamera - failed: ${e.message}", e)
        null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        info("[EP] surfaceCreated")
        mCamera?.let {
            it.setPreviewDisplay(holder)
            it.startPreview()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, w: Int, h: Int) {
        info("[EP] surfaceChanged - $format ($w, $h)")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        info("[EP] surfaceDestroyed")
        mCamera?.let {
            it.stopPreview()
            it.release()
            mCamera = null
        }
    }

    override fun showWaitDialog(enables: Boolean) {
        if (enables) {
            mProgressDialog.show()
        } else {
            mProgressDialog.dismiss()
        }
    }
}
