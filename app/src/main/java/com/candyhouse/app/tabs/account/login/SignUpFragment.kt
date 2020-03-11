package com.candyhouse.app.tabs.account.login

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult
import com.candyhouse.R
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.pBar
import java.lang.Exception


class SignUpFragment : DialogFragment() {

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
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pBar.visibility = View.GONE
        last_name_edt.setText("雅坦")
        first_name_edt.setText("尼斯")
        mail_edt.setText("tse+20@candyhouse.co")
        password_edt.setText("123456")
        backicon.setOnClickListener {
            dismiss()
        }
        checkBtn.setOnClickListener {
            val isLastnameEpt = last_name_edt.text.isEmpty()
            val isfirstNameEpt = first_name_edt.text.isEmpty()
            val isMailEpt = mail_edt.text.isEmpty()
            val isPasswordEpt = password_edt.text.isEmpty()
            if (isLastnameEpt or isfirstNameEpt or isMailEpt or isPasswordEpt) {
                return@setOnClickListener
            }

            Thread(Runnable {
                val awsAttributes = CognitoUserAttributes()
                awsAttributes.addAttribute("email", mail_edt.text.toString())
                awsAttributes.addAttribute("family_name", last_name_edt.text.toString())
                awsAttributes.addAttribute("given_name", first_name_edt.text.toString())
                AWSCognitoOAuthService.userPool.signUp(mail_edt.text.toString(), password_edt.text.toString(), awsAttributes, mapOf(), object : SignUpHandler {
                    override fun onSuccess(user: CognitoUser?, signUpResult: SignUpResult?) {
                        L.d("hcia", "user:" + user)
                        L.d("hcia", "signUpResult:" + signUpResult)

                        activity?.runOnUiThread {
                            second_zone.visibility = View.VISIBLE
                            Toast.makeText(
                                    activity,
                                    signUpResult?.codeDeliveryDetails?.destination,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(exception: Exception?) {

                        activity?.runOnUiThread {
                            Toast.makeText(
                                    activity,
                                    exception?.localizedMessage,
                                    Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                })
            }).start()

            last_name_edt.text


        }
        confirmBtn.setOnClickListener {
            if (last_name_edt.text.isEmpty()) {
                return@setOnClickListener
            }
            AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString()).confirmSignUpInBackground(confirm_edt.text.toString(), true, object : GenericHandler {
                override fun onSuccess() {
                    L.d("hcia", "onSuccess:")
                    dismiss()
                }

                override fun onFailure(exception: Exception?) {
                    L.d("hcia", "exception:" + exception)
                }

            })
        }
        resendBtn.setOnClickListener {
            Thread(Runnable {
                AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString()).resendConfirmationCodeInBackground(object :VerificationHandler{
                    override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                    activity,
                                    verificationCodeDeliveryMedium?.destination,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                        L.d("hcia", "verificationCodeDeliveryMedium:" + verificationCodeDeliveryMedium?.destination)
                    }

                    override fun onFailure(exception: Exception?) {
                        L.d("hcia", "exception:" + exception)
                    }
                })
            }).start()
        }

    }

    override fun onDestroyView() {
        L.d("hcia", "LoginFragment.fg?.nameEdt:" + LoginFragment.fg?.nameEdt)
        L.d("hcia", "LoginFragment.fg?.passwordEdt?:" + LoginFragment.fg?.passwordEdt)
        L.d("hcia", "mail_edt.text:" + mail_edt.text)
        L.d("hcia", "passwordEdt.text:" + password_edt.text)
        LoginFragment.fg?.nameEdt?.setText(mail_edt.text)
        LoginFragment.fg?.passwordEdt?.setText(password_edt.text)

        super.onDestroyView()
        isShowing = false


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


    companion object {
        var isShowing = false

        @JvmStatic
        fun newInstance() =
                SignUpFragment()
    }
}
