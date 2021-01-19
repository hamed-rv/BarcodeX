package com.hamedrahimvand.barcodex.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.camera.core.*
import androidx.camera.extensions.BeautyPreviewExtender
import androidx.camera.extensions.BokehPreviewExtender
import androidx.camera.extensions.HdrPreviewExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
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
    val detectionSpeed: Long,
    @FirebaseVisionBarcode.BarcodeFormat
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
        cameraProvider = cameraProviderFuture.get()
        // setup image capture
        imageCapture = setupImageCapture()


        cameraSelector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        // setup preview
        val preview = setupPreview(cameraSelector)


        val imageAnalysis = getImageAnalysis()

        camera = cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        mIsCameraStarted = true

    }

    private fun setupPreview(cameraSelector: CameraSelector): Preview {
        val previewBuilder = setupPreviewBuilder()

        setBokehEffect(previewBuilder, cameraSelector)

        val preview = previewBuilder.build()
        preview.setSurfaceProvider(
            previewView.surfaceProvider
        )
        return preview
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

    private fun setBokehEffect(
        previewBuilder: Preview.Builder,
        cameraSelector: CameraSelector
    ) {
        val bokehPreviewExtender = BokehPreviewExtender.create(previewBuilder)
        if (bokehPreviewExtender.isExtensionAvailable(cameraSelector)) {
            bokehPreviewExtender.enableExtension(cameraSelector)
        }
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

    private fun getImageAnalysis(): ImageAnalysis {

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        barcodeXAnalyzer = BarcodeXAnalyzer(
            barcodeXAnalyzerCallback
        ).also {
            if (supportedFormats != null)
                it.supportedFormats = supportedFormats
        }

        barcodeXAnalyzer.detectionSpeed = detectionSpeed

        imageAnalysis.setAnalyzer(executor, barcodeXAnalyzer)

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
}