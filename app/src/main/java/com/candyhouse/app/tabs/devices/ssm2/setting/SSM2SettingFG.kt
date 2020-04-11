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
import com.candyhouse.app.base.BaseSSMFG
import com.candyhouse.app.tabs.devices.DeviceListFG
import com.candyhouse.app.tabs.devices.ssm2.menber.AddMemberFG
import com.candyhouse.app.tabs.devices.ssm2.menber.DeleteMemberFG
import com.candyhouse.app.tabs.devices.ssm2.room.MainRoomFG
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.app.tabs.devices.ssm2.setting.angle.SSM2SetAngleFG
import com.candyhouse.sesame.ble.*
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.db.Model.CHMember
import com.candyhouse.sesame.db.Model.CHMemberAndOperater
import com.candyhouse.sesame.deviceprotocol.CHSesameIntention
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.sesame.deviceprotocol.SSM2MechStatus
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
import kotlinx.android.synthetic.main.back_sub.*
import kotlinx.android.synthetic.main.fg_room_main.*
import kotlinx.android.synthetic.main.fg_setting_main.*
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import pe.startapps.alerts.ext.inputTextAlert
import java.util.*

@ExperimentalUnsignedTypes
class SSM2SettingFG : BaseSSMFG() {
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
                val second = selected + 1
                mSesame?.autolock(second) { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                    autolock_status.text = second.toString()
                    autolock_status.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                    second_tv?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                    mWheelView.visibility = View.GONE
                }
            }
        })
        titleTextView?.text = mSesame?.customNickname
        return view
    }

    override fun onResume() {
        super.onResume()
        DfuServiceListenerHelper.registerProgressListener(activity!!, dfuLs)
    }

    override fun onPause() {
        super.onPause()
        DfuServiceListenerHelper.unregisterProgressListener(activity!!, dfuLs)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSesame?.delegate = object : CHSesameBleDeviceDelegate {
            override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
                if (status.value == CHDeviceLoginStatus.logined) {

                    mSesame?.getAutolockSetting() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                        autolock_status?.text = second.toString()
                        autolock_status?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                        second_tv?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE

                        autolockSwitch?.apply {

                            post {
                                autolockSwitch.isChecked = second?.toInt() != 0
                                setOnCheckedChangeListener { buttonView, isChecked ->
                                    if (isChecked) {
                                        mWheelView.visibility = View.VISIBLE
                                    } else {
                                        mSesame?.disableAutolock() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                                            autolock_status.text = second.toString()
                                            autolock_status?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                                            second_tv?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        change_ssm_fr_zone.setOnClickListener {
            val alert = AlertView("", "", AlertStyle.IOS)
            alert.addAction(AlertAction(getString(R.string.ssm_update), AlertActionStyle.NEGATIVE) { action ->
                mSesame?.updateFirmware(R.raw.sesame2) { a, b, c ->
                    val starter = DfuServiceInitiator(mSesame!!.ID)
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
        mSesame?.getAutolockSetting() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
            autolock_status?.text = second.toString()
            autolock_status?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
            second_tv?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE

            autolockSwitch?.apply {
                post {
                    autolockSwitch.isChecked = second?.toInt() != 0
                    setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            mWheelView?.visibility = View.VISIBLE
                        } else {
                            mSesame?.disableAutolock() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                                autolock_status?.text = second.toString()
                                autolock_status?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                                second_tv?.visibility = if (second!!.toInt() == 0) View.GONE else View.VISIBLE
                                mWheelView?.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
        mSesame?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
            firmwareVersion?.post {
                firmwareVersion?.text = tag_ts?.first
            }
        }
        backicon.setOnClickListener { findNavController().navigateUp() }
        chenge_angle_zone.setOnClickListener {
            findNavController().navigate(R.id.action_SSM2SettingFG_to_SSM2SetAngleFG)
        }
        change_ssm_name_zone.setOnClickListener {
            context?.inputTextAlert(getString(R.string.give_cool_name), getString(R.string.change_sesame_name), mSesame?.customNickname) {
                confirmButtonWithText("OK") { name, ss ->
                    mSesame?.renameDevice(ss) {
                        it.onSuccess {
                            titleTextView?.post {
                                titleTextView?.text = ss
                                MainRoomFG.instance?.titleView?.text = ss
                                MainRoomFG.instance?.reSetName(ss)
                                DeviceListFG.instance?.refleshPage()
                            }
                        }
                    }

                    dismiss()
                }
                cancelButton("Cancel")
            }?.show()
        }
        delete_zone.setOnClickListener {
            val alert = AlertView("", "", AlertStyle.IOS)
            alert.addAction(AlertAction(getString(R.string.ssm_delete), AlertActionStyle.NEGATIVE) { action ->
                mSesame?.unregister()
                mSesame?.unregisterServer {
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
        mSesame?.getDeviceMembers() {
            it.onSuccess {
                val ss: List<CHMemberAndOperater> = it.data
                memberList.clear()
                memberList.addAll(ss.sortedByDescending { it.member.role }).apply {
                    memberList.add(CHMemberAndOperater(CHMember("", "", "add", "", "", ""), null))
                    memberList.add(CHMemberAndOperater(CHMember("", "", "delete", "", "", ""), null))
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
                        var king: ImageView = itemView.findViewById(R.id.king)

                        @SuppressLint("SetTextI18n")
                        override fun bind(data: CHMemberAndOperater, pos: Int) {

                            when (data.member.type) {
                                "add" -> {
                                    avatar.setImageResource(R.drawable.ic_icon_add)
                                    avatar.setOnClickListener {
//                                        AddMemberFG.ssm = mSesame
                                        findNavController().navigate(R.id.action_SSM2SettingFG_to_addMemberFG)
                                    }
                                    king.visibility = View.GONE
                                }
                                "delete" -> {
                                    avatar.setImageResource(R.drawable.ic_icon_delete)
                                    avatar.setOnClickListener {
                                        findNavController().navigate(R.id.action_SSM2SettingFG_to_deleteMemberFG)
                                    }
                                    king.visibility = View.GONE

                                }
                                else -> {
                                    king.visibility = if (data.member.role == "OWNER") View.VISIBLE else View.GONE
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


    val dfuLs = object : DfuProgressListener {
        override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            firmwareVersion.post {
                firmwareVersion.text = "$percent%"
            }
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDeviceDisconnecting)//初期化中…
            }
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDeviceDisconnected)//初期化中…
            }
        }

        override fun onDeviceConnected(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDeviceConnected)//初期化中…
            }
        }

        override fun onDfuProcessStarting(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDfuProcessStarting)//初期化中…
            }
        }

        override fun onDfuAborted(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDfuAborted)//初期化中…
            }
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onEnablingDfuMode)//初期化中…
            }
        }

        override fun onDfuCompleted(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDfuCompleted)//完了


                mSesame?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
                    firmwareVersion.post {
                        firmwareVersion.text = tag_ts?.first
                    }
                }
            }
            firmwareVersion.postDelayed(Runnable {
                mSesame?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
                    firmwareVersion.post {
                        firmwareVersion.text = tag_ts?.first
                    }
                }
            }, 5000)
        }

        override fun onFirmwareValidating(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onFirmwareValidating)//初期化中…
            }
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDfuProcessStarted)//初期化中…
            }
        }

        override fun onDeviceConnecting(deviceAddress: String) {
            firmwareVersion.post {
                firmwareVersion.text = getString(R.string.onDeviceConnecting)//初期化中…
            }
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            firmwareVersion.post {
                L.d("hcia", "errorType:" + errorType)
                L.d("hcia", "message:" + message)
                L.d("hcia", "error:" + error)
                L.d("hcia", "deviceAddress:" + deviceAddress)
                firmwareVersion.text = getString(R.string.onDfuProcessStarted) + ":" + message
            }
        }


    }

}

