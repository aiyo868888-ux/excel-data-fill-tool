package com.fleetingnotes.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import com.fleetingnotes.presentation.MainActivity
import com.fleetingnotes.data.model.NoteType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import timber.log.Timber

/**
 * 悬浮窗服务 - 提供快速笔记捕获入口
 *
 * 完全照搬及时记的做法：
 * - 使用 ACTION_SHOW/ACTION_HIDE 控制悬浮窗
 * - 不监听剪切板，点击时读取
 * - 设置页面控制开关
 */
class FloatingWindowService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null

    // Lifecycle components
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    // 悬浮窗位置
    private var offsetX = 0f
    private var offsetY = 0f

    companion object {
        const val ACTION_SHOW = "com.fleetingnotes.ACTION_SHOW"
        const val ACTION_HIDE = "com.fleetingnotes.ACTION_HIDE"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "floating_window_channel"

        // 照搬及时记：运行状态标志
        var isRunning = false
            private set
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        Timber.d("FloatingWindowService created")

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("FloatingWindowService onStartCommand")
        val action = intent?.action

        when (action) {
            ACTION_SHOW -> {
                // 照搬及时记：显示悬浮窗
                if (!isRunning) {
                    Timber.i("✅ 开始显示悬浮窗")
                    setupFloatingWindow()
                    isRunning = true
                } else {
                    Timber.i("⚠️ 悬浮窗已在运行")
                }
            }
            ACTION_HIDE -> {
                // 照搬及时记：隐藏悬浮窗
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
        Timber.d("FloatingWindowService destroyed")
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        removeFloatingWindow()
        super.onDestroy()
    }

    /**
     * 创建通知渠道（Android 8.0+ 需要）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持悬浮窗服务运行"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("闪念笔记")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("闪念笔记")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        }
    }

    /**
     * 设置悬浮窗
     */
    private fun setupFloatingWindow() {
        floatingView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingWindowService)
            setViewTreeViewModelStoreOwner(this@FloatingWindowService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)

            setContent {
                FloatingWindowContent(
                    onOpenTypeDialog = { openTypeSelectionDialog() },
                    onClose = { stopSelf() },
                    onOffsetChange = { x, y ->
                        offsetX = x
                        offsetY = y
                        updateViewPosition()
                    },
                    currentOffsetX = offsetX,
                    currentOffsetY = offsetY
                )
            }
        }

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
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(floatingView, params)
    }

    /**
     * 移除悬浮窗
     */
    private fun removeFloatingWindow() {
        try {
            floatingView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Timber.e("Failed to remove floating window: ${e.message}")
        }
    }

    /**
     * 打开类型选择对话框
     *
     * 悬浮窗保持圆形按钮不变，弹出独立的类型选择对话框
     */
    private fun openTypeSelectionDialog() {
        Timber.d("openTypeSelectionDialog() called")
        try {
            // 启动类型选择 Activity
            val intent = Intent(this, com.fleetingnotes.presentation.ui.edit.TypeSelectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            Timber.d("Intent created: $intent")
            Timber.d("Intent component: ${intent.component}")
            startActivity(intent)
            Timber.d("TypeSelectionActivity started successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to open type selection dialog: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 更新悬浮窗位置
     */
    private fun updateViewPosition() {
        floatingView?.let { view ->
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return
            params.x = offsetX.roundToInt()
            params.y = offsetY.roundToInt()
            windowManager.updateViewLayout(view, params)
        }
    }
}

/**
 * 悬浮窗内容 - 只显示圆形按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowContent(
    onOpenTypeDialog: () -> Unit,
    onClose: () -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    currentOffsetX: Float,
    currentOffsetY: Float
) {
    var offsetX by remember { mutableStateOf(currentOffsetX) }
    var offsetY by remember { mutableStateOf(currentOffsetY) }
    var isDragging by remember { mutableStateOf(false) }

    // 只显示圆形按钮
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        isDragging = true
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onOffsetChange(offsetX, offsetY)
                    },
                    onDragEnd = {
                        // 拖拽结束
                    },
                    onDragCancel = {
                        // 拖拽取消
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B))
                .clickable(
                    onClickLabel = "打开类型选择"
                ) {
                    timber.log.Timber.d("Floating button clicked!")
                    onOpenTypeDialog()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💡",
                fontSize = 24.sp
            )
        }
    }
}
