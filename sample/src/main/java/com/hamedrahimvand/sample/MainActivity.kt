package com.hamedrahimvand.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hamedrahimvand.barcodex.BarcodeXActivity
import com.hamedrahimvand.barcodex.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = BarcodeXActivity.getLauncherIntent(this)
        startActivity(intent)
    }
}