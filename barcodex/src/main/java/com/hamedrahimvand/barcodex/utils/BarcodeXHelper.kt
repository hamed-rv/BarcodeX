package com.hamedrahimvand.barcodex.utils

import android.content.Context
import android.content.Intent
import com.hamedrahimvand.barcodex.BarcodeX
import com.hamedrahimvand.barcodex.BarcodeXActivity

/**
 *
 *@author Hamed.Rahimvand
 *@since 6/20/20
 */
class BarcodeXHelper {

    companion object{
        fun getBarcodeXIntent(context: Context): Intent = Intent(context,BarcodeXActivity::class.java)
    }
}