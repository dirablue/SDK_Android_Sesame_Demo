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

package com.candyhouse.app.tabs.friends

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.candyhouse.R
import com.candyhouse.app.tabs.MainActivity
import com.candyhouse.app.tabs.devices.ssm2.room.avatatImagGenaroter
import com.candyhouse.app.tabs.menu.BarMenuItem
import com.candyhouse.app.tabs.menu.CustomAdapter
import com.candyhouse.app.tabs.menu.ItemUtils
import com.candyhouse.sesame.server.CHAccountManager
import com.candyhouse.sesame.server.a.model.UserProfile
import com.candyhouse.utils.L
import com.utils.alertview.enums.AlertActionStyle
import com.utils.alertview.enums.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.kasturi.admin.genericadapter.GenericAdapter
import com.skydoves.balloon.*
import java.util.*

class FriendsFG : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swiperefreshView: SwipeRefreshLayout
    val mFriends = ArrayList<UserProfile>()
    private lateinit var customListBalloon: Balloon
    private val customAdapter by lazy {
        CustomAdapter(object : CustomAdapter.CustomViewHolder.Delegate {
            override fun onCustomItemClick(customItem: BarMenuItem) {
                customListBalloon?.dismiss()

                when (customItem.title) {
                    "Add Friend" -> {
                        findNavController().navigate(R.id.action_deviceFG_to_scanFG)
                    }
                    "New Sesame" -> {
                        findNavController().navigate(R.id.to_regist)
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fg_friends, container, false)
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
                .setWidth(200)
                .setHeight(120)
                .setTextSize(12f)
                .setCornerRadius(4f)
                .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                .setBackgroundColorResource(R.color.menu_bg)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenClicked(true)
                .setOnBalloonClickListener(object : OnBalloonClickListener {
                    override fun onBalloonClick(view: View) {
                        L.d("hcia", "onBalloonClick:")
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

        swiperefreshView = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh).apply {
            setOnRefreshListener { refleshPage() }
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recy).apply {
            setHasFixedSize(true)
            adapter = object : GenericAdapter<UserProfile>(mFriends) {
                override fun getLayoutId(position: Int, obj: UserProfile): Int {
                    return R.layout.cell_friend
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(view), Binder<UserProfile> {
                        var customName = itemView.findViewById<TextView>(R.id.title)
                        var head = itemView.findViewById<ImageView>(R.id.avatar)

                        override fun bind(data: UserProfile, pos: Int) {
                            customName.text = data.nickname
                            itemView.setOnClickListener {
                                val alert = AlertView(data.nickname, "", AlertStyle.IOS)
                                alert.addAction(AlertAction(getString(R.string.delete_friend), AlertActionStyle.NEGATIVE) { action ->
                                    CHAccountManager.unfriend(data.id) {
                                        refleshPage()
                                    }
                                })
                                alert.show(MainActivity.activity as AppCompatActivity)
                            }
                            head.setImageDrawable(avatatImagGenaroter(data.lastName))
                        }
                    }
                }
            }
        }

        return view
    }

    fun refleshPage() {
        CHAccountManager.meyFriends {
            it.onSuccess {
                recyclerView.post {
                    mFriends.clear()
                    mFriends.addAll(it.data)
                    (recyclerView.adapter as GenericAdapter<*>).notifyDataSetChanged()
                }
            }
            it.onFailure {
                L.d("hcia", "it:" + it)
            }
            ThreadUtils.runOnUiThread {
                swiperefreshView.isRefreshing = false
            }
        }
    }
}
