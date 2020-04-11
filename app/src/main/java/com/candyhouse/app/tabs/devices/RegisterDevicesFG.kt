package com.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.CHSesameLockPositionConfiguration
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.utils.L
import com.kasturi.admin.genericadapter.GenericAdapter
import com.utils.recycle.EmptyRecyclerView
import kotlinx.android.synthetic.main.fg_rg_device.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterDevicesFG : Fragment() {

    val deviceMap: MutableMap<String, CHSesameBleInterface> = mutableMapOf()
    var mDeviceList = ArrayList<Pair<String, CHSesameBleInterface>>()
    private lateinit var recyclerView: EmptyRecyclerView

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideMenu()
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (device.isRegistered) {
                    return
                }
                ThreadUtils.runOnUiThread {
                    deviceMap.put(device.ID, device)
                    mDeviceList.clear()
                    mDeviceList.addAll(deviceMap.toList().sortedByDescending { it.second.rssi })
                    (recyclerView.adapter as GenericAdapter<*>).notifyDataSetChanged()
                }
            }
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_rg_device, container, false)

        recyclerView = view.findViewById<EmptyRecyclerView>(R.id.leaderboard_list).apply {
            setHasFixedSize(true)
            adapter = object : GenericAdapter<Any>(mDeviceList) {
                override fun getLayoutId(position: Int, obj: Any): Int {
                    return R.layout.cell_device_unregist
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view),
                            Binder<Pair<String, CHSesameBleInterface>> {
                        var customName: TextView = itemView.findViewById(R.id.title)
                        var productName: TextView = itemView.findViewById(R.id.product_txt)

                        @SuppressLint("SetTextI18n")
                        override fun bind(data: Pair<String, CHSesameBleInterface>, pos: Int) {
                            val sesame = data.second
//                            customName.text =  "" + (sesame.rssi + 130)+"%"+sesame.ID
                            customName.text = "" + (sesame.rssi + 130) + "%"
                            productName.text = getString(R.string.Sesame2)
                            sesame.connnect()
                            itemView.setOnClickListener {
                                MainActivity.activity?.showProgress()
                                if (sesame.chDeviceStatus == CHDeviceStatus.readytoRegister) {
                                    registerSSM(sesame)
                                } else {
                                    sesame.delegate = object : CHSesameBleDeviceDelegate {
                                        override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
                                            if (status == CHDeviceStatus.readytoRegister) {
                                                registerSSM(sesame)
                                            }
                                        }
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
        recyclerView.setEmptyView(empty_view)
        backicon.setOnClickListener { findNavController().navigateUp() }
    }

    private fun RecyclerView.ViewHolder.registerSSM(sesame: CHSesameBleInterface) {
        val timetag = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
        val productTag = getString(R.string.Sesame)
        val ssmName = productTag + timetag
        sesame.register(ssmName) { res ->
            if (res.isSuccess) {
                sesame.configureLockPosition(CHSesameLockPositionConfiguration(0, 256))
                DeviceListFG.instance?.refleshPage()
                itemView?.post {
                    findNavController().navigateUp()
                }
            }
            MainActivity.activity?.hideProgress()
        }
    }


}
