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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import com.jishi.clipboard.utils.ClipboardUpdateEvent
import com.jishi.clipboard.utils.DialogManager
import com.jishi.clipboard.utils.PasteRequestEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        Timber.i("onStartCommand: action=$action, isRunning=$isRunning")

        // ✅ 转为前台服务，防止被系统杀死
        val notification = NotificationHelper.createNotification(this)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

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

        // ✅ 移除前台服务通知
        stopForeground(STOP_FOREGROUND_REMOVE)

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
     * 读取剪切板内容
     */
    private fun readClipboard(): String {
        return try {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            } else {
                @Suppress("DEPRECATION")
                clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            }
        } catch (e: Exception) {
            Timber.e(e, "读取剪贴板失败")
            ""
        }
    }

    /**
     * 自动吸附到最近的边缘
     */
    private fun snapToEdge(params: WindowManager.LayoutParams) {
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
                        handleFloatingWindowClick()
                    }
                })

                start()
                currentAnimator = this
            }
        }
    }

    /**
     * 处理悬浮窗点击事件
     * 根据当前状态决定：粘贴内容 / 带回对话框 / 显示类型选择
     */
    private fun handleFloatingWindowClick() {
        when {
            // 场景1：编辑对话框正在显示 → 直接发送粘贴事件
            DialogManager.isEditDialogShowing() -> {
                Timber.d("场景1：对话框已显示，发送粘贴事件")
                sendPasteEvent()
            }
            // 场景2：EditActivity 存在但对话框未显示 → 带到前台后发送粘贴事件
            com.jishi.clipboard.ui.EditActivity.instance?.get() != null -> {
                Timber.d("场景2：EditActivity存在但对话框未显示，带到前台")
                bringEditActivityToFront()
                waitForDialogAndSendPasteEvent()
            }
            // 场景3：首次使用 → 显示类型选择
            else -> {
                Timber.d("场景3：首次使用，显示类型选择")
                showContentTypeSelection()
            }
        }
    }

    /**
     * 等待对话框恢复后发送粘贴事件
     */
    private fun waitForDialogAndSendPasteEvent() {
        // 使用多次重试机制，每次间隔500ms，最多重试3次（总1.5秒）
        val maxRetries = 3
        val retryDelay = 500L

        fun trySendPasteEvent(retryCount: Int) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (DialogManager.isEditDialogShowing()) {
                    Timber.d("对话框已显示，发送粘贴事件（重试${retryCount}次后成功）")
                    sendPasteEvent()
                } else if (retryCount < maxRetries) {
                    Timber.w("对话框仍未显示，继续重试 (${retryCount + 1}/$maxRetries)")
                    trySendPasteEvent(retryCount + 1)
                } else {
                    Timber.e("对话框仍未显示，重试次数耗尽")
                    // 最后一次尝试直接发送事件
                    sendPasteEvent()
                }
            }, retryDelay)
        }

        trySendPasteEvent(0)
    }

    /**
     * 尝试将 EditActivity 带到前台
     * @return true 如果 EditActivity 存在并被带到前台，false 否则
     */
    private fun bringEditActivityToFront(): Boolean {
        val activityInstance = com.jishi.clipboard.ui.EditActivity.instance?.get() ?: return false

        // 检查 Activity 是否还在运行（未被销毁）
        if (activityInstance.isFinishing || activityInstance.isDestroyed) {
            Timber.d("EditActivity 已被销毁")
            return false
        }

        try {
            // 使用 FLAG_ACTIVITY_REORDER_TO_FRONT 将已有的 EditActivity 带到前台
            // 不会重建 Activity，只是调整任务栈顺序
            val intent = Intent(this, com.jishi.clipboard.ui.EditActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            startActivity(intent)
            Timber.d("将 EditActivity 带到前台")
            return true
        } catch (e: Exception) {
            Timber.e(e, "启动 EditActivity 失败")
            return false
        }
    }

    /**
     * 发送粘贴事件到对话框
     * 使用 EventBus 代替直接调用，解决时序问题
     */
    private fun sendPasteEvent() {
        serviceScope.launch {
            try {
                val clipboardContent = readClipboard()

                if (clipboardContent.isNotEmpty()) {
                    // 发送粘贴事件，由对话框的 EventBus 监听器处理
                    EventBus.getDefault().post(PasteRequestEvent(clipboardContent))
                    Timber.d("发送粘贴事件: ${clipboardContent.take(30)}...")
                } else {
                    // 剪贴板为空，提示用户
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FloatingWindowService, "剪贴板没有内容", Toast.LENGTH_SHORT).show()
                    }
                    Timber.w("剪贴板为空，用户尝试粘贴失败")
                }
            } catch (e: Exception) {
                Timber.e(e, "发送粘贴事件失败")
            }
        }
    }

    /**
     * 显示内容类型选择对话框
     */
    private fun showContentTypeSelection() {
        val intent = Intent(this, com.jishi.clipboard.ui.TypeSelectionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
    }

    // ==================== 内存管理 ====================

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Timber.d("onTrimMemory: level=$level")

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_COMPLETE -> {
                // 严重内存不足，释放悬浮窗
                Timber.w("⚠️ 内存严重不足，隐藏悬浮窗")
                saveWindowState()
                removeFloatingWindow()
            }
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_MODERATE -> {
                // 内存不足，保存状态
                Timber.d("💾 内存不足，保存悬浮窗状态")
                saveWindowState()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.w("⚠️ 系统内存不足，隐藏悬浮窗")
        saveWindowState()
        removeFloatingWindow()
    }

    /**
     * 保存悬浮窗状态
     */
    private fun saveWindowState() {
        val prefs = getSharedPreferences("floating_window", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_running", isRunning)
            putInt("last_x", initialX)
            putInt("last_y", initialY)
            apply()
        }
        Timber.d("💾 悬浮窗状态已保存")
    }

    /**
     * 检查并恢复悬浮窗状态
     */
    private fun checkAndRestoreWindow() {
        val prefs = getSharedPreferences("floating_window", Context.MODE_PRIVATE)
        val wasRunning = prefs.getBoolean("is_running", false)

        if (wasRunning && !isRunning) {
            Timber.d("🔄 检测到服务异常停止，尝试恢复悬浮窗")

            // 延迟恢复，避免启动冲突
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isRunning && hasOverlayPermission()) {
                    Timber.i("✅ 恢复悬浮窗显示")
                    showFloatingWindow()
                    isRunning = true
                }
            }, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // ✅ 检查并恢复状态
        Handler(Looper.getMainLooper()).postDelayed({
            checkAndRestoreWindow()
        }, 1000)

        Timber.i("悬浮窗服务已创建")
    }
}
