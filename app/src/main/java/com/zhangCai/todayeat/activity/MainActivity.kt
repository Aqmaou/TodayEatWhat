package com.zhangCai.todayeat.activity

import android.animation.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.zhangCai.todayeat.R
import com.zhangCai.todayeat.adapter.MenuAdapter
import com.zhangCai.todayeat.util.DefaultValueUtil
import com.zhangCai.todayeat.util.SPUtils
import com.zhangCai.todayeat.view.AddDialog
import com.zhangCai.todayeat.view.AddDialog.InputResultListener
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


/**
 * 主界面
 *
 *
 * 摇一摇 https://blog.csdn.net/u013144863/article/details/52958674
 */
class MainActivity : AppCompatActivity(), View.OnClickListener, SensorEventListener {
    private var iv_welcome: ImageView? = null //欢迎界面
    private var iv_add: ImageView? = null //添加菜品按钮
    private var ll_none: LinearLayout? = null //还没有菜品时的界面
    private var mDialog: AddDialog? = null //添加的dialog
    private var gv_menu: GridView? = null //菜单
    private var mMenuAdapter: MenuAdapter? = null //菜单的adapter
    private var tv_shakeTips: TextView? = null //摇一摇提示
    private var rl_addLayout: RelativeLayout? = null //添加布局
    private var rl_chooseLayout: RelativeLayout? = null //选择布局
    private var tv_chooseFinish: TextView? = null //全选
    private var tv_chooseCount: TextView? = null //选择的个数
    private var tv_chooseAll: TextView? = null //全选
    private var tv_delete: TextView? = null //删除
    private var iv_result: ImageView? = null //结果图片
    private var fl_resultLayout: FrameLayout? = null //结果布局
    private var tv_result: TextView? = null //结果文字
    private var tv_sure: TextView? = null //确定
    private var mSensorManager: SensorManager? = null //
    private var mAccelerometerSensor: Sensor? = null
    private var isShake = false
    private var mShakeHandle: ShakeHandle? = null
    private var isShakeEnabled = true //标志是否允许触发摇一摇，后来添加 7.10
    var mSoundPool: SoundPool? = null
    var mVibrator: Vibrator? = null //震动服务
    var isCanShake = false //是否可以摇一摇
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        initView()
        hideActionBar()
        //欢迎界面显示2秒
        iv_welcome!!.visibility = View.VISIBLE
        iv_welcome!!.postDelayed({ iv_welcome!!.visibility = View.GONE }, 2000)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        mShakeHandle = ShakeHandle(this)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager != null) {
            mAccelerometerSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (mAccelerometerSensor != null) {
                mSensorManager!!.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }
        @Suppress("DEPRECATION")
        mSoundPool = SoundPool(1, AudioManager.STREAM_SYSTEM, 5)
        mSoundPool!!.load(this, R.raw.shake, 1)
        /*
                val mVibrator = ContextCompat.getSystemService(applicationContext, Vibrator::class.java)
                mVibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        */

        mVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onPause() {
        if (mSensorManager != null) {
            mSensorManager!!.unregisterListener(this)
        }
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isCanShake) {
            return
        }
        val type = event.sensor.type
        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            val values = event.values
            val x = values[0]
            val y = values[1]
            val z = values[2]
            println("x is $x y is$y z is $z")
            //            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 17) && !isShake) {
            if ((abs(x) > 4 || abs(y) > 17 || abs(z) > 4) && !isShake) {
                isShake = true
                isShakeEnabled = false //设置标志位false，禁止触发摇一摇 7.10加
                val thread: Thread = object : Thread() {
                    override fun run() {
                        super.run()
                        try {
                            mShakeHandle!!.obtainMessage(ShakeHandle.START_SHAKE).sendToTarget()
                            sleep(500)
                            mShakeHandle!!.obtainMessage(ShakeHandle.AGAIN_SHAKE).sendToTarget()
                            sleep(500)
                            mShakeHandle!!.obtainMessage(ShakeHandle.END_SHAKE).sendToTarget()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } finally {
                            isShakeEnabled = true //重新设置标志为 true,表示允许触发摇一摇
                        }
                    }
                }
                thread.start()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    /**
     * 获取界面组件
     */
    private fun initView() {
        iv_welcome = findViewById<View>(R.id.activity_main_welcome_iv) as ImageView
        iv_add = findViewById<View>(R.id.activity_main_add_iv) as ImageView
        ll_none = findViewById<View>(R.id.activity_main_none_layout_ll) as LinearLayout
        gv_menu = findViewById<View>(R.id.activity_main_menu_gl) as GridView
        tv_shakeTips = findViewById<View>(R.id.activity_main_shake_tips_tv) as TextView
        iv_add!!.setOnClickListener(this)
        mMenuAdapter = MenuAdapter(applicationContext)
        /*        mMenuAdapter!!.setOnChooseListener { size ->
                    showDeleteAndHideAddHead()
                    if(tv_chooseCount != null){
                        tv_chooseCount?.text = "已选择 $size 项" ?: "并没有选择"
                    }

                }*/
        mMenuAdapter?.setOnChooseListener(object : MenuAdapter.OnChooseListener {
            @SuppressLint("SetTextI18n")
            fun onChoose() {
                showDeleteAndHideAddHead()

            }
            @SuppressLint("SetTextI18n")
            override fun onChooseSize(size: Int) {
                showDeleteAndHideAddHead()
                tv_chooseCount?.text = "已选择 $size 项"
            }
        })


        val meuns = SPUtils.getStringList(this, DATA_KEY)
        if (meuns != null) {
            mMenuAdapter!!.addItemList(meuns)
        }
        gv_menu!!.adapter = mMenuAdapter
        rl_addLayout = findViewById<View>(R.id.activity_main_add_layout_rl) as RelativeLayout
        rl_chooseLayout = findViewById<View>(R.id.activity_main_choose_layout_rl) as RelativeLayout
        tv_chooseFinish = findViewById<View>(R.id.activity_main_choose_finish_tv) as TextView
        tv_chooseCount = findViewById<View>(R.id.activity_main_choose_count_tv) as TextView
        tv_chooseAll = findViewById<View>(R.id.activity_main_choose_all_tv) as TextView
        tv_delete = findViewById<View>(R.id.activity_main_delete_tv) as TextView
        iv_result = findViewById<View>(R.id.activity_main_result_iv) as ImageView
        fl_resultLayout = findViewById<View>(R.id.activity_main_result_layout_fl) as FrameLayout
        tv_result = findViewById<View>(R.id.activity_main_result_tv) as TextView
        tv_sure = findViewById<View>(R.id.activity_main_sure_tv) as TextView
        tv_chooseFinish!!.setOnClickListener(this)
        tv_chooseAll!!.setOnClickListener(this)
        tv_delete!!.setOnClickListener(this)
        tv_shakeTips!!.setOnClickListener(this)
        iv_result!!.setOnClickListener(this)
        tv_sure!!.setOnClickListener(this)
        showMenuOrEmpty()
    }

    /**
     * 隐藏ActionBar
     */
    private fun hideActionBar() {
        val actionBar = supportActionBar
        actionBar?.hide()
    }

    private fun showMenuOrEmpty() {
        if (mMenuAdapter != null && mMenuAdapter!!.count == 0) {
            isCanShake = false
            ll_none!!.visibility = View.VISIBLE
            gv_menu!!.visibility = View.GONE
            tv_shakeTips!!.visibility = View.GONE
        } else {
            isCanShake = true
            ll_none!!.visibility = View.GONE
            gv_menu!!.visibility = View.VISIBLE
            tv_shakeTips!!.visibility = View.VISIBLE
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.activity_main_add_iv ->                 //点击添加条目按钮
                showAddDialog()

            R.id.activity_main_choose_finish_tv ->                 //点击完成按钮
                showAddAndHideDeleteHead()

            R.id.activity_main_choose_all_tv ->                 //点击全选按钮
                if (mMenuAdapter != null) {
                    mMenuAdapter!!.chooseAll()
                }

            R.id.activity_main_delete_tv ->                 //点击删除
                delete()

            R.id.activity_main_result_iv -> {
                //显示选择的答案
                val result = (Math.random() * (mMenuAdapter!!.count - 1) + 0.5).toInt()
                Log.d(TAG, "result:$result")
                val name = mMenuAdapter!!.getItem(result) as String
                tv_result!!.visibility = View.VISIBLE
                tv_result!!.text = name
                tv_result!!.setBackgroundResource(DefaultValueUtil.MENU_COLORS[result % DefaultValueUtil.MENU_COLORS.size])
                tv_sure!!.visibility = View.VISIBLE
                val inAnimator = AnimatorInflater.loadAnimator(this, R.animator.rotate_in_anim) as AnimatorSet
                val outAnimator = AnimatorInflater.loadAnimator(this, R.animator.rotate_out_anim) as AnimatorSet
                val distance = 16000
                val scale = resources.displayMetrics.density * distance
                iv_result!!.cameraDistance = scale
                tv_result!!.cameraDistance = scale
                outAnimator.setTarget(iv_result)
                inAnimator.setTarget(tv_result)
                outAnimator.start()
                inAnimator.start()
                outAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        iv_result!!.visibility = View.GONE
                        iv_result!!.alpha = 1.0f
                        iv_result!!.rotationY = 0.0f
                    }
                })
            }

            R.id.activity_main_sure_tv -> {
                //确定答案
                isCanShake = true
                iv_add!!.visibility = View.VISIBLE
                fl_resultLayout!!.visibility = View.GONE
                tv_sure!!.visibility = View.GONE
                tv_shakeTips!!.visibility = View.VISIBLE
                gv_menu!!.visibility = View.VISIBLE
            }

            else -> {}
        }
    }

    //连续删除有问题，需查看
    private fun delete() {
        if (mMenuAdapter != null) {
            val deleteList = mMenuAdapter!!.delete()
//            mMenuAdapter!!.count = mMenuAdapter!!.count - deleteList.size
            if (deleteList.isNotEmpty() && mMenuAdapter!!.count > 0) {
                animateReorder(deleteList, mMenuAdapter!!.count)
                mMenuAdapter?.notifyDataSetChanged() //刷新GridView的子项视图
                showMenuOrEmpty()
            }
        }
        saveData()
        //showMenuOrEmpty() 7.10隐藏
        if (mMenuAdapter != null && mMenuAdapter!!.count == 0) {
            showAddAndHideDeleteHead()
        }
    }

    private fun showAddAndHideDeleteHead() {
        rl_chooseLayout!!.visibility = View.GONE
        rl_addLayout!!.visibility = View.VISIBLE
        tv_delete!!.visibility = View.GONE
        tv_shakeTips!!.visibility = View.VISIBLE
        if (mMenuAdapter != null) {
            mMenuAdapter!!.cancelDelete()
        }
    }

    private fun showDeleteAndHideAddHead() {
        if (rl_chooseLayout!!.visibility != View.VISIBLE) {
            rl_chooseLayout!!.visibility = View.VISIBLE
            rl_addLayout!!.visibility = View.GONE
            tv_shakeTips!!.visibility = View.GONE
            tv_delete!!.visibility = View.VISIBLE
        }
    }

    /**
     * 删除item后，其他的item需要自动补齐
     *
     * @param deleteList
     * @param itemCount
     */
    private fun animateReorder(deleteList: List<Int>?, itemCount: Int) {
        if (deleteList.isNullOrEmpty() || itemCount < 1) {
            return
        }
        val list: MutableList<Animator> = ArrayList()
        val mutableList = deleteList.toMutableList()
        mutableList.sort()

        val beforeDelete = IntArray(itemCount + mutableList.size)
        for (i in mutableList.indices) {
            beforeDelete[mutableList[i]] = -1
        }
        for (i in 0 until itemCount) {
            if (beforeDelete[i] == -1 || beforeDelete[i] != i) {
                for (j in i until beforeDelete.size) {
                    if (beforeDelete[j] == 0) {
                        beforeDelete[j] = i
                        break
                    }
                }
            } else if (beforeDelete[i] != -1) {
                beforeDelete[i] = i
            }
        }
        Log.d(TAG, "beforeDelete：" + beforeDelete.contentToString())
        for (pos in mutableList[0] until itemCount) {
            var beforeDeleteCount = 0
            for (i in beforeDelete.indices) {
                if (beforeDelete[i] != pos) {
                    if (beforeDelete[i] == -1) {
                        beforeDeleteCount++
                    }
                } else {
                    break
                }
            }
            Log.d(TAG, "pos:$pos,beforeDeleteCount:$beforeDeleteCount")
            val view = gv_menu!!.getChildAt(pos - gv_menu!!.firstVisiblePosition)
            if ((pos + beforeDeleteCount) % gv_menu!!.numColumns == 0) {
                var removeHeightCount = beforeDeleteCount / gv_menu!!.numColumns
                if (beforeDeleteCount % gv_menu!!.numColumns != 0) {
                    removeHeightCount += 1
                }
                list.add(
                    createAnimator(
                        view,
                        (-view.width * (beforeDeleteCount % gv_menu!!.numColumns)).toFloat(),
                        0f,
                        (view.height * removeHeightCount).toFloat(),
                        0f
                    )
                )
            } else {
                list.add(
                    createAnimator(
                        view,
                        (view.width * (beforeDeleteCount % gv_menu!!.numColumns)).toFloat(),
                        0f,
                        (view.height * (beforeDeleteCount / gv_menu!!.numColumns)).toFloat(),
                        0f
                    )
                )
            }
        }
        val set = AnimatorSet()
        set.playTogether(list)
        set.interpolator = AccelerateDecelerateInterpolator()
        set.setDuration(300)
        set.start()
    }

    /**
     * 平移动画
     *
     * @param view   需要移动的view
     * @param startX 开始的X坐标
     * @param endX   结束的X坐标
     * @param startY 开始的Y坐标
     * @param endY   结束的Y坐标
     * @return
     */
    private fun createAnimator(
        view: View, startX: Float,
        endX: Float, startY: Float, endY: Float
    ): AnimatorSet {
        val animX = ObjectAnimator.ofFloat(
            view, "translationX",
            startX, endX
        )
        val animY = ObjectAnimator.ofFloat(
            view, "translationY",
            startY, endY
        )
        val animSetXY = AnimatorSet()
        animSetXY.playTogether(animX, animY)
        return animSetXY
    }

    /**
     * 摇一摇的反应
     */
    fun shake() {
        isCanShake = false
        gv_menu!!.visibility = View.GONE
        tv_shakeTips!!.visibility = View.GONE
        fl_resultLayout!!.visibility = View.VISIBLE
        tv_result!!.visibility = View.GONE
        iv_result!!.visibility = View.VISIBLE
        iv_add!!.visibility = View.GONE
    }

    /**
     * 显示添加菜品对话框
     */
    private fun showAddDialog() {
        if (mDialog == null) {
            mDialog = AddDialog()
            mDialog!!.setInputResultListener(object : InputResultListener {
                override fun result(input: String?) {
                    Log.d(TAG, "result input:$input")
                    if (mMenuAdapter!!.count == 0) {
                        if (ll_none!!.visibility != View.GONE) {
                            ll_none!!.visibility = View.GONE
                        }
                        if (gv_menu!!.visibility != View.VISIBLE) {
                            gv_menu!!.visibility = View.VISIBLE
                        }
                        if (tv_shakeTips!!.visibility != View.VISIBLE) {
                            tv_shakeTips!!.visibility = View.VISIBLE
                        }
                        isCanShake = true
                        input?.let { mMenuAdapter!!.addItem(it) }
                        saveData()
                        mDialog!!.dismissAllowingStateLoss()
                    } else {
                        if (mMenuAdapter != null) {
                            val dataList = mMenuAdapter!!.data
                            if (dataList != null && dataList.contains(input)) {
                                Toast.makeText(this@MainActivity, "列表中已经有这个了哦！", Toast.LENGTH_SHORT).show()
                            } else {
                                isCanShake = true
                                input?.let { mMenuAdapter!!.addItem(it) }
                                mDialog!!.dismiss()
                                saveData()
                            }
                        } else {
                            isCanShake = true
                            mDialog!!.dismiss()
                        }
                    }
                    showMenuOrEmpty()
                }

                override fun showPrompt(message: String?) {
                    isCanShake = true
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }


                /*                fun showPrompt(message: String) {
                                    isCanShake = true
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                                }*/
            })
        } else {
            if (mDialog!!.isVisible) {
                mDialog!!.dismiss()
            }
        }
        if (mDialog!!.dialog == null || !mDialog!!.dialog?.isShowing!!) {
            mDialog!!.show(supportFragmentManager, AddDialog::class.java.simpleName)
        }
        isCanShake = false
    }

    /**
     * 保存数据
     */
    private fun saveData() {
        if (mMenuAdapter != null) {
            val data = mMenuAdapter!!.data
            Log.d(TAG, "onDestroy data:$data")
            SPUtils.saveStringList(this, DATA_KEY, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveData()
    }

    private class ShakeHandle(activity: MainActivity) : Handler(Looper.getMainLooper()) { //这里替代了过时的Handler
        private val mReference: WeakReference<MainActivity>?
        private var mActivity: MainActivity? = null

        init {
            mReference = WeakReference(activity)
            mActivity = mReference.get()
        }
        @Suppress("DEPRECATION") //用来镇压vibrate过时错误
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d(TAG, "handleMessage msg:$msg")
            when (msg.what) {
                START_SHAKE -> {
                    mActivity!!.mVibrator!!.vibrate(300)
                    mActivity!!.mSoundPool!!.play(R.raw.shake, 1f, 1f, 0, 0, 1f)
                }

                AGAIN_SHAKE -> mActivity!!.mVibrator!!.vibrate(300)
                END_SHAKE -> {
                    mActivity!!.isShake = false
                    mActivity!!.shake()

                    //接下来的应用由自己设计，解决摇一摇后会持续触发ShakeHandle函数
                    // 移除传感器监听器
                    mActivity!!.mSensorManager?.unregisterListener(mActivity!!)

                    // 重新注册传感器监听器（延迟一定时间）
                    postDelayed({
                        mActivity!!.mSensorManager?.registerListener(
                            mActivity,
                            mActivity!!.mAccelerometerSensor,
                            SensorManager.SENSOR_DELAY_UI
                        )
                    }, 1000) // 这里的延迟时间可以根据需要进行调整
                    //在END_SHAKE设置一定延迟，保证不会一直触发
                }

                else -> {}
            }
        }

        companion object {
            const val START_SHAKE = 1
            const val AGAIN_SHAKE = 2
            const val END_SHAKE = 3
        }
    }

    /**
     * 适配UI，没有找到合适的机器去测试
     *
     * @param activity
     * @param application
     */
    private fun setCustomDensity(activity: Activity, application: Application) {
        val appDisplayMetrics = application.resources.displayMetrics
        if (sNoncompatDensity == 0f) {
            sNoncompatDensity = appDisplayMetrics.density
            sNoncompatScaleDensity = appDisplayMetrics.scaledDensity
            application.registerComponentCallbacks(object : ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    if (newConfig.fontScale > 0) {
                        sNoncompatScaleDensity = application.resources.displayMetrics.scaledDensity
                    }
                }

                override fun onLowMemory() {}
            })
        }
        Log.d(TAG, "sNoncompatDensity:$sNoncompatDensity,sNoncompatScaleDensity:$sNoncompatScaleDensity")
        val targetDensity = (appDisplayMetrics.widthPixels / 720).toFloat()
        val targetScaledDensity = targetDensity * (sNoncompatScaleDensity / sNoncompatDensity)
        val targetDensityDpi = (160 * targetDensity).toInt()
        Log.d(
            TAG,
            "targetDensity:$targetDensity,targetScaledDensity:$targetScaledDensity,targetDensityDpi:$targetDensityDpi"
        )
        Log.d(
            TAG,
            "appDisplayMetrics.density:" + appDisplayMetrics.density + ",appDisplayMetrics.scaledDensity:" + appDisplayMetrics.scaledDensity + ",appDisplayMetrics.densityDpi:" + appDisplayMetrics.densityDpi
        )
        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val DATA_KEY = "menu"
        private var sNoncompatDensity = 0f
        private var sNoncompatScaleDensity = 0f
    }
}
