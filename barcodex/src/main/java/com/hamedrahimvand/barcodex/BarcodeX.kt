package com.hamedrahimvand.barcodex

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.custom.BarcodeBoundingBox
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxModel
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalayzerCallBack
import com.hamedrahimvand.barcodex.utils.CameraXHelper
import kotlin.math.abs

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/16/20
 */
class BarcodeX : FrameLayout {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    lateinit var cameraXHelper: CameraXHelper
    var barcodeBoundingBox: BarcodeBoundingBox
    var setScale = false

    init {
        View.inflate(context, R.layout.barcodex, this)
        barcodeBoundingBox = findViewById(R.id.barcodeBoundingBox)
    }

    fun setup(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        barcodeXAnalayzerCallBack: BarcodeXAnalayzerCallBack
    ) {
        cameraXHelper = CameraXHelper(
            previewView = findViewById(R.id.previewView),
            lifecycleOwner = lifecycleOwner,
            barcodeXAnalyzerCallback = object : BarcodeXAnalayzerCallBack {
                override fun onNewFrame(w: Int, h: Int) {
                    if (!setScale) {
                        setScale = true
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
                    barcodeBoundingBox.drawBoundingBox(qrCodes.toBoundingBox())
                    barcodeXAnalayzerCallBack.onQrCodesDetected(qrCodes)
                }

                override fun onQrCodesFailed(exception: Exception) {
                    barcodeXAnalayzerCallBack.onQrCodesFailed(exception)
                }

            }
        )
        cameraXHelper.requestPermission(activity)
    }

    fun checkRequestPermissionResult(
        requestCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) = cameraXHelper.checkRequestPermissionResult(
        requestCode, doOnPermissionGranted, doOnPermissionNotGranted
    )

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun checkActivityResult(
        requestCode: Int, resultCode: Int,
        doOnPermissionGranted: () -> Unit = {},
        doOnPermissionNotGranted: () -> Unit = {}
    ) = cameraXHelper.checkActivityResult(
        requestCode,
        resultCode,
        doOnPermissionGranted,
        doOnPermissionNotGranted
    )

    fun List<FirebaseVisionBarcode>.toBoundingBox(): List<BarcodeBoundingBoxModel> {
        return this.map {
            BarcodeBoundingBoxModel(
                it.boundingBox,
                getBarcodeType(it.valueType),
                it.displayValue ?: "",
                getBarcodeBoundingBoxState(it.valueType, it.displayValue ?: "")
            )
        }
    }

    val barcodesMap = mutableMapOf<String, Int>()
    fun getBarcodeBoundingBoxState(valuetype: Int, displayValue: String): BarcodeBoundingBoxStates {
        return if (barcodesMap[displayValue] == valuetype) {
            BarcodeBoundingBoxStates.DUPLICATE
        } else {
            barcodesMap[displayValue] = valuetype
            BarcodeBoundingBoxStates.VALID
        }
    }

    fun getBarcodeType(valueType: Int?): String {
        return when (valueType) {
            //Handle the URL here
            FirebaseVisionBarcode.TYPE_URL ->
                "URL"
            // Handle the contact info here, i.e. address, name, phone, etc.
            FirebaseVisionBarcode.TYPE_CONTACT_INFO ->
                "Contact"
            // Handle the wifi here, i.e. firebaseBarcode.wifi.ssid, etc.
            FirebaseVisionBarcode.TYPE_WIFI ->
                "Wifi"
            // Handle the driver license barcode here, i.e. City, Name, Expiry, etc.
            FirebaseVisionBarcode.TYPE_DRIVER_LICENSE ->
                "Driver License"
            //Handle more types
            else ->
                "Generic"
        }
    }
}

