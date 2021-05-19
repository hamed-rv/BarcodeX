package com.hamedrahimvand.barcodex.utils

import com.google.mlkit.vision.barcode.Barcode
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxModel
import com.hamedrahimvand.barcodex.model.BarcodeX
import com.hamedrahimvand.barcodex.model.XBarcodeMapper

/**
 *
 *@author Hamed.Rahimvand
 *@since 1/9/21
 */
fun List<BarcodeX>.toBoundingBox(): List<BarcodeBoundingBoxModel> {
    return this.map { xbarcode ->
        XBarcodeMapper().mapToBoundingBox(xbarcode)
    }
}

fun Int.toBarcodeType(): String {
    return when (this) {
        //Handle the URL here
        Barcode.TYPE_URL ->
            "URL"
        // Handle the contact info here, i.e. address, name, phone, etc.
        Barcode.TYPE_CONTACT_INFO ->
            "Contact"
        // Handle the wifi here, i.e. firebaseBarcode.wifi.ssid, etc.
        Barcode.TYPE_WIFI ->
            "Wifi"
        // Handle the driver license barcode here, i.e. City, Name, Expiry, etc.
        Barcode.TYPE_DRIVER_LICENSE ->
            "Driver License"
        //Handle more types
        else ->
            "Generic"
    }
}