package com.candyhouse.app.tabs.devices.ssm2.room

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.candyhouse.R
import com.candyhouse.app.base.BaseNFG
import com.candyhouse.app.tabs.devices.ssm2.setting.SSM2SettingFG
import com.candyhouse.app.tabs.devices.ssmUIParcer
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHDeviceStatus
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.db.Model.HistoryAndOperater
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.sesame.server.CHResState
import com.candyhouse.sesame.utils.runOnUiThread
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.fg_room_main.*
import org.zakariya.stickyheaders.StickyHeaderLayoutManager
import java.text.SimpleDateFormat
import java.util.*


@ExperimentalUnsignedTypes
class MainRoomFG : BaseNFG() {

    var titleView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_room_main, container, false)
        titleView = view.findViewById(R.id.title)
        instance = this
        return view
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backicon.setOnClickListener { findNavController().navigateUp() }
        title.text = ssm!!.customNickname
        room_list.layoutManager = StickyHeaderLayoutManager()
//        toggle.setBackgroundResource(ssmUIParcer(ssm!!))
        ssmView.setLock(ssm!!)
        ssmView.setOnClickListener { ssm?.toggle() }
        right_icon.setOnClickListener {
            SSM2SettingFG.ssm = ssm
            findNavController().navigate(R.id.action_mainRoomFG_to_SSM2SettingFG)
        }

        refleshHistory()
    }

    @SuppressLint("SimpleDateFormat")
    private fun refleshHistory() {
//        L.d("hcia", "A: ＵＩ主動刷新歷史")
//        L.d("hcia", "A: ＵＩ主動刷新歷史")
        ssm?.requrstHistory { res ->
            if (res!!.isCache) {
                refleshUIList(res.data!!)
            } else {
                val hisRes: CHResState.net.historyRes<List<HistoryAndOperater>> = res as CHResState.net.historyRes
                if (!hisRes.isEnd) {
                    refleshHistory()
                }
                refleshUIList(res.data!!)
            }
        }
    }

    private fun refleshUIList(lists: List<HistoryAndOperater>) {
        if (lists.size == 0) {
            return
        }

        val mTestGoupHistory = lists.groupBy {
            groupTZ().format(showTZ().parse(it.history.timestamp))
        }
        val testGList = mTestGoupHistory.toList()
        val ssss = arrayListOf<Pair<String, List<HistoryAndOperater>>>()
        ssss.addAll(testGList)
        ssss.sortBy {
            groupTZ().parse(it.first)
        }.apply {
            runOnUiThread(Runnable {
                room_list?.adapter = SSMHistoryAdapter(ssss)
                room_list?.adapter?.notifyDataSetChanged()
                if (lists.size == 0) {

                } else {
                    room_list?.layoutManager?.scrollToPosition(room_list.adapter!!.getItemCount() - 1)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        ssm?.delegate = object : CHSesameBleDeviceDelegate {
            override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
                ssmView?.setLock(ssm!!)
            }

            override fun onBleCommandResult(device: CHSesameBleInterface, cmd: SSM2ItemCode, result: SSM2CmdResultCode) {
                if (cmd == SSM2ItemCode.history && result == SSM2CmdResultCode.success) {
                    refleshHistory()
                }
            }
        }
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (device.bleIdStr == ssm?.bleIdStr) {
                    L.d("hcia", "更新設備 evice.bleIdStr :" + device.bleIdStr)

                    device.delegate = ssm?.delegate
                    ssm = device
                    ssm?.connnect()
                    return
                }
            }
        }
    }

    fun reSetName(name: String) {
        titleView?.text = name
    }

    companion object {
        @JvmField
        var ssm: CHSesameBleInterface? = null
        var instance: MainRoomFG? = null
    }

}


