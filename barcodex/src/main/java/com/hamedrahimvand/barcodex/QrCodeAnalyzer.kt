package com.hamedrahimvand.barcodex

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/14/20
 */
class QrCodeAnalyzer(
    private val qrCodeAnalyzerCallback: QrCodeAnalayzerCallBack?
) : ImageAnalysis.Analyzer {


    override fun analyze(image: ImageProxy) {
        try {
            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()

            val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

            val rotation = rotationDegreesToFirebaseRotation(image.imageInfo.rotationDegrees)

            val metadata: FirebaseVisionImageMetadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(image.width)
                .setHeight(image.height)
                .setRotation(rotation)
                .build()

            val visionImage = FirebaseVisionImage.fromByteBuffer(image.planes[0].buffer, metadata)

            detector.detectInImage(visionImage)
                .addOnSuccessListener { barcodes ->
                    qrCodeAnalyzerCallback?.onQrCodesDetected(barcodes)
                }
                .addOnFailureListener {
                    qrCodeAnalyzerCallback?.onQrCodesFailed(it)
                }

        } catch (t: Exception) {
            t.printStackTrace()
            qrCodeAnalyzerCallback?.onQrCodesFailed(t)
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
}

interface QrCodeAnalayzerCallBack {
    fun onQrCodesDetected(qrCodes: List<FirebaseVisionBarcode>)
    fun onQrCodesFailed(exception: Exception)
} 