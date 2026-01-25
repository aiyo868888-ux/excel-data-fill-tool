package com.fleetingnotes.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 剪切板监听服务
 *
 * 功能：
 * - 监听剪切板内容变化
 * - 提供剪切板内容状态流
 * - 防抖动处理，避免频繁触发
 * - Android 12+ 需要用户授权
 */
class ClipboardService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var clipboardManager: ClipboardManager
    private var lastClipContent: String? = null
    private var debounceJob: Job? = null

    private val _clipboardText = MutableStateFlow<String?>(null)
    val clipboardText: StateFlow<String?> = _clipboardText.asStateFlow()

    private val isProcessing = AtomicBoolean(false)

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        handleClipboardChange()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("ClipboardService created")

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // 初始读取剪切板内容
        handleClipboardChange()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("ClipboardService started")

        // 注册剪切板监听器
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Timber.d("ClipboardService destroyed")

        // 移除监听器
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)

        // 取消所有协程
        debounceJob?.cancel()
        serviceScope.cancel()

        super.onDestroy()
    }

    /**
     * 处理剪切板内容变化（带防抖动）
     *
     * 防抖动逻辑：
     * - 如果正在处理，跳过本次变化
     * - 等待 500ms 后再处理，避免短时间内多次变化
     */
    private fun handleClipboardChange() {
        // 如果正在处理，跳过
        if (!isProcessing.compareAndSet(false, true)) {
            Timber.d("Clipboard change skipped: already processing")
            return
        }

        // 取消之前的待处理任务
        debounceJob?.cancel()

        // 延迟 500ms 处理，避免频繁触发
        debounceJob = serviceScope.launch {
            delay(500)
            processClipboardChange()
        }
    }

    /**
     * 实际处理剪切板内容变化
     */
    private suspend fun processClipboardChange() {
        try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()

                // 避免重复处理相同内容
                if (text != null && text != lastClipContent && text.isNotBlank()) {
                    lastClipContent = text
                    _clipboardText.emit(text)
                    Timber.d("Clipboard content changed: ${text.take(50)}...")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reading clipboard")
        } finally {
            // 重置处理标志
            isProcessing.set(false)
        }
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "剪切板监听",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "监听剪切板内容变化"
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
                .setContentText("剪切板监听中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("闪念笔记")
                .setContentText("剪切板监听中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "clipboard_listener_channel"
    }
}
