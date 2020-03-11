package com.candyhouse.app.base

import androidx.fragment.app.Fragment
import com.candyhouse.app.tabs.MainActivity

open class BaseFG : Fragment() {

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showMenu()
//        L.d("hcia", " BaseFG  onResume:")
//        L.d("hcia", "UI 開動掃描: ＣＣＣ " )
//        CHBleManager.enableScan()
    }

    override fun onPause() {
        super.onPause()
//        L.d("hcia", " BaseFG onPause:")

    }

}