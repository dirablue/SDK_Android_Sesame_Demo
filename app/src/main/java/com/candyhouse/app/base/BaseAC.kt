package com.candyhouse.app.base

import androidx.appcompat.app.AppCompatActivity

open class BaseAC : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
//        L.d("hcia", " onResume:")
//        CHBleManager.enableScan()
    }

    override fun onPause() {
        super.onPause()
//        L.d("hcia", " onPause:")
//        CHBleManager.disableScan()
    }

}