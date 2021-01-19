package com.hamedrahimvand.barcodex

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.graphics.toRect
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.custom.BarcodeBoundingBox
import com.hamedrahimvand.barcodex.custom.DarkFrame
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalyzer.Companion.DEFAULT_DETECTION_SPEED
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalyzerCallBack
import com.hamedrahimvand.barcodex.utils.CameraXHelper
import com.hamedrahimvand.barcodex.utils.toBoundingBox
import java.io.File
import kotlin.math.abs

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/16/20
 */
class BarcodeX @JvmOverloads constructor(
    context: Context, val attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var cameraXHelper: CameraXHelper
    private val analyzerCallBacks: MutableList<BarcodeXAnalyzerCallBack> = mutableListOf()
    private val barcodeMap = mutableMapOf<String, Int>()

    private var barcodeBoundingBox: BarcodeBoundingBox
    private var isScaled = false
    private var detectionSpeed = DEFAULT_DETECTION_SPEED
    private var darkFrame = DarkFrame(context)
    private var scale = 0f to 0f
    private var translation = 0f to 0f

    /**
     * Draw boundaries automatically, it'll draw all of detected barcode list items without particular conditions.
     */
    var autoDrawEnabled = true

    init {
        View.inflate(context, R.layout.barcodex, this)
        barcodeBoundingBox = findViewById(R.id.barcodeBoundingBox)
        getAttrs()
        addView(darkFrame)
        addView(scanFrame(context))
    }

    private fun scanFrame(context: Context) = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
            it.gravity = Gravity.CENTER
        }
        setImageResource(R.drawable.ic_scan_frame)
    }

    private fun getAttrs() {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BarcodeX)
        detectionSpeed =
            a.getInteger(R.styleable.BarcodeX_bx_detection_speed, DEFAULT_DETECTION_SPEED.toInt())
                .toLong()
        a.recycle()
    }

    fun setup(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        @FirebaseVisionBarcode.BarcodeFormat
        supportedFormats: IntArray? = null
    ) {
        cameraXHelper = CameraXHelper(
            previewView = findViewById(R.id.previewView),
            lifecycleOwner = lifecycleOwner,
            barcodeXAnalyzerCallback = analyzerCallBack,
            detectionSpeed = detectionSpeed,
            supportedFormats = supportedFormats
        )
        cameraXHelper.requestPermission(activity)
    }

    fun addAnalyzerCallBack(barcodeXAnalyzerCallBack: BarcodeXAnalyzerCallBack) {
        analyzerCallBacks.add(barcodeXAnalyzerCallBack)
    }

    fun removeAnalyzerCallBack(barcodeXAnalyzerCallBack: BarcodeXAnalyzerCallBack) {
        analyzerCallBacks.remove(barcodeXAnalyzerCallBack)
    }

    fun checkRequestPermissionResult(
        requestCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) = cameraXHelper.checkRequestPermissionResult(
        requestCode, doOnPermissionGranted, doOnPermissionNotGranted
    )

    fun checkActivityResult(
        requestCode: Int,
        resultCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) = cameraXHelper.checkActivityResult(
        requestCode,
        resultCode,
        doOnPermissionGranted,
        doOnPermissionNotGranted
    )


    private fun getBarcodeBoundingBoxState(
        type: Int,
        displayValue: String
    ): BarcodeBoundingBoxStates {
        return if (barcodeMap[displayValue] == type) {
            BarcodeBoundingBoxStates.DUPLICATE
        } else {
            barcodeMap[displayValue] = type
            BarcodeBoundingBoxStates.VALID
        }
    }

    private val analyzerCallBack = object : BarcodeXAnalyzerCallBack {
        override fun onNewFrame(w: Int, h: Int) {
            if (!isScaled) {
                isScaled = true
                val min: Int = w.coerceAtMost(h)
                val max: Int = w.coerceAtLeast(h)
                val localScale = (height.toFloat() / max).coerceAtLeast(width.toFloat() / min)
                if (height < max) {
                    barcodeBoundingBox.scaleY = 1f
                    barcodeBoundingBox.translationY = (-abs(height - max)).toFloat() / 2
                } else {
                    barcodeBoundingBox.scaleY = localScale
                    barcodeBoundingBox.translationY =
                        ((height - max).toFloat() / 2) * barcodeBoundingBox.scaleY
                }
                if (width < min) {
                    barcodeBoundingBox.scaleX = 1f
                    barcodeBoundingBox.translationX = (-abs(width - min)).toFloat() / 2
                } else {
                    barcodeBoundingBox.scaleX = localScale
                    barcodeBoundingBox.translationX =
                        ((width - min).toFloat() / 2) * barcodeBoundingBox.scaleX
                }
                //TODO refactor me
                scale = (barcodeBoundingBox.width.toFloat() / min) to barcodeBoundingBox.scaleY
//                translation = barcodeBoundingBox.translationX to barcodeBoundingBox.translationY
                translation = ((barcodeBoundingBox.width.toFloat() - min) / (4 * scale.first)).toFloat() to (abs(barcodeBoundingBox.height - max) / 4).toFloat()
//                translation = (abs(barcodeBoundingBox.width - max) / ((width.toFloat()/w) * (height.toFloat()/h) * 2 )) to (abs(barcodeBoundingBox.height - max) / 4).toFloat()
            }
        }

        override fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>) {
            Handler(context.mainLooper).post {
                val filteredList = qrCodes.filter {
                    if (it.boundingBox == null) {
                        false
                    } else {
                        //TODO refactor me
                        val scaledBound = Rect(it.boundingBox!!).apply {
//                            left = ((left*scale.first) + (640 - translation.first) / scale.first).toInt()
                            left = ((left * scale.first) + translation.first).toInt()
                            top = (top * scale.second).toInt()
//                            right = ((right*scale.first).toInt()  + (640 - translation.first) / scale.first).toInt()
                            right = ((right * scale.first) + translation.first).toInt()
                            bottom = (bottom * scale.second).toInt()
                        }
                        val rect = Rect(darkFrame.getCropRect().toRect())
                        rect.apply {
                            left = (width.toFloat() / 4).toInt()
                            right = ( (width.toFloat() * 4) / 5).toInt()
                        }
                        rect.contains(scaledBound)
                    }
                }
                if (autoDrawEnabled)
                    drawBoundaries(filteredList)
                analyzerCallBacks.forEach {
                    it.onQrCodesDetected(filteredList)
                }
            }
        }

        override fun onQrCodesFailed(exception: Exception) {
            analyzerCallBacks.forEach {
                it.onQrCodesFailed(exception)
            }
        }

    }

    fun drawBoundaries(barcodeList: List<FirebaseVisionBarcode>) {
        val barcodeBoundList = barcodeList.toBoundingBox() {
            getBarcodeBoundingBoxState(it.valueType, it.displayValue ?: "")
        }
        barcodeBoundingBox.drawBoundingBox(barcodeBoundList)
    }

    fun recalculate() {
        isScaled = false
    }

    fun takePhoto(
        file: File,
        doOnPhotoTaken: (ImageCapture.OutputFileResults) -> Unit,
        doOnError: (ImageCaptureException) -> Unit
    ) = cameraXHelper.takePhoto(file, doOnPhotoTaken, doOnError)

    fun torchOn(): Boolean {
        return cameraXHelper.torchOn()
    }

    fun torchOff(): Boolean {
        return cameraXHelper.torchOff()
    }

    fun toggleTorch(): Boolean? {
        return cameraXHelper.toggleTorch()
    }

    fun pauseDetection() {
        if (::cameraXHelper.isInitialized)
            cameraXHelper.pauseDetection()
    }

    fun resumeDetection() {
        if (::cameraXHelper.isInitialized)
            cameraXHelper.resumeDetection()
    }

    /**
     * Clear all drew bounds.
     */
    fun clearView() {

    }

    fun clearCash() {
        barcodeMap.clear()
    }
}

