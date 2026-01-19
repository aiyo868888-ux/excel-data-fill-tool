package com.jishi.clipboard.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 悬浮窗服务
 * 显示可拖动的悬浮窗，点击时弹出编辑对话框
 */
@AndroidEntryPoint
class FloatingWindowService : Service() {

    @Inject
    lateinit var clipboardRepository: com.jishi.clipboard.repository.ClipboardRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var currentAnimator: AnimatorSet? = null

    companion object {
        private const val DEFAULT_X = 100
        private const val DEFAULT_Y = 200

        const val ACTION_SHOW = "com.jishi.clipboard.ACTION_SHOW"
        const val ACTION_HIDE = "com.jishi.clipboard.ACTION_HIDE"

        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Timber.i("悬浮窗服务已创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        Timber.i("onStartCommand: action=$action, isRunning=$isRunning")

        when (action) {
            ACTION_SHOW -> {
                if (!isRunning) {
                    if (!hasOverlayPermission()) {
                        Timber.e("❌ 没有悬浮窗权限！")
                        stopSelf()
                        return START_NOT_STICKY
                    }

                    Timber.i("✅ 开始显示悬浮窗")
                    showFloatingWindow()
                    isRunning = true
                } else {
                    Timber.i("⚠️ 悬浮窗已在运行")
                }
            }
            ACTION_HIDE -> {
                if (isRunning) {
                    Timber.i("🛑 隐藏悬浮窗")
                    removeFloatingWindow()
                    isRunning = false
                    stopSelf()
                }
            }
            else -> {
                Timber.w("⚠️ 未知的 action: $action")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        currentAnimator?.cancel()
        currentAnimator = null
        removeFloatingWindow()
        isRunning = false
        super.onDestroy()
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    /**
     * 显示悬浮窗
     */
    private fun showFloatingWindow() {
        if (!hasOverlayPermission()) {
            Timber.e("❌ 没有悬浮窗权限，无法显示")
            stopSelf()
            return
        }

        if (floatingView != null) {
            Timber.w("⚠️ floatingView 已存在，跳过创建")
            return
        }

        Timber.i("📱 创建悬浮窗视图")
        try {
            floatingView = LayoutInflater.from(this)
                .inflate(R.layout.floating_window_layout, null)
            Timber.i("✅ 悬浮窗视图创建成功")
        } catch (e: Exception) {
            Timber.e(e, "❌ 创建悬浮窗视图失败")
            return
        }

        // 获取屏幕宽度，计算靠边位置
        val screenWidth = resources.displayMetrics.widthPixels
        val screenWidthDp = screenWidth / resources.displayMetrics.density
        val marginDp = 16 // 边距 16dp
        val marginPx = (marginDp * resources.displayMetrics.density).toInt()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = marginPx
            y = (200 * resources.displayMetrics.density).toInt() // 默认垂直位置
        }

        val clickSlop = ViewConfiguration.get(this).scaledTouchSlop

        // 设置触摸事件
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).toInt()
                        val newY = initialY + (event.rawY - initialTouchY).toInt()
                        params.x = newX
                        params.y = newY

                        floatingView?.let {
                            windowManager.updateViewLayout(it, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = kotlin.math.abs(event.rawX - initialTouchX)
                        val deltaY = kotlin.math.abs(event.rawY - initialTouchY)

                        if (deltaX < clickSlop && deltaY < clickSlop) {
                            onFloatingViewClick()
                        } else {
                            // 拖动结束后，自动吸附到最近的边缘
                            snapToEdge(params)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
        Timber.i("✅ 悬浮窗已显示到屏幕")
    }

    /**
     * 移除悬浮窗
     */
    private fun removeFloatingWindow() {
        floatingView?.let { view ->
            view.setOnTouchListener(null)
            windowManager.removeView(view)
            floatingView = null
        }
        Timber.i("悬浮窗已移除")
    }

    /**
     * 悬浮窗点击处理
     */
    private fun onFloatingViewClick() {
        Timber.i("悬浮窗被点击")
        animateFloatingViewClick()
    }

    /**
     * 自动吸附到最近的边缘
     */
    private fun snapToEdge(params: WindowManager.LayoutParams) {
        val screenWidth = resources.displayMetrics.widthPixels
        val marginPx = (16 * resources.displayMetrics.density).toInt()

        // 获取悬浮窗当前位置的中心点
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val displaySize = android.graphics.Point()
        windowManager.defaultDisplay.getSize(displaySize)

        // 计算悬浮窗中心在屏幕中的位置
        val viewCenter = params.x + (72 * resources.displayMetrics.density).toInt() / 2 // 72dp 是悬浮窗宽度
        val screenCenter = displaySize.x / 2

        // 根据中心点位置决定靠左还是靠右
        if (viewCenter < screenCenter) {
            // 靠左
            params.gravity = Gravity.TOP or Gravity.START
            params.x = marginPx
        } else {
            // 靠右
            params.gravity = Gravity.TOP or Gravity.END
            params.x = marginPx
        }

        floatingView?.let {
            windowManager.updateViewLayout(it, params)
        }
        Timber.i("悬浮窗已吸附到边缘")
    }

    /**
     * 悬浮窗点击动画
     */
    private fun animateFloatingViewClick() {
        floatingView?.let { view ->
            currentAnimator?.cancel()

            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.0f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                        showEditDialog()
                    }
                })

                start()
                currentAnimator = this
            }
        }
    }

    /**
     * 显示编辑对话框
     * 使用透明 Activity，不干扰当前应用
     */
    private fun showEditDialog() {
        val intent = Intent(this, com.jishi.clipboard.ui.EditActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra("auto_fill_clipboard", true)
        }
        startActivity(intent)
    }
}
