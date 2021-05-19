package com.hamedrahimvand.barcodex.utils

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.Barcode.FORMAT_ALL_FORMATS
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.hamedrahimvand.barcodex.model.BarcodeX
import com.hamedrahimvand.barcodex.model.XBarcodeMapper
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


    @Barcode.BarcodeFormat
    var supportedFormats: IntArray = intArrayOf(FORMAT_ALL_FORMATS)

    companion object {
        const val DEFAULT_DETECTION_SPEED = 60L
    }

    private var scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                supportedFormats[0],
                *supportedFormats.filterIndexed { index, _ -> index != 0 }.toIntArray()
            )
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image == null) return
        if (isLocked || isPaused) {
            imageProxy.close()
            barcodeXAnalyzerCallback.onQrCodesDetected(listOf())
            return
        }
        isLocked = true
        try {
            barcodeXAnalyzerCallback.onNewFrame(imageProxy.width, imageProxy.height)
            val image =
                InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodeXAnalyzerCallback.onQrCodesDetected(barcodes.map {
                        XBarcodeMapper().mapToXBarcode(it)
                    })
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


    fun pauseDetection() {
        isPaused = true
    }

    fun resumeDetection() {
        isPaused = false
    }
}

interface BarcodeXAnalyzerCallBack {
    fun onNewFrame(w: Int, h: Int) {}
    fun onQrCodesDetected(qrCodes: List<BarcodeX>)
    fun onQrCodesFailed(exception: Exception)
} 