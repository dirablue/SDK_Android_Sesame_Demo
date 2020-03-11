package com.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.candyhouse.R
import com.candyhouse.app.base.BaseNFG
import com.candyhouse.sesame.ble.*
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.CHSesameIntention
import com.candyhouse.sesame.deviceprotocol.CHSesameLockPositionConfiguration
import com.candyhouse.sesame.deviceprotocol.SSM2MechStatus
import com.kasturi.admin.genericadapter.GenericAdapter
import kotlinx.android.synthetic.main.fg_set_angle.*


@ExperimentalUnsignedTypes
class SSM2SetAngleFG : BaseNFG() {
    var titleTextView: TextView? = null
    var ssmView: SesameView? = null

    companion object {
        @JvmField
        var ssm: CHSesameBleInterface? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_set_angle, container, false)
        titleTextView = view.findViewById(R.id.titlec)
        ssmView = view.findViewById(R.id.ssmView)
        return view
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView?.text = ssm?.customNickname
        backicon.setOnClickListener {
            findNavController().navigateUp()
        }
        ssmView?.setLock(ssm!!)
        ssmView?.setOnClickListener { ssm?.toggle() }

        setunlock_zone?.setOnClickListener {
            if (ssm?.chDeviceStatus?.value == CHDeviceLoginStatus.unlogined) {
                return@setOnClickListener
            }
            ssm?.configureLockPosition(CHSesameLockPositionConfiguration(ssm?.mSSM2MechSetting!!.lockPosition, ssm?.mechStatus!!.position))
        }
        setlock_zone?.setOnClickListener {
            if (ssm?.chDeviceStatus?.value == CHDeviceLoginStatus.unlogined) {
                return@setOnClickListener
            }
            ssm?.configureLockPosition(CHSesameLockPositionConfiguration(ssm?.mechStatus!!.position, ssm?.mSSM2MechSetting!!.unlockPosition))
        }

        ssm?.delegate = object : CHSesameBleDeviceDelegate {
            override fun onMechStatusChanged(device: CHSesameBleInterface, status: SSM2MechStatus, intention: CHSesameIntention) {
                super.onMechStatusChanged(device, status, intention)
                ssmView?.setLock(device)
            }
        }


    }//end view created


    override fun onResume() {
        super.onResume()

        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {

                if (device.bleIdStr == ssm?.bleIdStr) {
                    ssm = device
                    ssm?.delegate = object : CHSesameBleDeviceDelegate {
                        override fun onMechStatusChanged(device: CHSesameBleInterface, status: SSM2MechStatus, intention: CHSesameIntention) {
                            super.onMechStatusChanged(device, status, intention)
                            ssmView?.setLock(device)
                        }
                    }
                    ssm?.connnect()
                }

            }
        }
    }
}

