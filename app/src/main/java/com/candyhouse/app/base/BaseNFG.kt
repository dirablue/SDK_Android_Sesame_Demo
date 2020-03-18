package com.candyhouse.app.base

import androidx.fragment.app.Fragment
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.utils.L

open class BaseNFG : Fragment() {


    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideMenu()
        L.d("hcia", "2 BaseNFG   onResume:")

    }

    override fun onPause() {
        super.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showMenu()

    }
}