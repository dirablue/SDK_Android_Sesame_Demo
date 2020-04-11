package com.candyhouse.app.tabs.account

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler
import com.candyhouse.R
import com.candyhouse.app.base.BaseFG
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.account.login.LoginFragment
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.app.tabs.menu.BarMenuItem
import com.candyhouse.app.tabs.menu.CustomAdapter
import com.candyhouse.app.tabs.menu.ItemUtils
import com.candyhouse.server.AWSCognitoOAuthService
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.utils.L
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.skydoves.balloon.*
import com.utils.SharedPreferencesUtils
import com.utils.alertview.enums.AlertActionStyle
import com.utils.alertview.enums.AlertStyle
import kotlinx.android.synthetic.main.fg_me.*
import kotlinx.android.synthetic.main.fragment_register.family_name
import kotlinx.android.synthetic.main.fragment_register.given_name
import kotlinx.android.synthetic.main.fragment_register.mail
import pe.startapps.alerts.ext.inputNameAlert


class MeFG : BaseFG() {
    private lateinit var customListBalloon: Balloon

    var headv: ImageView? = null
    private val customAdapter by lazy {
        CustomAdapter(object : CustomAdapter.CustomViewHolder.Delegate {
            override fun onCustomItemClick(customItem: BarMenuItem) {
                customListBalloon?.dismiss()
                when (customItem.index) {
                    0 -> {
                        findNavController().navigate(R.id.to_scan)
                    }
                    1 -> {
                        findNavController().navigate(R.id.to_regist)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
//        L.d("hcia", "我 onResume:" )

        if (MainActivity.nowTab == 2) {
            (activity as MainActivity).showMenu()
        }
//        setName()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_me, container, false)

        headv = view.findViewById<ImageView>(R.id.head)
        view.findViewById<View>(R.id.signup_btn).setOnClickListener {

            val alert = AlertView("", "", AlertStyle.IOS)
            alert.addAction(AlertAction(getString(R.string.logout), AlertActionStyle.NEGATIVE) { action ->
                LoginFragment.newInstance().show(activity?.supportFragmentManager!!, "")
                AWSCognitoOAuthService.logOut()
            })
            alert.show(activity as AppCompatActivity)

        }
        val menuBtn = view.findViewById<View>(R.id.right_icon).apply {
            setOnClickListener {
                customListBalloon.showAlignBottom(it)
            }
        }
        customListBalloon = Balloon.Builder(menuBtn.context)
                .setLayout(R.layout.layout_custom_list)
                .setArrowSize(12)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.85f)
                .setTextSize(12f)
                .setCornerRadius(4f)
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .setBackgroundColorResource(R.color.menu_bg)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenClicked(true)
                .setOnBalloonClickListener(object : OnBalloonClickListener {
                    override fun onBalloonClick(view: View) {
                    }
                })
                .setDismissWhenClicked(true)
                .setOnBalloonOutsideTouchListener(object : OnBalloonOutsideTouchListener {
                    override fun onBalloonOutsideTouch(view: View, event: MotionEvent) {
                        menuBtn.isClickable = false
                        customListBalloon?.dismiss()
                        menuBtn.postDelayed({
                            menuBtn.isClickable = true
                        }, 300)
                    }
                })
                .build()

        customListBalloon.getContentView().findViewById<RecyclerView>(R.id.list_recyclerView)
                .apply {
                    setHasFixedSize(true)
                    adapter = customAdapter
                    customAdapter.addCustomItem(ItemUtils.getCustomSamples(context!!))
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        L.d("hcia", "我 onViewCreated:" )

        qrcode.setOnClickListener {
            MyQrcodeFG.mailStr = mail.text.toString()
            MyQrcodeFG.givenName = given_name.text.toString()
            MyQrcodeFG.familyName = family_name.text.toString()
            findNavController().navigate(R.id.action_register_to_myQrcodeFG)
        }
        change_name_zone.setOnClickListener {
            context?.inputNameAlert(getString(R.string.edit_name), family_name.text.toString(), given_name.text.toString()) {
                confirmButtonWithDoubleEdit("OK") { name, top, down ->
                    Thread(Runnable {
                        val awsAttributes = CognitoUserAttributes()
                        awsAttributes.addAttribute("family_name", top)
                        awsAttributes.addAttribute("given_name", down)
                        AWSCognitoOAuthService.userPool.currentUser.updateAttributes(awsAttributes, object : UpdateAttributesHandler {
                            override fun onSuccess(attributesVerificationList: MutableList<CognitoUserCodeDeliveryDetails>?) {
                                this@MeFG.given_name?.post {
                                    this@MeFG.given_name?.text = down
                                    headv?.setImageDrawable(avatatImagGenaroter(down))
                                    this@MeFG.family_name?.text = top
                                    CHAccountManager.updateMyProfile(this@MeFG.given_name.text.toString(), this@MeFG.family_name.text.toString()) {}
                                }
                            }

                            override fun onFailure(exception: java.lang.Exception?) {
                            }
                        })

                    }).start()
                    dismiss()
                }
                cancelButton("Cancel")
            }?.show()
        }
        setName()

    }

    /**
     * given_name 名
     * family_name 姓
     * */
    private fun setName() {

        this@MeFG.family_name?.text = SharedPreferencesUtils.family_name
        this@MeFG.given_name?.text = SharedPreferencesUtils.given_name
        head.setImageDrawable(avatatImagGenaroter(SharedPreferencesUtils.given_name))

        AWSCognitoOAuthService.getID(object : AWSCognitoOAuthService.NameChange {
            override fun onName(myID: String?) {
                mail.text = myID
                AWSCognitoOAuthService.userPool.currentUser.getDetailsInBackground(object : GetDetailsHandler {
                    override fun onSuccess(cognitoUserDetails: CognitoUserDetails?) {
                        this@MeFG.given_name?.post {
                            cognitoUserDetails?.attributes?.attributes?.forEach {
                                when (it.key) {
                                    "given_name" -> {
                                        this@MeFG.given_name?.text = it.value
                                        head.setImageDrawable(avatatImagGenaroter(it.value))
                                        SharedPreferencesUtils.given_name = it.value
                                    }
                                    "family_name" -> {
                                        this@MeFG.family_name?.text = it.value
                                        SharedPreferencesUtils.family_name = it.value
                                    }
                                }
                            }
//                            L.d("hcia","綁定名字")
                            CHAccountManager.updateMyProfile(this@MeFG.given_name.text.toString(), this@MeFG.family_name.text.toString()) {}
                        }
                    }

                    override fun onFailure(exception: Exception?) {
                    }
                })
            }
        })
    }
}
