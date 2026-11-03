package com.wang.round

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.*
import androidx.annotation.IntRange
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max

class RoundedHelper(private val target: IRoundedView, attrs: AttributeSet?, defStyleAttr: Int) {
    companion object {
        private inline fun View.withStyledAttributes(
            set: AttributeSet?,
            attrs: IntArray,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int = 0,
            block: TypedArray.() -> Unit,
        ) = withStyledAttributesOrNull(set, attrs, defStyleAttr, defStyleRes) {
            this?.block()
        }

        /**
         * obtainStyledAttributes
         */
        @OptIn(ExperimentalContracts::class)
        private inline fun View.withStyledAttributesOrNull(
            set: AttributeSet?,
            attrs: IntArray,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int = 0,
            block: TypedArray?.() -> Unit,
        ) {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }
            if (set == null && defStyleAttr == 0 && defStyleRes == 0) {
                null.block()
            } else {
                val ta = context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
                try {
                    ta.block()
                } finally {
                    ta.recycle()
                }
            }
        }

        private inline fun Canvas.withSaveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?, block: Canvas.() -> Unit) {
            val saveCount = saveLayer(left, top, right, bottom, paint)
            try {
                block()
            } finally {
                restoreToCount(saveCount)
            }
        }


        @Suppress("NOTHING_TO_INLINE")
        private inline fun Float.minZero(): Float = this.coerceAtLeast(0f)
    }

    private var radii: FloatArray? = null

    /**
     * 最后一个必须为默认
     */
    private var pathInfos = listOf(SysPathInfo(), DefPathInfo())

    private val borderPath = Path()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    var isOval: Boolean = false
        set(value) {
            if (field == value) return

            field = value
            if (value) {
                radii = null
            }
            newRadiusBorderPath()
            target.view.invalidate()
        }

    init {
        if (attrs != null) {
            target.view.withStyledAttributes(attrs, R.styleable.RoundedHelper, defStyleAttr, 0) {
                val radius = getDimensionPixelSize(R.styleable.RoundedHelper_rivCornerRadius, 0)
                val topLeftRadius = getDimensionPixelSize(R.styleable.RoundedHelper_rivCornerTopLeftRadius, radius)
                val topRightRadius = getDimensionPixelSize(R.styleable.RoundedHelper_rivCornerTopRightRadius, radius)
                val bottomLeftRadius = getDimensionPixelSize(R.styleable.RoundedHelper_rivCornerBottomLeftRadius, radius)
                val bottomRightRadius = getDimensionPixelSize(R.styleable.RoundedHelper_rivCornerBottomRightRadius, radius)
                setCornerRadius(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius)
                isOval = getBoolean(R.styleable.RoundedHelper_rivOval, isOval)
                setBorderWidth(getDimensionPixelSize(R.styleable.RoundedHelper_rivBorderWidth, 0))
                setBorderColor(getColor(R.styleable.RoundedHelper_rivBorderColor, 0))
            }
        }
    }

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {//init完第一次layout时也会走一遍
            newRadiusBorderPath()
        }
    }

    /**
     * 重新计算圆角及path
     */
    private fun newRadiusBorderPath() {
        if (target.view.width == 0 || target.view.height == 0) return

        val stroke2 = (borderPaint.strokeWidth + 1) / 2

        pathInfos.forEach { it.radiusPath.reset() }
        borderPath.reset()

        val pathRectF = RectF(
            target.view.paddingStart.toFloat(),
            target.view.paddingTop.toFloat(),
            target.view.width - target.view.paddingEnd.toFloat(),
            target.view.height - target.view.paddingBottom.toFloat(),
        )
        val borderRectF = RectF(
            target.view.paddingStart + stroke2,
            target.view.paddingTop + stroke2,
            target.view.width - target.view.paddingEnd - stroke2,
            target.view.height - target.view.paddingBottom - stroke2,
        )

        if (isOval) {//椭圆效果
            val rx = (target.view.width - target.view.paddingStart - target.view.paddingEnd) / 2f
            val ry = (target.view.height - target.view.paddingTop - target.view.paddingBottom) / 2f

            pathInfos.forEach {
                it.radiusPath.addRoundRect(pathRectF, rx, ry, Path.Direction.CW)
            }

            borderPath.addRoundRect(
                borderRectF,
                (rx - stroke2).minZero(),
                (ry - stroke2).minZero(),
                Path.Direction.CW,
            )
        } else {
            val topLeft: Float
            val topRight: Float
            val bottomLeft: Float
            val bottomRight: Float
            val radii = radii
            if (radii != null) {
                pathInfos.forEach {
                    it.radiusPath.addRoundRect(pathRectF, radii, Path.Direction.CW)
                }

                topLeft = (radii[0] - stroke2).minZero()
                topRight = (radii[2] - stroke2).minZero()
                bottomLeft = (radii[4] - stroke2).minZero()
                bottomRight = (radii[6] - stroke2).minZero()
            } else {
                topLeft = 0f
                topRight = 0f
                bottomLeft = 0f
                bottomRight = 0f
            }
            borderPath.addRoundRect(
                borderRectF,
                floatArrayOf(
                    topLeft,
                    topLeft,
                    topRight,
                    topRight,
                    bottomLeft,
                    bottomLeft,
                    bottomRight,
                    bottomRight,
                ),
                Path.Direction.CW,
            )
        }

        currentPathInfo().onPathChanged(pathRectF)
    }

    private fun currentPathInfo(): PathInfo {
        return pathInfos.firstOrNull { it.isThisPath() } ?: pathInfos.last()
    }

    fun onDraw(canvas: Canvas) {
        currentPathInfo().onDraw(canvas)
        if ((!borderPath.isEmpty) && borderPaint.strokeWidth > 0) {
            canvas.drawPath(borderPath, borderPaint)
        }
    }

    fun setPadding() {
        newRadiusBorderPath()
    }

    private abstract inner class PathInfo {
        abstract val radiusPath: Path

        @CallSuper
        open fun onDraw(canvas: Canvas) {
            @SuppressLint("RestrictedApi")
            target.onSupperDrawCall(canvas)
        }

        abstract fun onPathChanged(pathRectF: RectF)

        abstract fun isThisPath(): Boolean
    }

    private inner class DefPathInfo : PathInfo() {
        override val radiusPath = Path().apply { fillType = Path.FillType.INVERSE_WINDING }
        private val radiusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

        override fun onDraw(canvas: Canvas) {
            if (radiusPath.isEmpty) {
                super.onDraw(canvas)
            } else {
                canvas.withSaveLayer(0f, 0f, target.view.width.toFloat(), target.view.height.toFloat(), null) {
                    super.onDraw(canvas)
                    canvas.drawPath(radiusPath, radiusPaint)
                }
            }
        }

        override fun onPathChanged(pathRectF: RectF) {
            target.view.clipToOutline = false
            target.view.outlineProvider = null
        }

        override fun isThisPath() = true
    }

    private inner class SysPathInfo : PathInfo() {

        private var pathRectF: RectF? = null
        private var radius: Float = -1f

        override val radiusPath = Path()

        private val provider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    //虽然path是30支持的，但bug却是33修复的
                    outline.setPath(radiusPath)
                } else {
                    val rectF = pathRectF
                    if (rectF != null && radius >= 0) {
                        outline.setRoundRect(
                            rectF.left.toInt(),
                            rectF.top.toInt(),
                            rectF.right.toInt(),
                            rectF.bottom.toInt(),
                            radius,
                        )
                    } else {
                        Log.i("riv", "圆角rect或radius不正确：$rectF,$radius")
                        outline.setRect(0, 0, view.width, view.height)
                    }
                }
            }
        }

        override fun onDraw(canvas: Canvas) {
            if (canvas.isHardwareAccelerated) {
                super.onDraw(canvas)
            } else {
                //没有硬件加速无法使用outlineProvider，大概率是准备截屏，退回到默认
                pathInfos.last().onDraw(canvas)
            }
        }

        override fun onPathChanged(pathRectF: RectF) {
            val isClip = target.view.clipToOutline
            if (radiusPath.isEmpty) {
                this.pathRectF = null
                this.radius = -1f
                if (isClip) {
                    target.view.clipToOutline = false
                    target.view.outlineProvider = null
                }
            } else {
                this.pathRectF = pathRectF
                this.radius = if (isOval) max(target.view.width, target.view.height).toFloat() else radii?.getOrNull(0) ?: -1f
                if (!isClip) {
                    target.view.clipToOutline = true
                    target.view.outlineProvider = provider
                }
            }
        }

        /**
         * 低api只有部分可裁剪，参考[Outline.canClip]注释
         */
        override fun isThisPath(): Boolean {
            val radii = radii
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || (isOval && target.view.width == target.view.height) || (radii != null && radii.all { it == radii[0] })
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法（public method）
    /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * =0表示无描边
     */
    fun setBorderWidth(@Px @IntRange(from = 0) borderWidth: Int) {
        borderPaint.strokeWidth = max(0f, borderWidth.toFloat())
        newRadiusBorderPath()
        target.view.invalidate()
    }

    fun setBorderColor(@ColorInt borderColor: Int) {
        borderPaint.color = borderColor
        target.view.invalidate()
    }

    /**
     * =0表示无圆角
     */
    fun setCornerRadius(
        @Px @IntRange(from = 0) topLeft: Int,
        @Px @IntRange(from = 0) topRight: Int,
        @Px @IntRange(from = 0) bottomLeft: Int,
        @Px @IntRange(from = 0) bottomRight: Int,
    ) {
        if ((topLeft <= 0) and (topRight <= 0) and (bottomLeft <= 0) and (bottomRight <= 0)) {
            radii = null
        } else {
            radii = (radii ?: FloatArray(8)).apply {
                if ((this[0] == topLeft.toFloat()) and (this[2] == topRight.toFloat()) and
                    (this[4] == bottomRight.toFloat()) and (this[6] == bottomLeft.toFloat())
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
            if (isOval) {
                isOval = false
            }
        }
        newRadiusBorderPath()
        target.view.invalidate()
    }

    @Size(4)
    fun getCornerRadius() = radii?.let { intArrayOf(it[0].toInt(), it[2].toInt(), it[4].toInt(), it[6].toInt()) }

    //isOval在上面

    fun reset() {
        setBorderWidth(0)
        setBorderColor(0)
        setCornerRadius(0, 0, 0, 0)
        isOval = false
    }
}