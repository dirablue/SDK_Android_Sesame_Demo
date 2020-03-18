package com.candyhouse.app

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
import com.candyhouse.utils.L
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


@ExperimentalUnsignedTypes
class MyApp : Application(), CHBleManagerDelegate, CHSesameBleDeviceDelegate {

    companion object {
        var ctx: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        L.d("hcia", "🌱:")

        CHBleManager.appContext = this
        CHBleManager.delegate = this
        ctx = this

//        ForegroundService.startService(this, "sesame")
    }

    //todo Widget
    fun showWidget(device: CHSesameBleInterface) {
        val channelID = "channel_id"
        val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val remoteViews = RemoteViews(packageName, R.layout.widget_layout)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelID, "name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent()
        intent.setAction("toggle")
        intent.putExtra("HELLO", device.bleIdStr)

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, device.bleIdStr.hashCode(), intent, 0)
        remoteViews.setOnClickPendingIntent(R.id.toggle, pendingIntent)
        remoteViews.setTextViewText(R.id.toggle, "toggle")
        remoteViews.setTextViewText(R.id.title, device.customNickname)
        remoteViews.setTextViewText(R.id.connectStatus, device.chDeviceStatus.toString())
        val builder = NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.avatar_2_raster)
                .setContent(remoteViews)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SOCIAL)
//                .setTicker(device.customNickname)
//                .setPriority(NotificationCompat)
//                .setVisibility(Notification.)


        val notification: Notification = builder.build()


        notificationManager.notify(device.bleIdStr.hashCode(), notification)

    }

    override fun didDiscoverSesame(device: CHSesameBleInterface) {

        if (device.isRegistered) {
//            L.d("hcia", "APP拿到設備拉:" + device.customNickname + " isRegistered:" + device.isRegistered)
//            showWidget(device)
            device.delegate = this
        } else {

        }

    }

    override fun onBleDeviceStatusChanged(device: CHSesameBleInterface, status: CHDeviceStatus) {
//        showWidget(device)
    }


}


class ForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundService Kotlin"

    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)

        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sesame is working")
                .setSmallIcon(R.drawable.avatar_4_raster)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)


//        CHBleManager.disableScan()
//        hideWidget(this)
//        stopService(this)

        L.d("hcia", "rootIntent:" + rootIntent)
    }

    private fun hideWidget(context: Context) {
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

fun InputStream.toFile(path: String) {
    use { input ->
        File(path).outputStream().use { input.copyTo(it) }
    }
}