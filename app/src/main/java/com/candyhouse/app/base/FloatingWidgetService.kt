package com.candyhouse.app.base

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.candyhouse.R
import com.candyhouse.app.ForegroundService
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleStatisDelegate
import com.candyhouse.sesame.ble.CHScanStatus
import com.candyhouse.sesame.utils.runOnUiThread
import com.candyhouse.utils.L

@ExperimentalUnsignedTypes
class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    lateinit var floatingWidgetView: FloatingWidgetView
    lateinit var tee: TextView
    lateinit var vee: ImageView
    lateinit var bleOP: ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        CHBleManager.statusDelegate = object : CHBleStatisDelegate {

            override fun didScanChange(ss: CHScanStatus) {

                L.d("hcia", "CHScanStatus:" + ss.toString() + " " + ss.value.toString())
                runOnUiThread(Runnable {
                    tee.text = "" + ss
                    vee.setImageResource(if (BluetoothAdapter.getDefaultAdapter().isEnabled) R.drawable.icon_lock else R.drawable.icon_unlock)
                })
            }

        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingWidgetView = FloatingWidgetView(this)
        tee = floatingWidgetView.findViewById<TextView>(R.id.float_status)
        vee = floatingWidgetView.findViewById<ImageView>(R.id.floatingIcon)
        bleOP = floatingWidgetView.findViewById<ImageView>(R.id.bleOP)
        tee.text = "" + CHBleManager.mScanning
        vee.setImageResource(if (BluetoothAdapter.getDefaultAdapter().isEnabled) R.drawable.icon_lock else R.drawable.icon_unlock)

        vee.setOnClickListener {
            L.d("hcia", "我點到圖片拉:" + vee)
            when (CHBleManager.mScanning) {
                CHScanStatus.enable -> {
                    CHBleManager.disableScan()
                }
                CHScanStatus.disable -> {
                    CHBleManager.enableScan()
                }
                CHScanStatus.bleclose -> {
                    BluetoothAdapter.getDefaultAdapter().enable()
                }
            }
        }

        bleOP.setOnClickListener {
            L.d("hcia", "我點到圖片拉:" + vee)
            if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
                BluetoothAdapter.getDefaultAdapter().disable()
            }else{
                BluetoothAdapter.getDefaultAdapter().enable()
            }
        }

        vee.setOnLongClickListener {
            Toast.makeText(this, "close scanner", Toast.LENGTH_SHORT).show()
            stopSelf()
            return@setOnLongClickListener true
        }
        ForegroundService.startService(this, "sesame")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::windowManager.isInitialized) windowManager.removeView(floatingWidgetView)
    }

}

class FloatingWidgetView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
    )

    private var x: Int = 0
    private var y: Int = 0
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var clickStartTimer: Long = 0
    private val windowManager: WindowManager

    init {
        View.inflate(context, R.layout.floating_widget_layout, this)

        layoutParams.x = x
        layoutParams.y = y

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, layoutParams)
    }

    companion object {
        private const val CLICK_DELTA = 200
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        L.d("hcia", "onInterceptTouchEvent:" + event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                clickStartTimer = System.currentTimeMillis()

                x = layoutParams.x
                y = layoutParams.y

                touchX = event.rawX
                touchY = event.rawY
//                return super.onTouchEvent(event)

            }

            MotionEvent.ACTION_MOVE -> {
                requestDisallowInterceptTouchEvent(true)
                layoutParams.x = (x + event.rawX - touchX).toInt()
                layoutParams.y = (y + event.rawY - touchY).toInt()
                windowManager.updateViewLayout(this, layoutParams)
//                return super.onInterceptTouchEvent(event)
                return true

            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        L.d("hcia", "onTouchEvent:" + event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                clickStartTimer = System.currentTimeMillis()

                x = layoutParams.x
                y = layoutParams.y

                touchX = event.rawX
                touchY = event.rawY
                return super.onTouchEvent(event)

            }
            MotionEvent.ACTION_MOVE -> {
                requestDisallowInterceptTouchEvent(true)
                layoutParams.x = (x + event.rawX - touchX).toInt()
                layoutParams.y = (y + event.rawY - touchY).toInt()
                windowManager.updateViewLayout(this, layoutParams)

            }
        }
        return false

    }
}




