package com.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.candyhouse.R
import com.candyhouse.app.base.BaseFG
import com.candyhouse.app.tabs.devices.ssm2.room.MainRoomFG
import com.candyhouse.app.tabs.devices.ssm2.setting.angle.SSMCellView
import com.candyhouse.app.tabs.devices.ssm2.setting.angle.SesameView
import com.candyhouse.app.tabs.devices.ssm2.test.BlueSesameControlActivity
import com.candyhouse.app.tabs.menu.BarMenuItem
import com.candyhouse.app.tabs.menu.CustomAdapter
import com.candyhouse.app.tabs.menu.ItemUtils
import com.candyhouse.sesame.BuildConfig
import com.candyhouse.sesame.ble.*
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.CHBatteryStatus
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.utils.L
import com.kasturi.admin.genericadapter.GenericAdapter
import com.skydoves.balloon.*
import java.util.*


@ExperimentalUnsignedTypes
class DeviceListFG : BaseFG() {
    companion object {
        var instance: DeviceListFG? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    val deviceMap: MutableMap<String, CHSesameBleInterface> = mutableMapOf()
    var mDeviceList = ArrayList<Pair<String, CHSesameBleInterface>>()
    lateinit var testSwich: Switch
    private lateinit var customListBalloon: Balloon
    private lateinit var recyclerView: RecyclerView
    private lateinit var swiperefreshView: SwipeRefreshLayout
    private val customAdapter by lazy {
        CustomAdapter(object : CustomAdapter.CustomViewHolder.Delegate {
            override fun onCustomItemClick(customItem: BarMenuItem) {
                customListBalloon?.dismiss()
                when (customItem.title) {
                    "Add Friend" -> {
                        findNavController().navigate(R.id.action_deviceFG_to_scanFG)
                    }
                    "New Sesame" -> {
                        findNavController().navigate(R.id.to_regist)
                    }
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()

        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (!device.isRegistered) {
                    return
                }
                if (device.accessLevel == CHDeviceAccessLevel.unknown) {
                    return
                }
                runOnUiThread {
                    deviceMap.put(device.bleIdStr, device)
                    mDeviceList.clear()
                    mDeviceList.addAll(deviceMap.toList())
                    (recyclerView.adapter as GenericAdapter<*>).notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        L.d("hcia", "鎖列表 ")
        val view = inflater.inflate(R.layout.fg_devicelist, container, false)
        val menuBtn = view.findViewById<View>(R.id.right_icon).apply {
            setOnClickListener {
                customListBalloon.showAlignBottom(it)
            }
        }

        customListBalloon = Balloon.Builder(menuBtn.context)
                .setLayout(R.layout.layout_custom_list)
                .setArrowSize(12)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.85f)
                .setWidth(200)
                .setHeight(120)
                .setTextSize(12f)
                .setCornerRadius(4f)
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .setBackgroundColorResource(R.color.menu_bg)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenClicked(true)
                .setOnBalloonClickListener(object : OnBalloonClickListener {
                    override fun onBalloonClick(view: View) {
//                        L.d("hcia", "onBalloonClick:")
                    }
                })
                .setDismissWhenClicked(true)
                .setOnBalloonOutsideTouchListener(object : OnBalloonOutsideTouchListener {
                    override fun onBalloonOutsideTouch(view: View, event: MotionEvent) {
                        menuBtn.isClickable = false
                        customListBalloon?.dismiss()
                        menuBtn.postDelayed({
                            menuBtn.isClickable = true
                        }, 300)
                    }
                })
                .build()

        customListBalloon.getContentView().findViewById<RecyclerView>(R.id.list_recyclerView)
                .apply {
                    setHasFixedSize(true)
                    adapter = customAdapter
                    customAdapter.addCustomItem(ItemUtils.getCustomSamples(context!!))
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                }

        swiperefreshView = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh).apply {
            setOnRefreshListener {
                refleshPage()
            }
        }
        recyclerView = view.findViewById<RecyclerView>(R.id.leaderboard_list).apply {
            setHasFixedSize(true)
            adapter = object : GenericAdapter<Any>(mDeviceList) {
                override fun getLayoutId(position: Int, obj: Any): Int {
                    return R.layout.sesame_layout
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view), Binder<Pair<String, CHSesameBleInterface>> {

                        var ssmView: SSMCellView = view.findViewById(R.id.ssmView)
                        var toggle: Button = view.findViewById(R.id.toggle)
                        var customName: TextView = view.findViewById(R.id.title)
                        var ownerName: TextView = view.findViewById(R.id.sub_title)
                        var testICon: View = view.findViewById(R.id.test)
                        var battery_percent: TextView = view.findViewById(R.id.battery_percent)
                        var battery: ImageView = view.findViewById(R.id.battery)


                        @SuppressLint("SetTextI18n")
                        override fun bind(data: Pair<String, CHSesameBleInterface>, pos: Int) {
                            val sesame = data.second
                            sesame.delegate = object : CHSesameBleDeviceDelegate {
                                override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
                                    toggle.setBackgroundResource(ssmUIParcer(device))
                                    battery.setBackgroundResource(ssmBatteryParcer(device))
                                    battery_percent.text = sesame.mechStatus?.batteryPrecentage().toString() + "%"
                                    battery_percent.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE
                                    battery.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE

                                    ssmView.setLock(device)
                                }
                            }
                            sesame.connnect()
                            ssmView.setLock(sesame)
                            ssmView.setOnClickListener {
                                sesame.toggle()
                            }

                            battery.setBackgroundResource(ssmBatteryParcer(sesame))
                            battery_percent.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE
                            battery_percent.text = sesame.mechStatus?.batteryPrecentage().toString() + "%"

                            battery.visibility = if (sesame.mechStatus == null) View.GONE else View.VISIBLE
                            customName.text = sesame.customNickname
                            ownerName.text = sesame.ownName
                            ownerName.visibility = if (sesame.ownName == sesame.customNickname) View.GONE else View.VISIBLE
                            toggle.setOnClickListener { sesame.toggle() }
                            toggle.setBackgroundResource(ssmUIParcer(sesame))
                            testICon.visibility = if (testSwich.isChecked) View.VISIBLE else View.GONE
                            testICon.setOnClickListener {
                                BlueSesameControlActivity.ssm = sesame
                                view.context.startActivity(Intent(view.context, BlueSesameControlActivity().javaClass))
                            }
                            battery_percent.text = sesame.mechStatus?.battery.toString()
                            view.setOnClickListener {

                                MainRoomFG.ssm = sesame
                                findNavController().navigate(R.id.action_deviceListPG_to_mainRoomFG)

//                                SSM2SetAngleFG.ssm = sesame
//                                findNavController().navigate(R.id.action_deviceListPG_to_SSM2SetAngleFG)

                            }



                        }
                    }
                }
            }

        }
        testSwich = view.findViewById<Switch>(R.id.testSwich).apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testSwich.isChecked = true
        if(BuildConfig.BUILD_TYPE == "debug"){
            testSwich.visibility = View.VISIBLE
        }

            CHBleManager.discoverALLDevices() {
            deviceMap.clear()
            mDeviceList.clear()
            it.forEach {
                deviceMap.put(it.bleIdStr, it)
            }
            mDeviceList.addAll(deviceMap.toList())
            runOnUiThread {
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    //todo ui sync fail
    fun refleshPage() {
        runOnUiThread {
            swiperefreshView.isRefreshing = true
        }
        CHAccountManager.flushDevices() { result ->
            result.onSuccess {
                L.d("hcia", "UI it:" + it)
                CHBleManager.discoverALLDevices() {
                    L.d("hcia", "UI it:" + it)
                    deviceMap.clear()
                    mDeviceList.clear()
                    it.forEach {
                        deviceMap.put(it.bleIdStr, it)
                    }
                    mDeviceList.addAll(deviceMap.toList())
                    runOnUiThread {
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                runOnUiThread {
                    swiperefreshView.isRefreshing = false
                }
            }
        }


    }
}

fun ssmUIParcer(device: CHSesameBleInterface): Int {
    return when (device.chDeviceStatus) {
        CHDeviceStatus.noSignal -> R.drawable.icon_nosignal
        CHDeviceStatus.receiveBle -> R.drawable.icon_receiveblee
        CHDeviceStatus.connecting -> R.drawable.icon_logining
        CHDeviceStatus.waitgatt -> R.drawable.icon_waitgatt
        CHDeviceStatus.logining -> R.drawable.icon_logining
        CHDeviceStatus.readytoRegister -> R.drawable.icon_nosignal
        CHDeviceStatus.locked -> R.drawable.icon_lock
        CHDeviceStatus.unlocked -> R.drawable.icon_unlock
        CHDeviceStatus.nosetting -> R.drawable.icon_nosetting
        CHDeviceStatus.moved -> R.drawable.icon_unlock
    }
}

fun ssmBatteryParcer(device: CHSesameBleInterface): Int {
    return when (device.mechStatus?.getBatteryStatus()) {
        CHBatteryStatus.healthy -> R.drawable.bt100
        CHBatteryStatus.low -> R.drawable.bt50
        else -> R.drawable.bt0
    }
}