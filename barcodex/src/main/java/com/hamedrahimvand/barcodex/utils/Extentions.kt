package com.hamedrahimvand.barcodex.utils

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxModel
import com.hamedrahimvand.barcodex.model.BarcodeBoundingBoxStates

/**
 *
 *@author Hamed.Rahimvand
 *@since 1/9/21
 */
inline fun List<FirebaseVisionBarcode>.toBoundingBox(getBarcodeBoundingBoxState: (FirebaseVisionBarcode) -> BarcodeBoundingBoxStates): List<BarcodeBoundingBoxModel> {
    return this.map {
        BarcodeBoundingBoxModel(
            it.boundingBox,
            it.valueType.toBarcodeType(),
            it.displayValue ?: "",
            getBarcodeBoundingBoxState(it)
        )
    }
}

fun Int.toBarcodeType(): String {
    return when (this) {
        //Handle the URL here
        FirebaseVisionBarcode.TYPE_URL ->
            "URL"
        // Handle the contact info here, i.e. address, name, phone, etc.
        FirebaseVisionBarcode.TYPE_CONTACT_INFO ->
            "Contact"
        // Handle the wifi here, i.e. firebaseBarcode.wifi.ssid, etc.
        FirebaseVisionBarcode.TYPE_WIFI ->
            "Wifi"
        // Handle the driver license barcode here, i.e. City, Name, Expiry, etc.
        FirebaseVisionBarcode.TYPE_DRIVER_LICENSE ->
            "Driver License"
        //Handle more types
        else ->
            "Generic"
    }
}