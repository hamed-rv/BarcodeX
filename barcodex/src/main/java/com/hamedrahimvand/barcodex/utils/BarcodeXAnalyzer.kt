package com.hamedrahimvand.barcodex.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.Surface.*
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.Barcode.FORMAT_ALL_FORMATS
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.*

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class BarcodeXAnalyzer(
    private val barcodeXAnalyzerCallback: BarcodeXAnalyzerCallBack
) : ImageAnalysis.Analyzer {

    private val timer = Timer()
    private var isLocked = false
    private var isPaused = false
    var detectionSpeed = DEFAULT_DETECTION_SPEED


    @Barcode.BarcodeFormat
    var supportedFormats: IntArray = intArrayOf(FORMAT_ALL_FORMATS)

    companion object {
        const val DEFAULT_DETECTION_SPEED = 60L
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (isLocked || isPaused) {
            imageProxy.close()
            barcodeXAnalyzerCallback.onQrCodesDetected(listOf())
            return
        }
        isLocked = true
        try {
            barcodeXAnalyzerCallback.onNewFrame(imageProxy.width, imageProxy.height)

            val rotation = rotationDegreesToFirebaseRotation(imageProxy.imageInfo.rotationDegrees)
            val image = InputImage.fromMediaImage( imageProxy.image, imageProxy.imageInfo.rotationDegrees)

            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        supportedFormats[0],
                        *supportedFormats.filterIndexed { index, _ -> index != 0 }.toIntArray()
                    )
                    .build()
            )


            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes?.firstOrNull().let { barcode ->
                        val rawValue = barcode?.rawValue
                        rawValue?.let {
                            Log.d("Barcode", it)
                            barcodeXAnalyzerCallback.onQrCodesDetected(barcodes)
                        }
                    }

                    isLocked = false
                    imageProxy.close()
                }
                .addOnFailureListener {
                    isLocked = false
                    barcodeXAnalyzerCallback.onQrCodesFailed(it)
                    imageProxy.close()
                }
        } catch (t: Exception) {
            t.printStackTrace()
            barcodeXAnalyzerCallback.onQrCodesFailed(t)
            isLocked = false
            imageProxy.close()
        }
    }

    private fun rotationDegreesToFirebaseRotation(rotationDegrees: Int): Int {
        return when (rotationDegrees) {
            0 -> ROTATION_0
            90 -> ROTATION_90
            180 -> ROTATION_180
            270 -> ROTATION_270
            else -> throw IllegalArgumentException("Not supported")
        }
    }

    fun pauseDetection() {
        isPaused = true
    }

    fun resumeDetection() {
        isPaused = false
    }
}

interface BarcodeXAnalyzerCallBack {
    fun onNewFrame(w: Int, h: Int) {}
    fun onQrCodesDetected(qrCodes: List<Barcode>)
    fun onQrCodesFailed(exception: Exception)
} 