package com.candyhouse.app.base.scan

import android.Manifest
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.bingoogolapple.qrcode.core.QRCodeView
import cn.bingoogolapple.qrcode.zxing.ZXingView
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.friends.FriendsFG
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.CHQrevent
import com.candyhouse.utils.L
import pub.devrel.easypermissions.EasyPermissions


interface ScanCallBack {
    fun onScanFriendSuccess(friendID: String)
}

class ScanFG : Fragment(), QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    companion object {
        var callBack: ScanCallBack? = null
    }

    private var originalColor: Int? = null
    lateinit var mZXingView: ZXingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_simple_scanner, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backIcon = view.findViewById(R.id.backicon) as View
        mZXingView = view.findViewById(R.id.zxingview)
        backIcon.setOnClickListener {
            findNavController().navigateUp()
        }
        mZXingView.setDelegate(this)
    }


    override fun onResume() {
        super.onResume()
        originalColor = MainActivity.activity?.getWindow()?.statusBarColor
        MainActivity.activity?.getWindow()?.statusBarColor = resources.getColor(R.color.black)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        (activity as MainActivity).hideMenu()

        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)) {
            mZXingView.startCamera()
            mZXingView.startSpotAndShowRect()
//            mZXingView.postDelayed({
//                mZXingView.visibility = View.VISIBLE
//            },1000)
        }
    }

    override fun onPause() {
        super.onPause()

        MainActivity.activity?.getWindow()?.statusBarColor = originalColor!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            window.statusBarColor = Color.WHITE
        }
        mZXingView.stopCamera()
    }

    override fun onDestroy() {
        mZXingView.onDestroy()
        super.onDestroy()
    }


    private fun getPermissions() {

        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)) {
        } else {
            EasyPermissions.requestPermissions(
                    this, "CAMERA", 0,
                    Manifest.permission.CAMERA
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(context, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onScanQRCodeSuccess(result: String?) {
        CHAccountManager.receiveQRCode(result) {
            it.onSuccess {
                when (it.first) {
                    CHQrevent.addFriendFromAccount -> {
                        callBack?.onScanFriendSuccess(it.second)
                        FriendsFG.instance?.refleshPage()
                    }
                    CHQrevent.getKeyFromOwner -> {
                        //todo

                    }
                }
            }
            it.onFailure {
            }
        }
        findNavController().navigateUp()
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
    }

    override fun onScanQRCodeOpenCameraError() {
    }
}
