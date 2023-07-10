package com.ampere.todayeat.util

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences存储或获取数据
 */
object SPUtils {
    private var sSp: SharedPreferences? = null
    private const val SP_NAME = "eat"

    /**
     * 保存List<String>菜单数据到SharedPreferences中
     *
     * @param context
     * @param key
     * @param stringList
    </String> */
    fun saveStringList(context: Context, key: String?, stringList: List<String>?) {
        if (stringList == null) {
            return
        }
        if (sSp == null) {
            sSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }
        val editor = sSp!!.edit()
        val data: Set<String> = HashSet(stringList)
        editor.putStringSet(key, data)
        editor.commit()
    }

    /**
     * 从SharedPreferences中获取List<String>数据
     *
     * @param context
     * @param key
     * @return
    </String> */
    fun getStringList(context: Context, key: String?): List<String>? {
        if (sSp == null) {
            sSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }
        val datas = sSp!!.getStringSet(key, null)
        return if (datas == null) {
            null
        } else {
            ArrayList(datas)
        }
    }
}
