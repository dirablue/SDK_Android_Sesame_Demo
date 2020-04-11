package com.candyhouse.app.tabs.devices.ssm2.test

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.candyhouse.R
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHDeviceStatus
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.*
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.activity_ble_control.*


@ExperimentalUnsignedTypes
class BlueSesameControlActivity : AppCompatActivity(), CHSesameBleDeviceDelegate, CHBleManagerDelegate {
    companion object {
        @JvmField
        var ssm: CHSesameBleInterface? = null
    }

    var lockDegree: Short = 0
    var unlockDegree: Short = 0
    var nowDegree: Short = 0

    override fun onResume() {
        super.onResume()
        CHBleManager.delegate = this
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_control)
        ssm?.delegate = this
        ssm?.connnect()

        accessLevel.setText("accessLevel: " + ssm?.accessLevel?.value)
        bleid.setText("bleid: " + ssm?.bleIdStr)
        connectStatus.setText(ssm!!.chDeviceStatus.toString() + " :" + ssm!!.chDeviceStatus.value.toString())
        nickname.setText("nickname: " + ssm?.customNickname)
        registerstatus.setText(if (ssm!!.isRegistered) "register" else "unregister")

        register.setOnClickListener {
            ssm?.register("test") { res ->
            }
        }


        connectBtn.setOnClickListener { ssm?.connnect() }
        disconnectBtn.setOnClickListener { ssm?.disconnect() }
        setAngle.setOnClickListener { ssm?.configureLockPosition(CHSesameLockPositionConfiguration(lockDegree, unlockDegree)) }
        setLockAngle.setOnClickListener {
            setLockAngle.text = "" + nowDegree
            lockDegree = nowDegree
        }
        setUnLockAngle.setOnClickListener {
            setUnLockAngle.text = "" + nowDegree
            unlockDegree = nowDegree
        }
        lockBtn.setOnClickListener { ssm?.lock() }
        unlockBtn.setOnClickListener { ssm?.unlock() }
        resetSSM.setOnClickListener { ssm?.unregister() }
        unregisterServer.setOnClickListener {
            ssm?.unregisterServer() {
                it.onSuccess {
                }
            }
        }

        enableAutolock.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Second")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)
            builder.setPositiveButton("OK", { dialogInterface: DialogInterface, i: Int ->
                val inputSecond = input.text.toString()
                ssm?.autolock(inputSecond.toInt()) { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                    autolockStatus.text = second.toString()
                }
            })
            builder.setNegativeButton("Cancle", { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            })
            builder.show()

        }
        disableAutolock.setOnClickListener {
            ssm?.disableAutolock() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                autolockStatus.text = second.toString()
            }
        }
        readAutolock.setOnClickListener {
            ssm?.getAutolockSetting() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: UShort? ->
                autolockStatus.text = second.toString()
            }
        }
        firmwareVersion.setOnClickListener {
            ssm?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
                firmwareVersion.post {
                    firmwareVersion.setText(tag_ts?.first)
                }
            }
        }
        ssm?.getVersionTag() { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, tag_ts: Pair<String, Long>? ->
            firmwareVersion.post {
                firmwareVersion.setText(tag_ts!!.first)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun didDiscoverSesame(device: CHSesameBleInterface) {
        runOnUiThread(Runnable {
            if (ssm?.bleIdStr == device.bleIdStr) {
                ssm = device
                ssm?.delegate = this
                registerstatus.setText(if (ssm!!.isRegistered) "register" else "unregister")
                registerstatus.setTextColor(if (ssm!!.isRegistered) Color.RED else Color.BLACK)
                connectStatus.setText(ssm!!.chDeviceStatus.toString() + " :" + ssm!!.chDeviceStatus.value.toString())
            }
        })

    }

    @SuppressLint("SetTextI18n")
    override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
        connectStatus.setText(ssm!!.chDeviceStatus.toString() + " :" + ssm!!.chDeviceStatus.value.toString())
    }


    override fun onMechStatusChanged(device: CHSesameBleInterface, status: SSM2MechStatus, intention: CHSesameIntention) {
        nowAngle.setText("angle:" + status.position)
        nowDegree = status.position
        lockState.setText(if (status.inLockRange) "locked" else if (status.inUnlockRange) "unlocked" else "moved")
        moveState.setText(intention.value)
    }

    override fun onMechSettingChanged(device: CHSesameBleInterface, setting: SSM2MechSetting) {
        setLockAngle.setText(setting.lockPosition.toString())
        setUnLockAngle.setText(setting.unlockPosition.toString())
    }

    override fun onBleCommandResult(device: CHSesameBleInterface, cmd: SSM2ItemCode, result: SSM2CmdResultCode) {
                cmdResult.setText("" + cmd + " " + result)
    }

}
