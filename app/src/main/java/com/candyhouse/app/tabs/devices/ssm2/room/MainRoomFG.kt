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
import com.candyhouse.app.base.BaseSSMFG
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
import kotlinx.android.synthetic.main.back_sub.*
import kotlinx.android.synthetic.main.fg_room_main.*
import org.zakariya.stickyheaders.StickyHeaderLayoutManager
import java.text.SimpleDateFormat
import java.util.*


@ExperimentalUnsignedTypes
class MainRoomFG : BaseSSMFG() {

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
        backTitle.text = mSesame!!.customNickname
        backTitle.visibility = View.VISIBLE
        room_list.layoutManager = StickyHeaderLayoutManager()
        ssmView.setLock(mSesame!!)
        ssmView.setOnClickListener { mSesame?.toggle() }
        right_icon.setOnClickListener {
            findNavController().navigate(R.id.action_mainRoomFG_to_SSM2SettingFG)
        }

        refleshHistory()
    }

    @SuppressLint("SimpleDateFormat")
    private fun refleshHistory() {
        mSesame?.requrstHistory { res ->
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
        val tmpList = arrayListOf<Pair<String, List<HistoryAndOperater>>>()
        tmpList.addAll(testGList)
        tmpList.sortBy {
            groupTZ().parse(it.first)
        }.apply {
            runOnUiThread(Runnable {
                room_list?.adapter = SSMHistoryAdapter(tmpList)
                room_list?.adapter?.notifyDataSetChanged()
                if (lists.size != 0) {
                    room_list?.layoutManager?.scrollToPosition(room_list.adapter!!.getItemCount() - 1)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        mSesame?.delegate = object : CHSesameBleDeviceDelegate {
            override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
                ssmView?.setLock(mSesame!!)
            }

            override fun onBleCommandResult(device: CHSesameBleInterface, cmd: SSM2ItemCode, result: SSM2CmdResultCode) {
                if (cmd == SSM2ItemCode.history && result == SSM2CmdResultCode.success) {
                    refleshHistory()
                }
            }
        }
    }

    fun reSetName(name: String) {
        titleView?.text = name
    }

    companion object {
        @JvmField
        var instance: MainRoomFG? = null
    }

}


