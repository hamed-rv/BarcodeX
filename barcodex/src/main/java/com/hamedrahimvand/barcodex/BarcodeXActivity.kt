package com.hamedrahimvand.barcodex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.android.synthetic.main.barcodex_activity.*

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class BarcodeXActivity : AppCompatActivity(R.layout.barcodex_activity), BarcodeXAnalayzerCallBack {
    lateinit var cameraXHelper: CameraXHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraXHelper = CameraXHelper(
            previewView = previewView,
            lifecycleOwner = this,
            barcodeXAnalyzerCallback = this
        )
        cameraXHelper.requestPermission(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraXHelper.checkRequestPermissionResult(
            requestCode = requestCode,
            doOnPermissionGranted = {
                Log.v(CameraXHelper.TAG, "permission granted")
            },
            doOnPermissionNotGranted = {
                Log.v(CameraXHelper.TAG, "permission not granted")
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraXHelper.checkActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            doOnPermissionGranted = {
                Log.v(CameraXHelper.TAG, "permission granted")
            }, doOnPermissionNotGranted = {
                Log.v(CameraXHelper.TAG, "permission not granted")
            }
        )
    }

    override fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>) {
        Log.v(CameraXHelper.TAG, "QRCode detected $qrCodes")
    }

    override fun onQrCodesFailed(exception: Exception) {
        exception.printStackTrace()
    }
}