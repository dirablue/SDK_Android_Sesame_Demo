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
import com.candyhouse.app.base.BaseSSMFG
import com.candyhouse.sesame.ble.*
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.CHSesameIntention
import com.candyhouse.sesame.deviceprotocol.CHSesameLockPositionConfiguration
import com.candyhouse.sesame.deviceprotocol.SSM2MechStatus
import com.kasturi.admin.genericadapter.GenericAdapter
import kotlinx.android.synthetic.main.fg_set_angle.*


@ExperimentalUnsignedTypes
class SSM2SetAngleFG : BaseSSMFG() {
    var titleTextView: TextView? = null
    var ssmView: SesameView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_set_angle, container, false)
        titleTextView = view.findViewById(R.id.titlec)
        ssmView = view.findViewById(R.id.ssmView)
        return view
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView?.text = mSesame?.customNickname
        backicon.setOnClickListener {
            findNavController().navigateUp()
        }
        ssmView?.setLock(mSesame!!)
        ssmView?.setOnClickListener { mSesame?.toggle() }

        setunlock_zone?.setOnClickListener {
            if (mSesame?.chDeviceStatus?.value == CHDeviceLoginStatus.unlogined) {
                return@setOnClickListener
            }
            mSesame?.configureLockPosition(CHSesameLockPositionConfiguration(mSesame?.mSSM2MechSetting!!.lockPosition, mSesame?.mechStatus!!.position))
        }
        setlock_zone?.setOnClickListener {
            if (mSesame?.chDeviceStatus?.value == CHDeviceLoginStatus.unlogined) {
                return@setOnClickListener
            }
            mSesame?.configureLockPosition(CHSesameLockPositionConfiguration(mSesame?.mechStatus!!.position, mSesame?.mSSM2MechSetting!!.unlockPosition))
        }

        mSesame?.delegate = object : CHSesameBleDeviceDelegate {
            override fun onMechStatusChanged(device: CHSesameBleInterface, status: SSM2MechStatus, intention: CHSesameIntention) {
                super.onMechStatusChanged(device, status, intention)
                ssmView?.setLock(device)
            }
        }


    }//end view created


    override fun onResume() {
        super.onResume()

//        CHBleManager.delegate = object : CHBleManagerDelegate {
//            override fun didDiscoverSesame(device: CHSesameBleInterface) {
//
//                if (device.bleIdStr == mSesame?.bleIdStr) {
//                    mSesame = device
//                    mSesame?.delegate = object : CHSesameBleDeviceDelegate {
//                        override fun onMechStatusChanged(device: CHSesameBleInterface, status: SSM2MechStatus, intention: CHSesameIntention) {
//                            super.onMechStatusChanged(device, status, intention)
//                            ssmView?.setLock(device)
//                        }
//                    }
//                    mSesame?.connnect()
//                }
//
//            }
//        }
    }
}

