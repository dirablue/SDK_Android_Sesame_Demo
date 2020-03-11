package com.candyhouse.app.base

import androidx.fragment.app.Fragment
import com.candyhouse.app.tabs.MainActivity

open class BaseNFG : Fragment() {


    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideMenu()
    }

    override fun onPause() {
        super.onPause()
//        CHBleManager.delegate = null
    }

}