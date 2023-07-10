package com.ampere.todayeat.util

import android.content.Context

/**
 * 像素单位之间的转换
 */
object DisplayUtils {
    /**
     * dp转px
     *
     * @param context 上下文
     * @param dp      需要转换的数值
     * @return
     */
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param px      需要转换的数值
     * @return
     */
    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }
}
