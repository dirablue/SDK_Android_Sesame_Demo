package com.candyhouse.app.tabs.devices.ssm2.setting

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.candyhouse.R
import com.candyhouse.app.base.BaseNFG
import com.candyhouse.app.tabs.devices.DeviceListFG
import com.candyhouse.app.tabs.devices.ssm2.menber.AddMemberFG
import com.candyhouse.app.tabs.devices.ssm2.menber.DeleteMemberFG
import com.candyhouse.app.tabs.devices.ssm2.room.MainRoomFG
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.app.tabs.devices.ssm2.setting.angle.SSM2SetAngleFG
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.db.Model.CHMember
import com.candyhouse.sesame.db.Model.CHMemberAndOperater
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.utils.L
import com.utils.alertview.enums.AlertActionStyle
import com.utils.alertview.enums.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.kasturi.admin.genericadapter.GenericAdapter
import com.kasturi.admin.genericadapter.toDp
import com.kasturi.admin.genericadapter.toPx
import com.utils.recycle.GridSpacingItemDecoration
import com.utils.wheelview.WheelView
import com.utils.wheelview.WheelviewAdapter
import kotlinx.android.synthetic.main.fg_room_main.*
import kotlinx.android.synthetic.main.fg_setting_main.*
import kotlinx.android.synthetic.main.fg_setting_main.backicon
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import pe.startapps.alerts.ext.inputTextAlert
import java.util.*

@ExperimentalUnsignedTypes
class SSM2SettingFG : BaseNFG() {
    val dfuLs = object : DfuProgressListener {
        override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            L.d("hcia", deviceAddress + ":" + percent)
            firmwareVersion.post {
                firmwareVersion.text = "" + percent + "%"
            }
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            L.d("hcia", deviceAddress!!)
            firmwareVersion.post {
                firmwareVersion.text = "onDeviceDisconnecting"
            }
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDeviceDisconnected"
            }
        }

        override fun onDeviceConnected(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDeviceConnected"
            }
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDfuProcessStarting"
            }
        }

        override fun onDfuAborted(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDfuAborted"
            }
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onEnablingDfuMode"
            }
        }

        override fun onDfuCompleted(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDfuCompleted"

                ssm?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
                    firmwareVersion.post {
                        firmwareVersion.text = tag_ts?.first
                    }
                }
            }
            firmwareVersion.postDelayed(Runnable {
                ssm?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
                    firmwareVersion.post {
                        firmwareVersion.text = tag_ts?.first
                    }
                }
            }, 5000)
        }

        override fun onFirmwareValidating(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onFirmwareValidating"
            }
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDfuProcessStarted"
            }
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onError:" + message
            }
        }

        override fun onDeviceConnecting(deviceAddress: String) {
            L.d("hcia", deviceAddress)
            firmwareVersion.post {
                firmwareVersion.text = "onDeviceConnecting:"
            }
        }

    }

    var memberList = ArrayList<CHMemberAndOperater>()
    var titleTextView: TextView? = null
    lateinit var mWheelView: WheelView<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_setting_main, container, false)
        titleTextView = view.findViewById(R.id.titlec)
        mWheelView = view.findViewById(R.id.wheelview)

        val secondSetting = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val providerAdapter = WheelviewAdapter(secondSetting.toList())
        mWheelView.setAdapter(providerAdapter)
        mWheelView.setWheelScrollListener(object : WheelView.WheelScrollListener {
            override fun changed(selected: Int, name: Any?) {
                L.d("hcia", "selected:" + selected + " name:" + name)
                val second = selected + 1
                ssm?.autolock(second) { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                    L.d("hcia", "cmd:" + cmd)
                    L.d("hcia", "res:" + res)
                    autolock_status.text = second.toString()
                    mWheelView.visibility = View.GONE
                }
            }
        })
        titleTextView?.text = ssm?.customNickname
        return view
    }

    override fun onResume() {
        super.onResume()
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (device.bleIdStr == ssm?.bleIdStr) {
                    L.d("hcia", "更新設備 evice.bleIdStr :" + device.bleIdStr)
                    ssm = device
                    ssm?.connnect()

                    return
                }
            }
        }
        DfuServiceListenerHelper.registerProgressListener(activity!!, dfuLs)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.unregisterProgressListener(activity!!, dfuLs)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        change_ssm_fr_zone.setOnClickListener {
            L.d("hcia", "it:" + it)

            val alert = AlertView("", "", AlertStyle.IOS)
            alert.addAction(AlertAction(getString(R.string.ssm_update), AlertActionStyle.NEGATIVE) { action ->
                ssm?.updateFirmware(R.raw.a6c801cdc) { a, b, c ->
                    val starter = DfuServiceInitiator(ssm!!.ID)
                    starter.setZip(R.raw.sesame2)
//            starter.setScope(DfuServiceInitiator.SCOPE_APPLICATION)
                    starter.setPacketsReceiptNotificationsEnabled(false)
                    starter.setPrepareDataObjectDelay(400)
                    starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
                    starter.setDisableNotification(true)
                    starter.setForeground(false)
                    starter.start(activity!!, DfuService::class.java)
                }
            })
            alert.show(activity as AppCompatActivity)

        }
        ssm?.getAutolockSetting() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
            autolock_status?.text = second.toString()
            autolockSwitch.apply {

                post {
                    autolockSwitch.isChecked = second?.toInt() != 0
                    setOnCheckedChangeListener { buttonView, isChecked ->

                        L.d("hcia", "isChecked:" + isChecked)

                        if (isChecked) {

                            mWheelView.visibility = View.VISIBLE

                        } else {
                            ssm?.disableAutolock() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                                autolock_status.text = second.toString()
                            }
                        }
                    }

                }
            }
        }
        /**
         * todo
         *  java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.widget.Switch.post(java.lang.Runnable)' on a null object reference
        at com.candyhouse.app.tabs.devices.ssm2.setting.SSM2SettingFG$onViewCreated$2.invoke(SSM2SettingFG.kt:235)
        at com.candyhouse.app.tabs.devices.ssm2.setting.SSM2SettingFG$onViewCreated$2.invoke(SSM2SettingFG.kt:57)
        at com.candyhouse.sesame.ble.Sesame2.Sesame2BleDevice$getAutolockSetting$1$1.run(Sesame2BleDevice.kt:138)
         *
        * */
        ssm?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
            firmwareVersion.post {
                firmwareVersion?.text = tag_ts?.first
            }
        }
        backicon.setOnClickListener { findNavController().navigateUp() }
        chenge_angle_zone.setOnClickListener {
            SSM2SetAngleFG.ssm = ssm
            findNavController().navigate(R.id.action_SSM2SettingFG_to_SSM2SetAngleFG)
        }
        change_ssm_name_zone.setOnClickListener {
            context?.inputTextAlert(getString(R.string.change_sesame_name), "") {
                confirmButtonWithText("OK") { name, ss ->
                    ssm?.renameDevice(ss) {
                        it.onSuccess {
                            titleTextView?.post {
                                titleTextView?.text = ss
                                MainRoomFG.instance?.title?.text = ss
                                MainRoomFG.instance?.reSetName(ss)
                                DeviceListFG.instance?.refleshPage()
                            }
                        }
                    }
                    dismiss()
                }
                cancelButton(getString(R.string.cancel))
            }?.show()
        }
        delete_zone.setOnClickListener {
            val alert = AlertView(getString(R.string.ssm_delete), "", AlertStyle.IOS)
            alert.addAction(AlertAction(getString(R.string.ssm_delete), AlertActionStyle.NEGATIVE) { action ->
                ssm?.unregister()
                ssm?.unregisterServer {
                    it.onSuccess {
                        findNavController().navigateUp()
                        findNavController().navigateUp()
                        DeviceListFG.instance?.refleshPage()

                    }
                    it.onFailure {
                        findNavController().navigateUp()
                        findNavController().navigateUp()
                        DeviceListFG.instance?.refleshPage()
                    }
                }
            })
            alert.show(activity as AppCompatActivity)


        }
        ssm?.getDeviceMembers() {
            it.onSuccess {
//                L.d("hcia", "UI收到成員it:" + it)
                val ss: List<CHMemberAndOperater> = it.data
                memberList.clear()
                memberList.addAll(ss).apply {
                    memberList.add(CHMemberAndOperater(CHMember("", "add", "", "", ""), null))
                    memberList.add(CHMemberAndOperater(CHMember("", "delete", "", "", ""), null))
                    list?.post {
                        list?.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }
        list.apply {
            val spanCount = Resources.getSystem().displayMetrics.widthPixels.toDp() / 60
            val ss = GridLayoutManager(context, spanCount)
            ss.setOrientation(LinearLayoutManager.VERTICAL)
            val sdsd = Resources.getSystem().displayMetrics.widthPixels - spanCount * 60.toPx()
            val spacing = sdsd / (spanCount + 1)
            val includeEdge = true
            addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            layoutManager = ss
            adapter = object : GenericAdapter<CHMemberAndOperater>(memberList) {
                override fun getLayoutId(position: Int, obj: CHMemberAndOperater): Int {
                    return R.layout.cell_member
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view),
                            Binder<CHMemberAndOperater> {
                        var avatar: ImageView = itemView.findViewById(R.id.avatar)

                        @SuppressLint("SetTextI18n")
                        override fun bind(data: CHMemberAndOperater, pos: Int) {

                            when (data.member.type) {
                                "add" -> {
                                    avatar.setImageResource(R.drawable.ic_icon_add)
                                    avatar.setOnClickListener {
                                        AddMemberFG.ssm = ssm
                                        findNavController().navigate(R.id.action_SSM2SettingFG_to_addMemberFG)
                                    }
                                }
                                "delete" -> {
                                    avatar.setImageResource(R.drawable.ic_icon_delete)
                                    avatar.setOnClickListener {
                                        DeleteMemberFG.ssm = ssm
                                        findNavController().navigate(R.id.action_SSM2SettingFG_to_deleteMemberFG)
                                    }
                                }
                                else -> {
                                    avatar.setImageDrawable(avatatImagGenaroter(data.opetator?.firstname))
                                    avatar.setOnClickListener { }
                                }
                            }

                        }
                    }
                }
            }
        }
    }//end view created

    companion object {
        @JvmField
        var ssm: CHSesameBleInterface? = null
    }
}

