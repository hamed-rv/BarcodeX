package com.hamedrahimvand.barcodex.utils

import android.content.Context
import android.util.TypedValue

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/16/20
 */
fun dpToPx(context: Context, dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
