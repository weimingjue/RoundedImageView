package com.wang.round

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.Size
import androidx.appcompat.widget.AppCompatImageView

class RoundedImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr), IRoundedView {

    private val helper = RoundedHelper(this, attrs, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        helper.onDraw(canvas)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        helper.setPadding()
    }

    override fun invalidate() {
        super.invalidate()
        if (clipToOutline) {
            invalidateOutline()
        }
    }

    @SuppressLint("WrongCall")
    override fun onSupperDrawCall(canvas: Canvas) = super.onDraw(canvas)

    override val view get() = this


    /////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法（public method）
    /////////////////////////////////////////////////////////////////////////////////////////////////

    override var isOval by helper::isOval

    /**
     * =0表示无描边
     */
    override fun setBorderWidth(@Px @IntRange(from = 0) borderWidth: Int) = helper.setBorderWidth(borderWidth)

    override fun setBorderColor(@ColorInt borderColor: Int) = helper.setBorderColor(borderColor)

    /**
     * =0表示无圆角
     */
    override fun setCornerRadius(
        @Px @IntRange(from = 0) topLeft: Int,
        @Px @IntRange(from = 0) topRight: Int,
        @Px @IntRange(from = 0) bottomLeft: Int,
        @Px @IntRange(from = 0) bottomRight: Int,
    ) = helper.setCornerRadius(topLeft, topRight, bottomLeft, bottomRight)

    @Size(4)
    override fun getCornerRadius() = helper.getCornerRadius()

    override fun reset() = helper.reset()
}