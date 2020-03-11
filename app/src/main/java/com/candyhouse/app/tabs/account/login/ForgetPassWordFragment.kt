package com.candyhouse.app.tabs.account.login

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.candyhouse.R
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.fragment_fg_pass.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.pBar
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.backicon
import kotlinx.android.synthetic.main.fragment_signup.checkBtn
import kotlinx.android.synthetic.main.fragment_signup.confirmBtn
import kotlinx.android.synthetic.main.fragment_signup.confirm_edt
import kotlinx.android.synthetic.main.fragment_signup.mail_edt
import java.lang.Exception


class ForgetPassWordFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, R.style.AppTheme_FlatDialog)
        dialog.window.attributes.windowAnimations = R.style.DialogAnimation
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fg_pass, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pBar.visibility = View.GONE

        mail_edt.setText("tse+20@candyhouse.co")
        new_password_edt.setText("222222")
        backicon.setOnClickListener {
            dismiss()
        }
        confirmBtn.setOnClickListener {

            if (confirm_edt.text.isEmpty()) {
                return@setOnClickListener
            }

            Thread(Runnable {
                AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString()).confirmPassword(confirm_edt.text.toString(), new_password_edt.text.toString(), object : ForgotPasswordHandler {
                    override fun onSuccess() {
                        L.d("hcia", "onSuccess:")
                    }

                    override fun onFailure(exception: Exception?) {
                        L.d("hcia", "exception:" + exception)
                    }

                    override fun getResetCode(continuation: ForgotPasswordContinuation?) {
                        L.d("hcia", "continuation:" + continuation)
                    }

                })
            }).start()

        }
        checkBtn.setOnClickListener {
            if (mail_edt.text.isEmpty()) {
                return@setOnClickListener
            }
            AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString()).forgotPasswordInBackground(object : ForgotPasswordHandler {
                override fun onSuccess() {
                    L.d("hcia", "onSuccess:")

                    activity?.runOnUiThread {
                        second_zone_p.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(exception: Exception?) {
                    L.d("hcia", "exception:" + exception)
                }

                override fun getResetCode(continuation: ForgotPasswordContinuation?) {
                    L.d("hcia", "continuation:" + continuation)

                    Toast.makeText(
                            activity,
                            getString(R.string.re_send_verification_code),
                            Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        L.d("hcia", "onDestroy:")
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isShowing) {
            return
        }
        super.show(manager, tag)
        isShowing = true
    }

    override fun onDestroyView() {
        L.d("hcia", "mail_edt.text:" + mail_edt.text)
        L.d("hcia", "passwordEdt.text:" + new_password_edt.text)
        LoginFragment.fg?.nameEdt?.setText(mail_edt.text)
        LoginFragment.fg?.passwordEdt?.setText(new_password_edt.text)

        super.onDestroyView()
        isShowing = false


    }
    companion object {
        var isShowing = false

        @JvmStatic
        fun newInstance() =
                ForgetPassWordFragment()
    }
}
