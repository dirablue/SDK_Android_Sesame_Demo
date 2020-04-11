package com.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.candyhouse.R
import com.candyhouse.app.tabs.devices.ssmUIParcer
import com.candyhouse.sesame.ble.CHSesameBleInterface
import com.candyhouse.utils.L
import kotlin.math.cos
import kotlin.math.sin
import android.util.DisplayMetrics as DisplayMetrics

class SSMCellView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var ssmImg: Bitmap
    private var midx: Float? = null
    private var midy: Float? = null
    private var angle: Float = 0f
    private var lockAngle: Float = 0f
    private var unlockAngle: Float = 0f

    var ssmWidth: Int = 0
    var ssmMargin: Int = 0
    var lockWidth: Int = 0
    var lockMargin: Int = 0
    var lockCenter: Float = 0f
    var dotPaint: Paint

    init {

        ssmImg = ContextCompat.getDrawable(context,R.drawable.img_knob_3x)!!.toBitmap()
        dotPaint = Paint()
        dotPaint.setColor(ContextCompat.getColor(context, R.color.clear))
//        dotPaint.setStrokeWidth(30F)
        dotPaint.setStyle(Paint.Style.FILL)
        dotPaint.setAntiAlias(true)
        dotPaint.setDither(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        midx = width / 2.toFloat()
        midy = height / 2.toFloat()

        ssmWidth = width * 8 / 10
        ssmMargin = (width - ssmWidth) / 2

        lockWidth = width / 30
        lockMargin = ssmWidth / 2 + lockWidth
        lockCenter = midx!! // must  x = y
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(ssmImg, Rect(0, 0, 0 + ssmImg.width, 0 + ssmImg.width), Rect(ssmMargin, ssmMargin, ssmMargin + ssmWidth, ssmMargin + ssmWidth), null)

        val lockdeg = angle.toDG()
        val lockMarginX = lockCenter + cos(lockdeg) * (lockMargin)
        val lockMarginY = lockCenter - sin(lockdeg) * (lockMargin)

        canvas.drawCircle(lockMarginX.toFloat(), lockMarginY.toFloat(), lockWidth.toFloat(), dotPaint)

    }

    fun setLock(ssm: CHSesameBleInterface) {
        ssmImg = getResources().getDrawable(ssmUIParcer(ssm)).toBitmap()

        if (ssm.mSSM2MechSetting == null) {
            dotPaint.setColor(ContextCompat.getColor(context, R.color.clear))

        }else{
            dotPaint.setColor(ContextCompat.getColor(context, if (ssm.mechStatus!!.inLockRange) R.color.lock_red else R.color.unlock_blue))
            ssm.mSSM2MechSetting?.unlockPosition
            val degree = ssm.mechStatus!!.position.toFloat() * 360 / 1024
            angle = degree % 360
        }
        invalidate()
    }
}

