package pe.startapps.alerts

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.candyhouse.R
import com.candyhouse.utils.L
import kotlinx.android.synthetic.main.alert_check.*
import pe.startapps.alerts.ext.AlertType


class CheckAlert(context: Context) : BaseAlert(context) {

    var titleText: String? = null
    var contentText: String? = null

    private var cancelText: String? = null

    private var mOnCancel: ((CheckAlert) -> Unit)? = null

    override val layout: Int get() = R.layout.alert_check

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(mAlertView) {

            titleText?.let {
                tvTitle.text = it
                tvTitle.visibility = View.VISIBLE
            }
            contentText?.let {
                subTitle.text = it
                subTitle.visibility = View.VISIBLE
            }

        }
        update()

    }

    fun update() {
        btnCancel.visibility = View.VISIBLE
        btnCancel.setOnClickListener {
            this@CheckAlert.dismiss()
//                mOnCancel?.invoke(this@CheckAlert) ?: this@CheckAlert.dismiss()
        }
    }


}