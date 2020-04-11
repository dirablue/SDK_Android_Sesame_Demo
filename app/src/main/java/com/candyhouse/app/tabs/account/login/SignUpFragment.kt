package com.candyhouse.app.tabs.account.login

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException
import com.candyhouse.R
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.pBar
import pe.startapps.alerts.ext.Alert


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            dialog?.window?.setStatusBarColor(Color.WHITE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
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
//        last_name_edt.setText("肯")
//        first_name_edt.setText("德基")
//        mail_edt.setText("tse+26@candyhouse.co")
//        password_edt.setText("123456")
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
                awsAttributes.addAttribute("email", mail_edt.text.toString().toMail())
                awsAttributes.addAttribute("family_name", last_name_edt.text.toString())
                awsAttributes.addAttribute("given_name", first_name_edt.text.toString())
                AWSCognitoOAuthService.userPool.signUp(mail_edt.text.toString().toMail(), password_edt.text.toString(), awsAttributes, mapOf(), object : SignUpHandler {
                    override fun onSuccess(user: CognitoUser?, signUpResult: SignUpResult?) {
                        toastMSG(signUpResult?.codeDeliveryDetails?.destination)
                        activity?.runOnUiThread {
                            Alert("Code Sent", "Code Sent to " + signUpResult?.codeDeliveryDetails?.destination)?.show()
                            second_zone.visibility = View.VISIBLE
                            last_name_edt.isEnabled = false
                            first_name_edt.isEnabled = false
                            mail_edt.isEnabled = false
                            password_edt.isEnabled = false
                            checkBtn.isEnabled = false
                            checkBtn.setBackgroundResource(R.drawable.round_gray)
                        }
                    }

                    override fun onFailure(exception: Exception?) {

                        when (exception) {
                            is AmazonServiceException -> {
                                toastMSG(exception.errorMessage)
                                //An account with the given email already exists.
                                //Invalid email address format.
                                //1 validation error detected: Value at 'password' failed to satisfy constraint: Member must have length greater than or equal to 6
                                //An account with the given email already exists.
                                if (exception is UsernameExistsException) {
                                    activity?.runOnUiThread {
                                        second_zone.visibility = View.VISIBLE
                                    }
                                }
                            }
                            else -> {
                                toastMSG(exception?.localizedMessage)
                            }
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
            AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString().toMail()).confirmSignUpInBackground(confirm_edt.text.toString(), true, object : GenericHandler {
                override fun onSuccess() {
                    dismiss()
                }

                override fun onFailure(exception: Exception?) {
                    when (exception) {
                        is AmazonServiceException -> {
                            toastMSG(exception.errorMessage)
                        }
                        else -> {
                            toastMSG(exception?.localizedMessage)
                        }
                    }
                }
            })
        }
        resendBtn.setOnClickListener {
            Thread(Runnable {
                AWSCognitoOAuthService.userPool.getUser(mail_edt.text.toString().toMail()).resendConfirmationCodeInBackground(object : VerificationHandler {
                    override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
                        activity?.runOnUiThread {
                            Alert("Code Sent", "Code Sent to " + verificationCodeDeliveryMedium?.destination)?.show()

                            Toast.makeText(
                                    activity,
                                    verificationCodeDeliveryMedium?.destination,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(exception: Exception?) {
                    }
                })
            }).start()
        }

    }


    override fun onDestroyView() {
        LoginFragment.fg?.nameEdt?.setText(mail_edt.text.toString().toMail())
        LoginFragment.fg?.passwordEdt?.setText(password_edt.text)

        super.onDestroyView()
        isShowing = false

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

fun DialogFragment.toastMSG(msg: String?) {
    activity?.runOnUiThread {
        Toast.makeText(
                activity,
                msg,
                Toast.LENGTH_LONG
        ).show()
    }
}
