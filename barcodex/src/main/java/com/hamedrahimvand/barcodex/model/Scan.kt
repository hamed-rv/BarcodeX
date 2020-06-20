package com.hamedrahimvand.barcodex.model

import android.graphics.Rect
import java.io.Serializable

/**
 * @author vahabghadiri
 * @since 6/14/20
 */

data class BarcodeBoundingBoxModel(
    val rect: Rect?,
    val qrCode: QrCode?,
    val barcodeBoundingBoxStates: BarcodeBoundingBoxStates
)

enum class BarcodeBoundingBoxStates {
    VALID, INVALID, DUPLICATE, SEMI_VALID
}

data class QrCode(val type: String, val value: String)