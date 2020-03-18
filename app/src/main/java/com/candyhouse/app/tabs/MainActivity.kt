/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.candyhouse.app.tabs

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.candyhouse.R
import com.candyhouse.app.tabs.account.login.LoginFragment
import com.candyhouse.app.tabs.friends.FriendsFG
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.server.identityProviderCognito
import com.candyhouse.sesame.ble.CHBleManager
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.CHLoginProvider
import com.candyhouse.sesame.server.CHOauthToken
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var currentNavController: LiveData<NavController>

    companion object {
        var activity: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

        }
//        L.d("hcia", "主ACtivity啟動:")
        initView()


//        if (isDrawOverlaysAllowed()) {
//            startService(Intent(this@MainActivity, FloatingWidgetService::class.java))
////            startService(Intent(this@MainActivity, ShowHudService::class.java))
//            return
//        }else{
//            requestForDrawingOverAppsPermission()
//        }

        getPermissions()

    }

    private fun isDrawOverlaysAllowed(): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    private fun requestForDrawingOverAppsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, 666)
    }

    override fun onResume() {
        super.onResume()
        if (AWSCognitoOAuthService.userPool.currentUser.userId == null) {
            LoginFragment.newInstance().show(supportFragmentManager, "")
        } else {
            checkLogin()
        }
    }

    private fun checkLogin() {
        AWSCognitoOAuthService.userPool.currentUser?.getSessionInBackground(object : AuthenticationHandler {

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
            }

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            }

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                AWSCognitoOAuthService.userToken = userSession!!.idToken.jwtToken
                CHAccountManager.setupLoginSession(object : CHLoginProvider {
                    override fun oauthToken(): CHOauthToken {
                        return CHOauthToken(identityProviderCognito, userSession.idToken.jwtToken)
                    }
                })
                refreshFriend()
            }

            override fun onFailure(exception: Exception) { // Sign-in failed, check exception for the cause
                when (exception) {
                    is NullPointerException -> {
                        LoginFragment.newInstance().show(supportFragmentManager, "")
                    }
                    is CognitoInternalErrorException -> {
                        L.d("hcia", "CognitoInternalErrorException:" )
                        checkLogin()
                    }
                    else -> {
                        L.d("hcia", "exception:" + exception)
                        Toast.makeText(
                                applicationContext,
                                exception.message,
                                Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        })
    }


    private fun initView() {
        right_icon.setOnClickListener {
            L.d("hcia", "currentNavController?.value?:" + currentNavController?.value)
        }
        toggleAll.setOnClickListener {
            //            startActivity(Intent(this, ScanVC().javaClass))
//            CHBleManager.toggleAll()
        }
        setupBottomNavigationBar()

    }


    fun refreshFriend() {
        val firstTab = supportFragmentManager.findFragmentByTag("bottomNavigation#1") as NavHostFragment
        val fragment = firstTab.childFragmentManager.fragments.get(0) as FriendsFG
        fragment.refleshPage()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
                R.navigation.list,
                R.navigation.home,
                R.navigation.account_ng
        )

        val controller = bottom_nav.setupWithNavController(//BottomNavigationView
                navGraphIds = navGraphIds,
                fragmentManager = supportFragmentManager,
                containerId = R.id.nav_host_container,
                intent = intent
        )


        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            currentNavController = controller
        })
        currentNavController = controller

    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(
                applicationContext,
                "ACCESS_FINE_LOCATION(bluetooth  nead)",
                Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        L.d("hcia", "onPermissionsGranted CHBleManager:" + CHBleManager)
        L.d("hcia", "UI 開動掃描 ＡＡＡ:")
        CHBleManager.enableScan()
    }

    fun hideMenu() {
        bottom_nav.visibility = View.GONE
    }

    fun showMenu() {
        bottom_nav.visibility = View.VISIBLE
    }

    private fun getPermissions() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            L.d("hcia", "UI 開動掃描 ＢＢＢ:")
            CHBleManager.enableScan()
        } else {
            EasyPermissions.requestPermissions(
                    this, "ACCESS_FINE_LOCATION(bluetooth  nead)", 0,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun showProgress() {
        pBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        pBar.visibility = View.GONE
    }
}
