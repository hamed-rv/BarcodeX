package com.hamedrahimvand.barcodex.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RestrictTo
import androidx.camera.core.*
import androidx.camera.extensions.BeautyPreviewExtender
import androidx.camera.extensions.BokehPreviewExtender
import androidx.camera.extensions.HdrPreviewExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import java.io.File
import java.util.concurrent.Executors


/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class CameraXHelper(
    val previewView: PreviewView,
    val lifecycleOwner: LifecycleOwner,
    val barcodeXAnalyzerCallback: BarcodeXAnalyzerCallBack,
    @Barcode.BarcodeFormat
    val supportedFormats: IntArray? = null
) {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var mIsCameraStarted = false
    private val applicationContext: Context = previewView.context.applicationContext
    private lateinit var cameraSelector: CameraSelector
    private lateinit var barcodeXAnalyzer: BarcodeXAnalyzer

    companion object {
        const val TAG = "CameraXExtension"
        const val REQUEST_CAMERA_PERMISSION = 10
        const val REQUEST_PERMISSION_SETTING = 11
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        val REQUIRED_PERMISSIONS = arrayOf(CAMERA_PERMISSION)
    }


    /**
     * Use this method inside Activity#onActivityResult()
     * if permission is granted, the camera will be started automatically
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun checkActivityResult(
        requestCode: Int, resultCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) {
        if (requestCode == REQUEST_PERMISSION_SETTING && resultCode == Activity.RESULT_OK) {
            if (allPermissionsGranted()) {
                previewView.post { startCamera() }
                doOnPermissionGranted()
            } else {
                doOnPermissionNotGranted()
            }
        }
    }

    /**
     * Use this method inside Activity#onRequestPermissionsResult()
     * if permission is granted, the camera will be started automatically
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun checkRequestPermissionResult(
        requestCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                previewView.post { startCamera() }
                doOnPermissionGranted()
            } else {
                doOnPermissionNotGranted()
            }
        }
    }

    /**
     * Check permission and request permission if needed.
     */
    private inline fun requestPermission(
        activity: Activity,
        crossinline doOnPermissionGranted: () -> Unit = {}
    ) {
        when {
            allPermissionsGranted() -> {
                previewView.post {
                    doOnPermissionGranted()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            ) -> {
                ActivityCompat.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CAMERA_PERMISSION
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CAMERA_PERMISSION
                )
            }
        }

    }

    /**
     * Check permission and request to get permission. it'll started the camera if permission is granted.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun requestPermission(activity: Activity) {
        requestPermission(activity) {
            startCamera()
        }
    }

    private fun openPermissionSetting(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", applicationContext.packageName, null)
        intent.data = uri
        activity.startActivityForResult(
            intent,
            REQUEST_PERMISSION_SETTING
        )
    }

    private fun startCamera() {
        if (mIsCameraStarted) return

        if (!allPermissionsGranted())
            throw SecurityException("Permission Denial, call CameraXHelper#requestPermission()")
        cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(applicationContext))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        cameraProvider?.unbindAll()

//         setup image capture
        imageCapture = setupImageCapture()


        cameraSelector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        // setup preview
        val preview = setupPreview()

        imageAnalysis = initialImageAnalysis()

        camera = cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        mIsCameraStarted = true

    }

    private fun setupPreview(): Preview {
        val previewBuilder = setupPreviewBuilder()

        previewView.afterMeasured {
            //Request focus on setup view
            requestFocus()
            previewView.setOnTouchListener { v, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        //Request focus on touch
                        requestFocus(event.x, event.y)
                        true
                    }
                    else -> false
                }
            }
        }

        val preview = previewBuilder.build()
        preview.setSurfaceProvider(
            previewView.surfaceProvider
        )
        return preview
    }

    private var lastFocusTime = 0L

    fun requestFocus(x: Float? = null, y: Float? = null) {
        if (System.currentTimeMillis() - lastFocusTime < 500) return
        lastFocusTime = System.currentTimeMillis()
        val width = if (x == null) 1f else previewView.width.toFloat()
        val height = if (y == null) 1f else previewView.height.toFloat()
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(width, height)
            .createPoint(x ?: .5f, y ?: .5f)
        try {
            camera?.cameraControl?.startFocusAndMetering(
                FocusMeteringAction.Builder(
                    autoFocusPoint,
                    FocusMeteringAction.FLAG_AF
                ).apply {
//                    setAutoCancelDuration(1, TimeUnit.SECONDS)
//                    disableAutoCancel()
                }.build()
            )
        } catch (e: CameraInfoUnavailableException) {
            Log.d("ERROR", "cannot access camera", e)
        }
    }

    private fun setupPreviewBuilder(): Preview.Builder {
        return Preview.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
        }
    }

    private fun setupImageCapture(): ImageCapture {
        return ImageCapture.Builder().apply {
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        }.build()
    }


    /**
     * Take photo and store it on file automatically
     * @param file uses to save photo
     */
    fun takePhoto(
        file: File,
        doOnPhotoTaken: (ImageCapture.OutputFileResults) -> Unit,
        doOnError: (ImageCaptureException) -> Unit
    ) {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
            .build()
        imageCapture?.takePicture(outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.v(TAG, "Photo capture succeeded: ${file.absolutePath}")
                    previewView.post {
                        doOnPhotoTaken(outputFileResults)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.v(TAG, "Photo capture failed : $exception")
                    previewView.post {
                        doOnError(exception)
                    }
                }
            })
    }

    var imageAnalysis: ImageAnalysis? = null

    private fun initialImageAnalysis(): ImageAnalysis? {

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        barcodeXAnalyzer = BarcodeXAnalyzer(
            barcodeXAnalyzerCallback
        ).also {
            if (supportedFormats != null)
                it.supportedFormats = supportedFormats
        }


        imageAnalysis?.setAnalyzer(executor, barcodeXAnalyzer)

        return imageAnalysis
    }

    fun stopCamera() {
        mIsCameraStarted = false
        cameraProvider?.unbindAll()
    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun checkExtensionAvailability(
        previewBuilder: Preview.Builder,
        cameraSelector: CameraSelector
    ) {
        val bokehPreviewExtender = BokehPreviewExtender.create(previewBuilder)
        val hdrPreviewExtender = HdrPreviewExtender.create(previewBuilder)
        val beautyPreviewExtender = BeautyPreviewExtender.create(previewBuilder)
        Log.d(
            TAG,
            """
                bokeh : ${bokehPreviewExtender.isExtensionAvailable(cameraSelector)}
                HDR: ${hdrPreviewExtender.isExtensionAvailable(cameraSelector)}
                Beauty: ${beautyPreviewExtender.isExtensionAvailable(cameraSelector)}
                """
        )
    }

    /**
     * @return True if device support Flash, otherwise it'll return False
     */
    fun torchOff(): Boolean {
        return if (hasFlash()) {
            enableTorch(false)
            true
        } else false
    }

    /**
     * @return True if device support Flash, otherwise it'll return False
     */
    fun torchOn(): Boolean {
        return if (hasFlash()) {
            enableTorch(true)
            true
        } else false
    }

    /**
     * @return True if torch is on, and off if torch is off. Also it return null if device doesn't support torch
     */
    fun toggleTorch(): Boolean? {
        if (!hasFlash()) return null
        return if (camera?.cameraInfo?.torchState?.value == TorchState.ON) {
            torchOff()
            false
        } else {
            torchOn()
            true
        }
    }

    private fun hasFlash() = camera?.cameraInfo?.hasFlashUnit() == true

    private fun enableTorch(torch: Boolean) {
        camera?.cameraControl?.enableTorch(torch)
    }

    fun pauseDetection() {
        if (::barcodeXAnalyzer.isInitialized)
            barcodeXAnalyzer.pauseDetection()
    }

    fun resumeDetection() {
        if (::barcodeXAnalyzer.isInitialized)
            barcodeXAnalyzer.resumeDetection()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun shutDown() {
        executor.shutdown()
    }
}

inline fun View.afterMeasured(crossinline block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}