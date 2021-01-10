package com.hamedrahimvand.barcodex

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.custom.BarcodeBoundingBox
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates
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
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var cameraXHelper: CameraXHelper
    private val analyzerCallBacks: MutableList<BarcodeXAnalyzerCallBack> = mutableListOf()
    private val barcodeMap = mutableMapOf<String, Int>()

    private var barcodeBoundingBox: BarcodeBoundingBox
    private var isScaled = false

    init {
        View.inflate(context, R.layout.barcodex, this)
        barcodeBoundingBox = findViewById(R.id.barcodeBoundingBox)
    }

    fun setup(
        activity: Activity,
        lifecycleOwner: LifecycleOwner
    ) {
        cameraXHelper = CameraXHelper(
            previewView = findViewById(R.id.previewView),
            lifecycleOwner = lifecycleOwner,
            barcodeXAnalyzerCallback = analyzerCallBack
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
            if(!isScaled) {
                isScaled = true
                val min: Int = w.coerceAtMost(h)
                val max: Int = w.coerceAtLeast(h)
                val scale = (height.toFloat() / max).coerceAtLeast(width.toFloat() / min)
                if (height < max) {
                    barcodeBoundingBox.scaleY = 1f
                    barcodeBoundingBox.translationY = (-abs(height - max)).toFloat() / 2
                } else {
                    barcodeBoundingBox.scaleY = scale
                    barcodeBoundingBox.translationY =
                        ((height - max).toFloat() / 2) * barcodeBoundingBox.scaleY
                }
                if (width < min) {
                    barcodeBoundingBox.scaleX = 1f
                    barcodeBoundingBox.translationX = (-abs(width - min)).toFloat() / 2
                } else {
                    barcodeBoundingBox.scaleX = scale
                    barcodeBoundingBox.translationX =
                        ((width - min).toFloat() / 2) * barcodeBoundingBox.scaleX
                }
            }
        }

        override fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>) {
            //draw
            val barcodeBoundList = qrCodes.toBoundingBox() {
                getBarcodeBoundingBoxState(it.valueType, it.displayValue ?: "")
            }
            barcodeBoundingBox.drawBoundingBox(barcodeBoundList)
            analyzerCallBacks.forEach {
                it.onQrCodesDetected(qrCodes)
            }
        }

        override fun onQrCodesFailed(exception: Exception) {
            analyzerCallBacks.forEach {
                it.onQrCodesFailed(exception)
            }
        }
    }

    fun recalculate(){
        isScaled = false
    }

    fun takePhoto(
        file: File,
        doOnPhotoTaken: (ImageCapture.OutputFileResults) -> Unit,
        doOnError: (ImageCaptureException) -> Unit
    ) = cameraXHelper.takePhoto(file, doOnPhotoTaken, doOnError)

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if(event?.action == MotionEvent.ACTION_DOWN){
//            cameraXHelper.onTouch(event.x,event.y)
//        }
//        return super.onTouchEvent(event)
//    }
}

