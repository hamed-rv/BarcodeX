package com.hamedrahimvand.barcodex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.FORMAT_CODE_128
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.FORMAT_QR_CODE
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
        barcodeX.setup(this, this,intArrayOf(FORMAT_CODE_128, FORMAT_QR_CODE))
        barcodeX.addAnalyzerCallBack(barcodeXAnalyzerCallBack)
        ibTorch.setOnClickListener{
            toggleTorch()
        }
    }

    private fun toggleTorch() {
        when (barcodeX.toggleTorch()) {
            true -> {
                ibTorch.setImageResource(R.drawable.ic_baseline_flash_off_24)
            }
            false -> {
                ibTorch.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }
            else -> {
                ibTorch.setImageResource(R.drawable.ic_baseline_no_flash_24)
                Toast.makeText(this, "Device does not have Flash", Toast.LENGTH_SHORT).show()
            }
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