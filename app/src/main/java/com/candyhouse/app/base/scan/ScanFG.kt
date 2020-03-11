package com.candyhouse.app.base.scan

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.CHQrevent
import com.candyhouse.utils.L
import com.google.zxing.Result
import pub.devrel.easypermissions.EasyPermissions

class ScanFG : Fragment(), ZXingScannerView.ResultHandler, EasyPermissions.PermissionCallbacks {

    private var mScannerView: ZXingScannerView? = null
    lateinit var contentFrame: ViewGroup


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_simple_scanner, container, false)

        contentFrame = view.findViewById(R.id.content_frame) as ViewGroup
        mScannerView = ZXingScannerView(context)
        mScannerView!!.setResultHandler(this)
        contentFrame.addView(mScannerView)

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermissions()
    }


    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideMenu()

        L.d("hcia", "mScannerView:" + mScannerView)
        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)) {
            mScannerView!!.startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showMenu()

        L.d("hcia", "mScannerView:" + mScannerView)
        mScannerView!!.stopCamera()
    }


    override fun handleResult(rawResult: Result?) {

        Toast.makeText(context, "Contents = " + rawResult?.text +
                ", Format = " + rawResult?.barcodeFormat.toString(), Toast.LENGTH_SHORT).show()


        CHAccountManager.receiveQRCode(rawResult?.text) {
            it.onSuccess {

                when(it){
                    CHQrevent.addFriendFromACcount -> {
                        (MainActivity.activity)?.refreshFriend()
                    }
                }
                L.d("hcia", "it:" + it)

            }
            it.onFailure {
                L.d("hcia", "it:" + it)
            }
        }
        findNavController().navigateUp()

//        Handler().postDelayed({ mScannerView!!.resumeCameraPreview(this@ScanVC) }, 2000)
    }

    private fun getPermissions() {

        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)) {
//            initScanner()
            L.d("hcia", "已經有權限 mScannerView:" + mScannerView)

//            Handler().postDelayed({ mScannerView!!.resumeCameraPreview(this@ScanVC) }, 2000)
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
//        mScannerView!!.startCamera()
        L.d("hcia", "權限是可以得 mScannerView:" + mScannerView)

    }

}
