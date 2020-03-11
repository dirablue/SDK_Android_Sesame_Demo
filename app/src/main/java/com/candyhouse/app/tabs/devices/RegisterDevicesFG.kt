package com.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHDeviceStatus
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.deviceprotocol.CHSesameLockPositionConfiguration
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.utils.L
import com.kasturi.admin.genericadapter.GenericAdapter
import kotlinx.android.synthetic.main.fg_rg_device.*
import java.util.*

class RegisterDevicesFG : Fragment() {

    val deviceMap: MutableMap<String, CHSesameBleInterface> = mutableMapOf()
    var mDeviceList = ArrayList<Pair<String, CHSesameBleInterface>>()
    private lateinit var recyclerView: RecyclerView

    override fun onResume() {
        super.onResume()
        L.d("hcia", "註冊設備列表:")
        (activity as MainActivity).hideMenu()
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (device.isRegistered) {
                    return
                }
//                L.d("hcia", device.bleIdStr+" ID:" + device.ID + "isRegistered:" + device.isRegistered)

                ThreadUtils.runOnUiThread {
                    deviceMap.put(device.ID, device)
                    mDeviceList.clear()
                    mDeviceList.addAll(deviceMap.toList())
                    mDeviceList.forEach {
                        it.second.customNickname
                    }
                    (recyclerView.adapter as GenericAdapter<*>).notifyDataSetChanged()
                }
            }

        }
        L.d("hcia", "請有所有設備列表:")

        CHBleManager.discoverALLDevices(){

        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_rg_device, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.leaderboard_list).apply {
            setHasFixedSize(true)
            adapter = object : GenericAdapter<Any>(mDeviceList) {
                override fun getLayoutId(position: Int, obj: Any): Int {
                    return R.layout.cell_device_unregist
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view),
                            Binder<Pair<String, CHSesameBleInterface>> {
                        var customName: TextView = itemView.findViewById(R.id.title)
                        @SuppressLint("SetTextI18n")
                        override fun bind(data: Pair<String, CHSesameBleInterface>, pos: Int) {
                            val sesame = data.second
                            sesame.connnect()
                            customName.text = sesame.ID + ":" + (sesame.rssi + 130) + ":" + sesame.chDeviceStatus
                            if (sesame.chDeviceStatus == CHDeviceStatus.readytoRegister) {
                                itemView.setOnClickListener {
                                    MainActivity.activity?.showProgress()
                                    sesame.register() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: Any? ->
                                        sesame.configureLockPosition(CHSesameLockPositionConfiguration(0, 256))
                                        DeviceListFG.instance?.refleshPage()
                                        itemView.post {
                                            MainActivity.activity?.hideProgress()
                                        }
                                        findNavController().navigateUp()
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backicon.setOnClickListener { findNavController().navigateUp() }
    }
}
