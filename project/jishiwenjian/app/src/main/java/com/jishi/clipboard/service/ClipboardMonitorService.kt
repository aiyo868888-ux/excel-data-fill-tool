package com.jishi.clipboard.service

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 剪贴板监听服务
 * 监听系统剪贴板的变化
 */
@AndroidEntryPoint
class ClipboardMonitorService : Service() {

    @Inject
    lateinit var clipboardRepository: com.jishi.clipboard.repository.ClipboardRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var clipboardManager: ClipboardManager
    private var lastContent: String? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val currentContent = readClipboard()
        Timber.d("剪贴板变化: ${currentContent?.take(50)}...")

        if (currentContent != null && currentContent.isNotEmpty() && currentContent != lastContent) {
            lastContent = currentContent
            Timber.d("剪贴板内容已更新，开始保存")

            // 保存到数据库
            serviceScope.launch {
                try {
                    clipboardRepository.saveClipboard(currentContent, emptyList())
                    Timber.d("剪贴板内容已保存")
                } catch (e: Exception) {
                    Timber.e(e, "保存剪贴板内容失败")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        Timber.i("剪贴板监听服务已创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 避免重复注册监听器
        try {
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
            Timber.i("剪贴板监听器已注册")
        } catch (e: Exception) {
            Timber.e(e, "注册剪贴板监听器失败")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        Timber.i("剪贴板监听服务已销毁")
        super.onDestroy()
    }

    /**
     * 读取当前剪贴板内容
     */
    fun readClipboard(): String? {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "读取剪贴板失败")
            null
        }
    }

    companion object {
        const val ACTION_START = "com.jishi.clipboard.ACTION_START_MONITOR"
        const val ACTION_STOP = "com.jishi.clipboard.ACTION_STOP_MONITOR"
    }
}
