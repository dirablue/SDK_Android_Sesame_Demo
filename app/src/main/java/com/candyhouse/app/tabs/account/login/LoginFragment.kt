package com.candyhouse.app.tabs.account.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.devices.DeviceListFG
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.fragment_login.*


//todo
//  forgetpassword
class LoginFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fg = this
    }

    override fun onDestroy() {
        fg = null
        super.onDestroy()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        view.setFocusableInTouchMode(true)
        view.requestFocus()

        view.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return true
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pBar.visibility = View.GONE
        sign_up.setOnClickListener {
            SignUpFragment.newInstance().show(activity!!.supportFragmentManager, "")
        }
        forget_password.setOnClickListener {
            ForgetPassWordFragment.newInstance().show(activity!!.supportFragmentManager, "")
        }
        val mLoginCallback: AWSCognitoOAuthService.LoginResult =
                object : AWSCognitoOAuthService.LoginResult {
                    override fun onSuccess(v: String?) {
                        CHAccountManager.setupLoginSession(AWSCognitoOAuthService)
                        DeviceListFG.instance?.refleshPage()
                        (activity as MainActivity).refreshFriend()
                        this@LoginFragment.dismiss()
                    }

                    override fun onError(exception: Exception) {
                        Toast.makeText(
                                activity,
                                exception.localizedMessage,
                                Toast.LENGTH_LONG
                        ).show()
                        pBar.visibility = View.GONE

                    }
                }
        loginBtn.setOnClickListener {
            if (pBar.isShown) {
                return@setOnClickListener
            }
            pBar.visibility = View.VISIBLE
            AWSCognitoOAuthService.loginWithUsernamePassword(
                    nameEdt.text.toString().toMail(),
                    passwordEdt.text.toString(),
                    mLoginCallback
            )
        }
        tsetest.setOnClickListener {
            if (pBar.isShown) {
                return@setOnClickListener
            }
            pBar.visibility = View.VISIBLE
            AWSCognitoOAuthService.loginWithUsernamePassword(
                    "tse@candyhouse.co".toMail(),
                    "111111",
                    mLoginCallback
            )
        }
        jmingtest.setOnClickListener {
            if (pBar.isShown) {
                return@setOnClickListener
            }
            pBar.visibility = View.VISIBLE
            AWSCognitoOAuthService.loginWithUsernamePassword(
                    "@candyhouse.co",
                    "",
                    mLoginCallback
            )
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isShowing) {
            return
        }
        super.show(manager, tag)
        isShowing = true
    }

    override fun dismiss() {
        super.dismiss()
        isShowing = false
    }

    companion object {
        var isShowing = false
        var fg: LoginFragment? = null

        @JvmStatic
        fun newInstance() =
                LoginFragment()
    }
}


fun String.toMail(): String {
//    L.d("hcia", "toItemCode:" + this)
    val ss = this.toLowerCase().replace(" ", "").replace(" ", "").replace("\n", "")
    return ss
}
