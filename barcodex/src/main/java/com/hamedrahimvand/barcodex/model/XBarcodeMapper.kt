package com.hamedrahimvand.barcodex.model

import com.google.mlkit.vision.barcode.Barcode
import com.hamedrahimvand.barcodex.utils.toBarcodeType

class XBarcodeMapper {

    fun mapToXBarcode(barcode: Barcode): XBarcode {
        return XBarcode(
            barcode.displayValue ?: "",
            barcode.boundingBox,
            barcode.format,
            barcode.rawValue ?: "",
            barcode.valueType,
        )
    }

    fun mapToBoundingBox(xBarcode: XBarcode): BarcodeBoundingBoxModel {
        return BarcodeBoundingBoxModel(
            xBarcode.boundingBox,
            xBarcode.valueType.toBarcodeType(),
            xBarcode.displayValue,
            xBarcode.barcodeBoundingBoxStates
        )
    }
}