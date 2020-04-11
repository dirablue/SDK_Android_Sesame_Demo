package com.candyhouse.app

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.devices.ssm2.room.getTZ
import com.candyhouse.app.tabs.devices.ssm2.room.showTZ
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.ble.CHBleManagerDelegate
import com.candyhouse.sesame.ble.CHDeviceStatus
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.ble.Sesame2.CHSesameBleDeviceDelegate
import com.candyhouse.sesame.utils.*
import com.candyhouse.utils.L
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalUnsignedTypes
class MyApp : Application() {
    companion object {
        var ctx: Context? = null
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        L.d("hcia", "🌱:")
        CHBleManager.appContext = this
        ctx = this
    }
}
