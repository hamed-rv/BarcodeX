package com.hamedrahimvand.sample

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates
import com.hamedrahimvand.barcodex.model.BarcodeX
import com.hamedrahimvand.barcodex.model.BarcodeX.Companion.FORMAT_CODE_128
import com.hamedrahimvand.barcodex.model.BarcodeX.Companion.FORMAT_QR_CODE
import com.hamedrahimvand.barcodex.utils.BarcodeXAnalyzerCallBack
import kotlinx.android.synthetic.main.activity_barcodex.*

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class BarcodeXActivity : AppCompatActivity(R.layout.activity_barcodex) {

    companion object {
        const val BARCODE_SOUND_INTERVAL = 1000
        fun getLauncherIntent(context: Context): Intent =
            Intent(context, BarcodeXActivity::class.java)
    }

    var myBarcodeList: MutableList<BarcodeX> = mutableListOf()
    var lastNotifyTime = System.currentTimeMillis()
    var barcodeXAnalyzerCallBack = object : BarcodeXAnalyzerCallBack {
        override fun onQrCodesDetected(qrCodes: List<BarcodeX>) {
            //find difference between myBarcodeList and qrCodes by displayValue
            //combine two list
            val tempList = qrCodes + myBarcodeList
            //omit duplicate items
            val diff = tempList.distinctBy { it.displayValue }

            //alarm once
            val newItems = diff.size - myBarcodeList.size

            if (newItems != 0 && System.currentTimeMillis() - lastNotifyTime > BARCODE_SOUND_INTERVAL) {
                //avoid alarming under interval time
                beepVibrate()
                lastNotifyTime = System.currentTimeMillis()
            }

            myBarcodeList = diff.toMutableList()

            //draw all due to business logic
            barcodeX.drawBoundaries(qrCodes)
            tvCount.text = myBarcodeList.size.toString()
        }

        override fun onQrCodesFailed(exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getBarcodeBoundingBoxState(
        type: Int,
        displayValue: String
    ): BarcodeBoundingBoxStates {
        return BarcodeBoundingBoxStates.VALID

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeX.setup(this, this, intArrayOf(FORMAT_CODE_128, FORMAT_QR_CODE))
        barcodeX.autoDrawEnabled = false
//        barcodeX.threshold = 2
        barcodeX.addAnalyzerCallBack(barcodeXAnalyzerCallBack)
        ibTorch.setOnClickListener {
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


    @Suppress("DEPRECATION")
    fun beepVibrate() {
        MediaPlayer.create(this, R.raw.beep).start()
//        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            v!!.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
//        } else {
//            v!!.vibrate(200)
//        }
    }

}