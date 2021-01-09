package com.hamedrahimvand.barcodex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalyzerCallBack
import com.hamedrahimvand.barcodex.utils.CameraXHelper
import kotlinx.android.synthetic.main.activity_barcodex.*

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class BarcodeXActivity : AppCompatActivity(R.layout.activity_barcodex) {

    companion object {
        fun getLauncherIntent(context: Context): Intent =
            Intent(context, BarcodeXActivity::class.java)
    }

    var barcodeXAnalyzerCallBack = object : BarcodeXAnalyzerCallBack {
        override fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>) {
            Log.v(CameraXHelper.TAG, "QRCode detected: $qrCodes")
        }

        override fun onQrCodesFailed(exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeX.setup(this, this)
        barcodeX.addAnalyzerCallBack(barcodeXAnalyzerCallBack)
        captureButton.setOnClickListener{
//            barcodeX.takePhoto() //TODO not implemented yet
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        barcodeX.checkRequestPermissionResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        barcodeX.checkActivityResult(requestCode, resultCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeX.removeAnalyzerCallBack(barcodeXAnalyzerCallBack)
    }


}