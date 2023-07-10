package com.zhangCai.todayeat.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment //自己加的
//import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.zhangCai.todayeat.R
import com.zhangCai.todayeat.util.DisplayUtils.dp2px

/**
 * 添加菜单项的弹出框
 * Author: zhangmiao
 * Date: 2018/10/8
 */
class AddDialog : DialogFragment() {
    var et_name: EditText? = null //输入框
    var tv_cancel: TextView? = null //取消
    var tv_sure: TextView? = null //确定
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val window = dialog?.window
        val view = inflater.inflate(
            R.layout.dialog_add,
            window!!.findViewById<View>(android.R.id.content) as ViewGroup,
            false
        ) //需要用android.R.id.content这个view
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(dp2px(requireContext(), 296f), dp2px(requireContext(), 170f))
//        window.setLayout(dp2px(context, 296f), dp2px(context, 170f))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_name = view.findViewById(R.id.dialog_add_name_et)
        tv_cancel = view.findViewById(R.id.dialog_add_cancel_tv)
        tv_sure = view.findViewById(R.id.dialog_add_sure_tv)
        val temp1 = et_name //这里只能用var 不能用val，因为后面还要用temp进行转存
        if (temp1 != null){
            et_name = temp1
        }
        et_name?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.length > EDIT_MAX) {
                    if (mInputResultListener != null) {
                        mInputResultListener!!.showPrompt("最多输入10个字哦")
                    }
                    et_name!!.setText(editable.toString().substring(0, EDIT_MAX))
                }
            }
        })
        val temp2 = tv_cancel // 自己加的
        if (temp2 != null){ //参考博客 https://blog.csdn.net/Joven0/article/details/105219989
            tv_cancel = temp2
        }
        val temp3 = tv_sure
        if (temp3 != null){
            tv_sure = temp3
        }
        tv_cancel?.setOnClickListener(View.OnClickListener { dismiss() })
        tv_sure?.setOnClickListener(View.OnClickListener {
            if (et_name != null) {
                var name = et_name!!.text.toString()
                name = name.replace(" ", "")
                if (!TextUtils.isEmpty(name)) {
                    if (mInputResultListener != null) {
                        mInputResultListener!!.result(name)
                    } else {
                        dismiss()
                    }
                    et_name!!.setText("")
                } else {
                    if (mInputResultListener != null) {
                        mInputResultListener!!.showPrompt("没有输入菜品名称呀！")
                    }
                }
            }
        })
    }

    private var mInputResultListener: InputResultListener? = null
    fun setInputResultListener(inputResultListener: InputResultListener?) {
        mInputResultListener = inputResultListener
    }

    /**
     * 输入框的回调
     */
    interface InputResultListener {
        fun result(input: String?) //输入结果
        fun showPrompt(message: String?) //显示提示
    }

    companion object {
        private val TAG = AddDialog::class.java.simpleName
        private const val EDIT_MAX = 10 //可以输入的最长字符
    }
}
