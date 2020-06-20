package com.hamedrahimvand.barcodex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalayzerCallBack
import com.hamedrahimvand.barcodex.utils.CameraXHelper
import kotlinx.android.synthetic.main.barcodex_activity.*

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class BarcodeXActivity : AppCompatActivity(R.layout.barcodex_activity),
    BarcodeXAnalayzerCallBack {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeX.setup(this,this,this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        barcodeX.checkRequestPermissionResult(
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
        barcodeX.checkActivityResult(
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