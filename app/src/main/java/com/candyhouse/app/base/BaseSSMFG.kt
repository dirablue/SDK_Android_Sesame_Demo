package com.candyhouse.app.base

import androidx.fragment.app.Fragment
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.utils.L

open class BaseSSMFG : BaseNFG() {
    companion object {
        @JvmField
        var mSesame: CHSesameBleInterface? = null
    }

    override fun onResume() {
        super.onResume()
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (device.bleIdStr == mSesame?.bleIdStr) {
                    device.delegate = mSesame?.delegate
                    mSesame = device
                    mSesame?.connnect()
                    return
                }
            }
        }
    }
}