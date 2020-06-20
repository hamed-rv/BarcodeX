package com.hamedrahimvand.barcodex

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalayzerCallBack
import com.hamedrahimvand.barcodex.utils.CameraXHelper

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

    init {
        View.inflate(context, R.layout.barcodex, this)
    }

    fun setup(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        barcodeXAnalayzerCallBack: BarcodeXAnalayzerCallBack
    ) {
        cameraXHelper = CameraXHelper(
            previewView = findViewById(R.id.previewView),
            lifecycleOwner = lifecycleOwner,
            barcodeXAnalyzerCallback = barcodeXAnalayzerCallBack
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
    )= cameraXHelper.checkActivityResult(requestCode, resultCode, doOnPermissionGranted, doOnPermissionNotGranted)
}