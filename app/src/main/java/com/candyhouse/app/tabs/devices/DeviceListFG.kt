package com.candyhouse.app.tabs.devices

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.candyhouse.BuildConfig
import com.candyhouse.R
import com.candyhouse.app.base.BaseFG
import com.candyhouse.app.base.BaseSSMFG
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.devices.ssm2.setting.angle.SSMCellView
import com.candyhouse.app.tabs.devices.ssm2.test.BlueSesameControlActivity
import com.candyhouse.app.tabs.menu.BarMenuItem
import com.candyhouse.app.tabs.menu.CustomAdapter
import com.candyhouse.app.tabs.menu.ItemUtils
import com.candyhouse.sesame.ble.*
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.deviceprotocol.CHBatteryStatus
import com.candyhouse.sesame.server.CHAccountManager
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
                when (customItem.index) {
                    0 -> {
                        findNavController().navigate(R.id.to_scan)
                    }
                    1 -> {
                        findNavController().navigate(R.id.to_regist)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.nowTab == 0) {
            (activity as MainActivity).showMenu()
        }

        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverSesame(device: CHSesameBleInterface) {
                if (!device.isRegistered) {
                    return
                }
                if (device.accessLevel == CHDeviceAccessLevel.unknown) {
                    return
                }
                deviceMap.put(device.bleIdStr, device)
                refleshTable()
            }
        }
        recyclerView.isEmpty()
//        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
                .setTextSize(12f)
                .setCornerRadius(4f)
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .setBackgroundColorResource(R.color.menu_bg)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenClicked(true)
                .setOnBalloonClickListener(object : OnBalloonClickListener {
                    override fun onBalloonClick(view: View) {
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
                    var ly: LinearLayoutManager = object : LinearLayoutManager(context) {
                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                    customAdapter.addCustomItem(ItemUtils.getCustomSamples(context!!))
                    layoutManager = ly
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
                        var deviceId: TextView = view.findViewById(R.id.deviceId)

                        @SuppressLint("SetTextI18n")
                        override fun bind(data: Pair<String, CHSesameBleInterface>, pos: Int) {
                            val sesame = data.second
                            sesame.delegate = object : CHSesameBleDeviceDelegate {
                                override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
//                                    L.d("hcia", device.customNickname+"device.chDeviceStatus:" + device.chDeviceStatus)
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

                            deviceId.text = sesame.deviceId?.toString()
                            deviceId.isClickable = true
                            deviceId.setOnClickListener {
                                val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("", (it as TextView).text))

                                Toast.makeText(it.context,"クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                            }

                            toggle.setOnClickListener { sesame.toggle() }
                            toggle.setBackgroundResource(ssmUIParcer(sesame))
                            testICon.visibility = if (testSwich.isChecked) View.VISIBLE else View.GONE
                            testICon.setOnClickListener {
                                BlueSesameControlActivity.ssm = sesame
                                view.context.startActivity(Intent(view.context, BlueSesameControlActivity().javaClass))
                            }
                            view.setOnClickListener {
                                BaseSSMFG.mSesame = sesame
                                findNavController().navigate(R.id.action_deviceListPG_to_mainRoomFG)
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

        if (BuildConfig.BUILD_TYPE == "debug") {
            testSwich.isChecked = true
            testSwich.visibility = View.VISIBLE
        } else {
            testSwich.isChecked = false
            testSwich.visibility = View.INVISIBLE
        }

        CHBleManager.discoverALLDevices() {
            deviceMap.clear()
            it.forEach {
                deviceMap.put(it.bleIdStr, it)
            }

            refleshTable()
        }
    }

    private fun refleshTable() {
        recyclerView?.post {
            mDeviceList.clear()
            mDeviceList.addAll(deviceMap.toList().sortedByDescending { it.second.customNickname })
            (recyclerView.adapter as GenericAdapter<*>).notifyDataSetChanged()
        }
    }

    fun refleshPage() {
        runOnUiThread {
            swiperefreshView.isRefreshing = true
        }
        CHAccountManager.flushDevices { result ->
            result.onSuccess {
                CHBleManager.discoverALLDevices() {
                    deviceMap.clear()
                    it.forEach {
                        deviceMap.put(it.bleIdStr, it)
                    }


                    refleshTable()

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
        CHDeviceStatus.reset -> R.drawable.icon_nosignal
    }
}

fun ssmBatteryParcer(device: CHSesameBleInterface): Int {
    return when (device.mechStatus?.getBatteryStatus()) {
        CHBatteryStatus.healthy -> R.drawable.bt100
        CHBatteryStatus.low -> R.drawable.bt50
        else -> R.drawable.bt0
    }
}