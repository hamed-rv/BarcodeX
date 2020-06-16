package com.hamedrahimvand.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hamedrahimvand.barcodex.BarcodeXActivity
import com.hamedrahimvand.barcodex.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this, BarcodeXActivity::class.java))
    }
}