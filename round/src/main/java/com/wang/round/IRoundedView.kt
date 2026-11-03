package com.wang.round

import android.graphics.Canvas
import android.view.View
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX
import androidx.annotation.RestrictTo.Scope.SUBCLASSES

interface IRoundedView {
    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun IRoundedView.setCornerRadius(@Px @IntRange(from = 0) radius: Int) =
            setCornerRadius(radius, radius, radius, radius)
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX, SUBCLASSES)//目前lib限制有bug所以写两个
    fun onSupperDrawCall(canvas: Canvas)

    val view: View

    var isOval: Boolean

    /**
     * =0表示无描边
     */
    fun setBorderWidth(@Px @IntRange(from = 0) borderWidth: Int)

    fun setBorderColor(@ColorInt borderColor: Int)

    /**
     * =0表示无圆角
     */
    fun setCornerRadius(
        @Px @IntRange(from = 0) topLeft: Int,
        @Px @IntRange(from = 0) topRight: Int,
        @Px @IntRange(from = 0) bottomLeft: Int,
        @Px @IntRange(from = 0) bottomRight: Int,
    )

    @Size(4)
    fun getCornerRadius(): IntArray?

    fun reset()
}