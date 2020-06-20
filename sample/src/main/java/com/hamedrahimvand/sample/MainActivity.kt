package com.hamedrahimvand.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hamedrahimvand.barcodex.R
import com.hamedrahimvand.barcodex.utils.BarcodeXHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = BarcodeXHelper.getBarcodeXIntent(this)
        startActivity(intent)
    }
}