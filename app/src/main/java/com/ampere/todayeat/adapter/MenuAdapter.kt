package com.ampere.todayeat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ampere.todayeat.R
import com.ampere.todayeat.util.DefaultValueUtil
import java.util.*



/**
 * 菜单列表的adapter
 * Author: zhangmiao
 * Date: 2018/10/8
 */
class MenuAdapter(//上下文
    private val mContext: Context
) : BaseAdapter() {
    private var mData: MutableList<String>? = null //显示的数据
    private var mShowChoose = false //显示删除选择
    private val mItemChooseList //被选择的链表
            : MutableList<String>?

    /**
     * 添加
     *
     * @param name
     */
    fun addItem(name: String) {
        if (mData == null) {
            mData = ArrayList()
        }
        mData!!.add(name)
        notifyDataSetChanged()
    }

    /**
     * 添加列表
     *
     * @param datas 需要添加的数据
     */
    fun addItemList(datas: List<String>?) {
        if (mData == null) {
            mData = ArrayList()
        }
        mData!!.addAll(datas!!)
    }

    val data: List<String>?
        /**
         * 获取数据
         *
         * @return
         */
        get() = mData

    override fun getCount(): Int {
        return if (mData == null) 0 else mData!!.size
    }

    override fun getItem(position: Int): Any {
        return mData!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        var menuHolder: MenuHolder? = null //后续自主添加
        if (view == null) {
            // 如果 convertView 为 null，创建一个新的视图对象
            view = LayoutInflater.from(parent.context).inflate(R.layout.grid_menu_item, parent, false)
            menuHolder = MenuHolder(view)
            view.tag = menuHolder
        } else {
            menuHolder = view.tag as MenuHolder
        }
        //var menuHolder: MenuHolder? = null这是原来的位置
        //menuHolder = view.tag as MenuHolder 这是原来的位置
        menuHolder.tv_name.text = mData!![position]
        menuHolder.tv_name.setBackgroundResource(DefaultValueUtil.MENU_COLORS[position % DefaultValueUtil.MENU_COLORS.size])
        menuHolder.tv_name.setOnClickListener { Log.d(TAG, "name:" + mData!![position]) }
        menuHolder.tv_name.setOnLongClickListener {
            if (!mShowChoose) {
                mShowChoose = true
                mItemChooseList!!.add(position.toString() + "")
                if (mOnChooseListener != null) {
                    mOnChooseListener!!.onChooseSize(mItemChooseList.size)
                }
                Log.d(TAG, "onLongClick")
                notifyDataSetChanged()
            }
            true
        }
        Log.d(TAG, "position:$position,mShowChoose:$mShowChoose")
        if (mShowChoose) {
            menuHolder.iv_choose.visibility = View.VISIBLE
        } else {
            menuHolder.iv_choose.visibility = View.GONE
        }
        if (mItemChooseList!!.contains(position.toString() + "")) {
            menuHolder.iv_choose.setBackgroundResource(R.mipmap.pick)
        } else {
            menuHolder.iv_choose.setBackgroundResource(R.mipmap.choose)
        }
        menuHolder.iv_choose.setOnClickListener { v ->
            if (mItemChooseList.contains(position.toString() + "")) {
                v.setBackgroundResource(R.mipmap.choose)
                mItemChooseList.remove(position.toString() + "")
            } else {
                v.setBackgroundResource(R.mipmap.pick)
                mItemChooseList.add(position.toString() + "")
            }
            if (mOnChooseListener != null) {
                mOnChooseListener!!.onChooseSize(mItemChooseList.size)
            }
        }
        return view!!
    }

    /**
     * 全选
     */
    fun chooseAll() {
        for (i in mData!!.indices) {
            if (!mItemChooseList!!.contains(i.toString() + "")) {
                mItemChooseList.add(i.toString() + "")
            }
        }
        if (mOnChooseListener != null) {
            mOnChooseListener!!.onChooseSize(mItemChooseList!!.size)
        }
        notifyDataSetChanged()
    }

    /**
     * 删除
     */
    fun delete(): List<Int> {
        val deleteList: MutableList<Int> = ArrayList()
//        Collections.sort(mItemChooseList) //后续的5个mItemChooseList均以被替换成mutableList
/*        val mutableList = deleteList.toMutableList()
7.10隐藏
        mutableList.sort()*/
        mItemChooseList!!.sort()
        for (i in mItemChooseList.indices.reversed()){
            val index = mItemChooseList[i].toInt()
            deleteList.add(index)
            mData!!.removeAt(index)
        }
        mItemChooseList.clear()
        notifyDataSetChanged()
/*        if (mutableList != null && mutableList.size > 0) { 7.10隐藏
            for (i in mutableList.indices.reversed()) {
                val index = mutableList[i].toInt()
                deleteList.add(index)
                mData!!.removeAt(index)
            }
            mutableList.clear()
            notifyDataSetChanged()
        }*/
        return deleteList
    }

    /**
     * 取消删除
     */
    fun cancelDelete() {
        mShowChoose = false
        mItemChooseList!!.clear() //mItemChooseList
        notifyDataSetChanged()
    }

    private inner class MenuHolder(view: View) {
        val tv_name: TextView
        val iv_choose: ImageView

        init {
            tv_name = view.findViewById<View>(R.id.grid_menu_name_tv) as TextView
            iv_choose = view.findViewById<View>(R.id.grid_menu_choose_iv) as ImageView
        }
    }

    private var mOnChooseListener: OnChooseListener? = null

    init {
        mItemChooseList = ArrayList()
    }

    fun setOnChooseListener(OnChooseListener: OnChooseListener) { //MenuAdapter.
        mOnChooseListener = OnChooseListener
    }


    /**
     * 选择回调
     */
    interface OnChooseListener {
        fun onChooseSize(size: Int)
    }

    companion object {
        private val TAG = MenuAdapter::class.java.simpleName
    }
}
