package com.hamedrahimvand.barcodex.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.util.*
import kotlin.concurrent.schedule

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

    @FirebaseVisionBarcode.BarcodeFormat
    var supportedFormats: IntArray = intArrayOf(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)

    companion object {
        const val DEFAULT_DETECTION_SPEED = 60L
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        if (isLocked || isPaused) {
            image.close()
            barcodeXAnalyzerCallback.onQrCodesDetected(listOf())
            return
        }
        isLocked = true
        timer.schedule(detectionSpeed) {
            isLocked = false
            try {
                barcodeXAnalyzerCallback.onNewFrame(image.width, image.height)
                val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(
                        supportedFormats[0],
                        *supportedFormats.filterIndexed { index, _ -> index != 0 }.toIntArray()
                    )
                    .build()

                val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

                val rotation = rotationDegreesToFirebaseRotation(image.imageInfo.rotationDegrees)

                val visionImage = FirebaseVisionImage.fromMediaImage(image.image!!, rotation)

                detector.detectInImage(visionImage)
                    .addOnSuccessListener { barcodes ->
                        barcodeXAnalyzerCallback.onQrCodesDetected(barcodes)
                        image.close()
                    }
                    .addOnFailureListener {
                        barcodeXAnalyzerCallback?.onQrCodesFailed(it)
                        image.close()
                    }

            } catch (t: Exception) {
                t.printStackTrace()
                barcodeXAnalyzerCallback?.onQrCodesFailed(t)
                image.close()
            }
        }
    }

    private fun rotationDegreesToFirebaseRotation(rotationDegrees: Int): Int {
        return when (rotationDegrees) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw IllegalArgumentException("Not supported")
        }
    }

    fun pauseDetection(){
        isPaused = true
    }

    fun resumeDetection(){
        isPaused = false
    }
}

interface BarcodeXAnalyzerCallBack {
    fun onNewFrame(w: Int, h: Int) {}
    fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>)
    fun onQrCodesFailed(exception: Exception)
} 