package com.candyhouse.app.tabs.devices.ssm2.menber

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.candyhouse.R
import com.candyhouse.app.base.BaseFG
import com.candyhouse.app.base.BaseNFG
import com.candyhouse.app.base.BaseSSMFG
import com.candyhouse.app.base.scan.ScanCallBack
import com.candyhouse.app.base.scan.ScanFG
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.sesame.deviceprotocol.CHAccessLevel
import com.candyhouse.sesame.deviceprotocol.SSM2CmdResultCode
import com.candyhouse.sesame.deviceprotocol.SSM2ItemCode
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.a.model.UserProfile
import com.candyhouse.utils.L
import com.utils.alertview.enums.AlertActionStyle
import com.utils.alertview.enums.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.kasturi.admin.genericadapter.GenericAdapter
import kotlinx.android.synthetic.main.back_sub.*
import kotlinx.android.synthetic.main.fg_add_member.*
import java.util.*


class AddMemberFG : BaseSSMFG() {
    val mFriends = ArrayList<UserProfile>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_add_member, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swiperefresh.apply {
            setOnRefreshListener { refleshPage() }
        }
        recy.apply {
            setHasFixedSize(true)
            adapter = object : GenericAdapter<UserProfile>(mFriends) {
                override fun getLayoutId(position: Int, obj: UserProfile): Int {
                    return R.layout.cell_friend
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view), Binder<UserProfile> {
                        var customName = itemView.findViewById<TextView>(R.id.title)
                        var head = itemView.findViewById<ImageView>(R.id.avatar)

                        @SuppressLint("SetTextI18n")
                        override fun bind(data: UserProfile, pos: Int) {
                            customName.text = data.nickname
                            itemView.setOnClickListener {
                                val alert = AlertView(data.nickname, "", AlertStyle.IOS)
                                alert.addAction(AlertAction(getString(R.string.add_member), AlertActionStyle.NEGATIVE) { action ->
                                    MainActivity.activity?.showProgress()
                                    mSesame?.addKeyByFriend(CHAccessLevel.MANAGER, data.id) { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: Any? ->
                                        MainActivity.activity?.hideProgress()
                                        itemView?.post {
                                            findNavController().navigateUp()
                                        }
                                    }
                                })
                                alert.show(MainActivity.activity as AppCompatActivity)
                            }
                            head.setImageDrawable(avatatImagGenaroter(data.firstName))
                        }
                    }
                }
            }
        }

        backicon.setOnClickListener {
            findNavController().navigateUp()
        }
        right_icon.setOnClickListener {
//            findNavController().navigateUp()
            ScanFG.callBack = object : ScanCallBack {
                override fun onScanFriendSuccess(friendID: String) {
                    MainActivity.activity?.showProgress()
                    mSesame?.addKeyByFriend(CHAccessLevel.MANAGER, UUID.fromString(friendID)) { cmd: SSM2ItemCode?, res: SSM2CmdResultCode?, second: Any? ->
                        MainActivity.activity?.hideProgress()
                        right_icon?.post {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
            findNavController().navigate(R.id.to_scan)


        }
        refleshPage()

    }

    fun refleshPage() {

        CHAccountManager.myFriends {
            it.onSuccess {
                recy?.post {
                    mFriends.clear()
                    mFriends.addAll(it.data.sortedBy { it.nickname })
                    (recy?.adapter as GenericAdapter<*>)?.notifyDataSetChanged()
                }
            }
            it.onFailure {
            }
            swiperefresh?.post {
                swiperefresh.isRefreshing = false
            }
        }
    }

}