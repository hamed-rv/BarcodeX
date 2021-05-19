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
import com.google.mlkit.vision.barcode.Barcode
import com.hamedrahimvand.barcodex.custom.BarcodeBoundingBox
import com.hamedrahimvand.barcodex.custom.DarkFrame
import com.hamedrahimvand.barcodex.model.BarcodeX
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
    context: Context, private val attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var cameraXHelper: CameraXHelper
    private val analyzerCallBacks: MutableList<BarcodeXAnalyzerCallBack> = mutableListOf()
    private val barcodeMap = mutableMapOf<String, Int>()

    private var barcodeBoundingBox: BarcodeBoundingBox
    private var isScaled = false
    private var darkFrame = DarkFrame(context)
    private var scale = 0f to 0f

    private val qrList = hashMapOf<String, Int>()// arrayListOf<Pair<Barcode,Int>>()

    /**
     * Draw boundaries automatically, it'll draw all of detected barcode list items without particular conditions.
     */
    var autoDrawEnabled = true
    private var cropCenterScan = true
    private var autoDarkFrame = true

    companion object {
    const val THRESHOLD_DEFAULT = 0 //scan iteration to ensure the verified scan
    private var defaultLeft = 100
    private var defaultRight = 120

    }

    var threshold = THRESHOLD_DEFAULT

    init {
        View.inflate(context, R.layout.barcodex, this)
        getAttrs()
        barcodeBoundingBox = findViewById(R.id.barcodeBoundingBox)
        if (autoDarkFrame) {
            addView(darkFrame)
            addView(scanFrame(context))
        }
    }

    private fun getAttrs() {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BarcodeX)
        autoDarkFrame =
            a.getBoolean(R.styleable.BarcodeX_bx_show_dark_frame, true)
        cropCenterScan =
            a.getBoolean(R.styleable.BarcodeX_bx_crop_center, true)
        a.recycle()
    }

    private fun scanFrame(context: Context) = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
            it.gravity = Gravity.CENTER
        }
        setImageResource(R.drawable.ic_scan_frame)
    }


    fun setup(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        @Barcode.BarcodeFormat
        supportedFormats: IntArray? = null
    ) {
        cameraXHelper = CameraXHelper(
            previewView = findViewById(R.id.previewView),
            lifecycleOwner = lifecycleOwner,
            barcodeXAnalyzerCallback = analyzerCallBack,
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
                scale = (height.toFloat() / max).coerceAtLeast(width.toFloat() / min) to
                        (height.toFloat() / max).coerceAtMost(width.toFloat() / min)
            }
        }

        override fun onQrCodesDetected(qrCodes: List<BarcodeX>) {
            Handler(context.mainLooper).post {
                val filteredList = qrCodes.filter {
                    if (cropCenterScan) {
                        if (it.boundingBox == null) {
                            false
                        } else {
                            val scaledBound = Rect(it.boundingBox!!).apply {
                                left = (left * scale.first).toInt() //+ defaultLeft
                                top = (top * scale.second).toInt() + defaultRight
                                right = (right * scale.first).toInt()
                                bottom = (bottom * scale.second).toInt()
                            }
                            darkFrame.getCropRect().toRect().contains(scaledBound)
                        }
                    } else {
                        true
                    }
                }.map { barcode ->
                    BarcodeX(
                        barcode.displayValue,
                        barcode.boundingBox,
                        barcode.format,
                        barcode.rawValue,
                        barcode.valueType
                    )
                }
                if (autoDrawEnabled)
                    drawBoundaries(filteredList)
                analyzerCallBacks.forEach { callback ->

                    val resultFilterList = mutableListOf<BarcodeX>()

                    filteredList.forEach { newBarcode ->
                        val displayValue = newBarcode.displayValue
                        val index = qrList.containsKey(displayValue)
                        if (index) {
                            if (qrList[displayValue]!! >= threshold) {
                                resultFilterList.add(newBarcode)
                            } else {
                                qrList[displayValue] = (qrList[displayValue] ?: 0) + 1
                            }
                        } else {
                            displayValue.let { it1 -> qrList.put(it1, 1) }
                        }
                    }
                    callback.onQrCodesDetected(resultFilterList)
                    if(filteredList.isNotEmpty()){
                        //Request focus on new barcode detected
                        cameraXHelper.requestFocus()
                    }
                }
            }
        }

        override fun onQrCodesFailed(exception: Exception) {
            analyzerCallBacks.forEach {
                it.onQrCodesFailed(exception)
            }
        }

    }

    fun clearBoundaries(){
        barcodeBoundingBox.drawBoundingBox(listOf())
    }

    fun drawBoundaries(
        barcodeList: List<BarcodeX>,
    ) {
        val barcodeBoundList = barcodeList.toBoundingBox()
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

