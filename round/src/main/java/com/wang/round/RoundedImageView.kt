package com.wang.round

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max

/**
 * 圆角view
 */
open class RoundedImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatImageView(context, attrs, defStyleAttr) {
    private var radii: FloatArray? = null
    private val radiusPath = Path()
    private val radiusPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPath = Path()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var isOval: Boolean = false

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        if (attrs != null) {
            val a =
                context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyleAttr, 0)

            val radius = a.getDimensionPixelSize(R.styleable.RoundedImageView_cornerRadius, 0)
            val topLeftRadius =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_cornerTopLeftRadius, radius)
            val topRightRadius =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_cornerTopRightRadius, radius)
            val bottomLeftRadius =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_cornerBottomLeftRadius, radius)
            val bottomRightRadius = a.getDimensionPixelSize(
                R.styleable.RoundedImageView_cornerBottomRightRadius,
                radius
            )
            setCornerRadiusPrivate(
                topLeftRadius,
                topRightRadius,
                bottomLeftRadius,
                bottomRightRadius,
                false
            )
            isOval = a.getBoolean(R.styleable.RoundedImageView_oval, isOval)
            setBorderWidth(a.getDimensionPixelSize(R.styleable.RoundedImageView_borderWidth, 0))
            setBorderColor(a.getColor(R.styleable.RoundedImageView_borderColor, 0))

            a.recycle()
        }

        super.setCropToPadding(true)//false在使用crop属性时padding会无效，难以计算

        borderPaint.style = Paint.Style.STROKE

        radiusPath.fillType = Path.FillType.INVERSE_WINDING
        radiusPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {//init完第一次layout时也会走一遍
            newRadiusBorderPath()
        }
    }

    private fun setCornerRadiusPrivate(
        @Px @IntRange(from = 0) topLeft: Int,
        @Px @IntRange(from = 0) topRight: Int,
        @Px @IntRange(from = 0) bottomLeft: Int,
        @Px @IntRange(from = 0) bottomRight: Int,
        isInvalidate: Boolean
    ) {
        if ((topLeft <= 0) and (topRight <= 0) and (bottomLeft <= 0) and (bottomRight <= 0)) {
            radii = null
        } else {
            if (radii == null) {
                radii = FloatArray(8)
            }
            radii?.run {
                if ((this[0] == topLeft.toFloat()) and (this[2] == topRight.toFloat()) and
                    (this[4] == bottomLeft.toFloat()) and (this[6] == bottomRight.toFloat())
                ) {
                    return
                }
                this[0] = topLeft.toFloat()
                this[1] = this[0]
                this[2] = topRight.toFloat()
                this[3] = this[2]
                this[4] = bottomRight.toFloat()
                this[5] = this[4]
                this[6] = bottomLeft.toFloat()
                this[7] = this[6]
            }
        }
        if (isInvalidate) {
            newRadiusBorderPath()
            invalidate()
        }
    }

    /**
     * 重新计算圆角及path
     */
    protected open fun newRadiusBorderPath() {
        if (width == 0 || height == 0) {
            return
        }

        val stroke2 = borderPaint.strokeWidth / 2
        val pStart =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) paddingStart else paddingLeft
        val pEnd =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) paddingEnd else paddingRight

        if (isOval) {//椭圆效果
            val rx = (width - pStart - pEnd) / 2f
            val ry = (height - paddingTop - paddingBottom) / 2f

            radiusPath.reset()
            radiusPath.addRoundRect(
                RectF(
                    pStart.toFloat(),
                    paddingTop.toFloat(),
                    width - pEnd.toFloat(),
                    height - paddingBottom.toFloat()
                ),
                rx,
                ry,
                Path.Direction.CW
            )

            borderPath.reset()
            borderPath.addRoundRect(
                RectF(
                    pStart + stroke2 - 1,
                    paddingTop + stroke2 - 1,
                    width - pEnd - stroke2 + 1,
                    height - paddingBottom - stroke2 + 1
                ),
                if (rx - stroke2 < 0) 0f else (rx - stroke2),
                if (ry - stroke2 < 0) 0f else (ry - stroke2),
                Path.Direction.CW
            )
        } else {
            radii?.run {
                radiusPath.reset()
                radiusPath.addRoundRect(
                    RectF(
                        pStart.toFloat(),
                        paddingTop.toFloat(),
                        width - pEnd.toFloat(),
                        height - paddingBottom.toFloat()
                    ),
                    this,
                    Path.Direction.CW
                )

                borderPath.reset()
                val topLeft = if (this[0] - stroke2 < 0) 0f else (this[0] - stroke2)
                val topRight = if (this[2] - stroke2 < 0) 0f else (this[2] - stroke2)
                val bottomLeft = if (this[4] - stroke2 < 0) 0f else (this[4] - stroke2)
                val bottomRight = if (this[6] - stroke2 < 0) 0f else (this[6] - stroke2)
                borderPath.addRoundRect(
                    RectF(
                        pStart + stroke2 - 1,
                        paddingTop + stroke2 - 1,
                        width - pEnd - stroke2 + 1,
                        height - paddingBottom - stroke2 + 1
                    ),
                    floatArrayOf(
                        topLeft,
                        topLeft,
                        topRight,
                        topRight,
                        bottomLeft,
                        bottomLeft,
                        bottomRight,
                        bottomRight
                    ),
                    Path.Direction.CW
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val saveCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        else
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        super.onDraw(canvas)
        if (isOval || radii != null) {
            canvas.drawPath(radiusPath, radiusPaint)
        }
        canvas.restoreToCount(saveCount)
        if (borderPaint.strokeWidth > 0) {
            canvas.drawPath(borderPath, borderPaint)
        }
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        newRadiusBorderPath()
    }

    @Deprecated("设置false的效果也不好，并且会导致padding难以计算，所以暂不支持设置")
    override fun setCropToPadding(cropToPadding: Boolean) {//空实现
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法（public method）
    /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * =0表示无描边
     */
    fun setBorderWidth(@Px @IntRange(from = 0) borderWidth: Int) {
        borderPaint.strokeWidth = max(0f, borderWidth.toFloat())
        invalidate()
    }

    fun setBorderColor(@ColorInt borderColor: Int) {
        borderPaint.color = borderColor
        invalidate()
    }

    /**
     * =0表示无圆角
     */
    fun setCornerRadius(@Px @IntRange(from = 0) radius: Int) {
        setCornerRadius(radius, radius, radius, radius)
    }

    fun setCornerRadius(
        @Px @IntRange(from = 0) topLeft: Int,
        @Px @IntRange(from = 0) topRight: Int,
        @Px @IntRange(from = 0) bottomLeft: Int,
        @Px @IntRange(from = 0) bottomRight: Int
    ) {
        setCornerRadiusPrivate(topLeft, topRight, bottomLeft, bottomRight, true)
    }

    /**
     * 是否以椭圆效果绘制
     */
    fun setIsOval(isOval: Boolean) {
        if (this.isOval == isOval) {
            return
        }
        this.isOval = isOval
        newRadiusBorderPath()
        invalidate()
    }
}