package com.hamedrahimvand.barcodex.model

import android.graphics.Rect
import java.io.Serializable

/**
 * @author vahabghadiri
 * @since 6/14/20
 */

data class BarcodeBoundingBoxModel(
    val rect: Rect?,
    val type: String, val value: String,
    val barcodeBoundingBoxStates: BarcodeBoundingBoxStates
)

enum class BarcodeBoundingBoxStates {
    VALID, INVALID, DUPLICATE
}
