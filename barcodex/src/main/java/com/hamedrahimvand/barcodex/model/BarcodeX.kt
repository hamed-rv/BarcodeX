package com.hamedrahimvand.barcodex.model

import android.graphics.Rect

data class BarcodeX(
    var displayValue: String,
    var boundingBox: Rect?,
    var format: Int,
    var rawValue: String,
    var valueType: Int,
    var barcodeBoundingBoxStates: BarcodeBoundingBoxStates = BarcodeBoundingBoxStates.VALID
) {
    companion object {
        const val FORMAT_UNKNOWN = -1
        const val FORMAT_ALL_FORMATS = 0
        const val FORMAT_CODE_128 = 1
        const val FORMAT_CODE_39 = 2
        const val FORMAT_CODE_93 = 4
        const val FORMAT_CODABAR = 8
        const val FORMAT_DATA_MATRIX = 16
        const val FORMAT_EAN_13 = 32
        const val FORMAT_EAN_8 = 64
        const val FORMAT_ITF = 128
        const val FORMAT_QR_CODE = 256
        const val FORMAT_UPC_A = 512
        const val FORMAT_UPC_E = 1024
        const val FORMAT_PDF417 = 2048
        const val FORMAT_AZTEC = 4096
        const val TYPE_UNKNOWN = 0
        const val TYPE_CONTACT_INFO = 1
        const val TYPE_EMAIL = 2
        const val TYPE_ISBN = 3
        const val TYPE_PHONE = 4
        const val TYPE_PRODUCT = 5
        const val TYPE_SMS = 6
        const val TYPE_TEXT = 7
        const val TYPE_URL = 8
        const val TYPE_WIFI = 9
        const val TYPE_GEO = 10
        const val TYPE_CALENDAR_EVENT = 11
        const val TYPE_DRIVER_LICENSE = 12
    }
}
