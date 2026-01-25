package com.jishi.clipboard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jishi.clipboard.R
import com.jishi.clipboard.network.security.TokenManager
import com.jishi.clipboard.network.server.WebServerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Web同步前台服务
 * 负责保持HTTP服务器运行，显示通知，监控网络状态
 */
@AndroidEntryPoint
class WebSyncService : Service() {

    companion object {
        private const val TAG = "WebSyncService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "web_sync_channel"
        private const val ACTION_START = "com.jishi.clipboard.ACTION_START_WEB_SYNC"
        private const val ACTION_STOP = "com.jishi.clipboard.ACTION_STOP_WEB_SYNC"

        fun startService(context: Context) {
            val intent = Intent(context, WebSyncService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WebSyncService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var webServerManager: WebServerManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binder = LocalBinder()

    // 网络状态监听
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager

    // 配对码
    private val _pairingToken = MutableStateFlow<String?>(null)
    val pairingToken: MutableStateFlow<String?> = _pairingToken

    inner class LocalBinder : Binder() {
        fun getService(): WebSyncService = this@WebSyncService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WebSyncService创建")

        createNotificationChannel()
        setupNetworkCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWebSync()
            ACTION_STOP -> stopWebSync()
            else -> Log.w(TAG, "未知操作: ${intent?.action}")
        }

        // START_STICKY: 服务被杀死后自动重启
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WebSyncService销毁")

        // 停止服务器
        stopWebSync()

        // 取消协程
        serviceScope.cancel()

        // 注销网络监听
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "注销网络监听失败", e)
        }
    }

    /**
     * 启动Web同步
     */
    private fun startWebSync() {
        Log.d(TAG, "启动Web同步")

        // 检查WiFi连接
        if (!webServerManager.isWifiConnected()) {
            Log.w(TAG, "WiFi未连接，无法启动")
            updateNotification("WiFi未连接", "请连接WiFi后重试")
            return
        }

        // 生成配对码
        val token = TokenManager.generateToken()
        _pairingToken.value = token

        // 启动服务器
        serviceScope.launch {
            val success = webServerManager.start()
            if (success) {
                val address = webServerManager.serverAddress.value
                updateNotification(
                    "Web同步运行中",
                    "地址: https://$address\n配对码: $token"
                )
                Log.i(TAG, "✅ Web同步已启动: https://$address")
            } else {
                updateNotification("启动失败", "请检查网络设置")
                Log.e(TAG, "❌ Web同步启动失败")
            }
        }

        // 监听网络状态
        registerNetworkCallback()
    }

    /**
     * 停止Web同步
     */
    private fun stopWebSync() {
        Log.d(TAG, "停止Web同步")

        webServerManager.stop()
        _pairingToken.value = null

        // 停止前台服务
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Web同步服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持Web同步服务运行，允许电脑浏览器访问"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 更新通知
     */
    private fun updateNotification(title: String, content: String) {
        val notification = createNotification(title, content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 创建通知
     */
    private fun createNotification(title: String, content: String): Notification {
        val intent = Intent(this, com.jishi.clipboard.ui.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * 设置网络回调
     */
    private fun setupNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "网络连接可用")
                handleNetworkChange()
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "网络连接断开")
                handleNetworkChange()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                handleNetworkChange()
            }
        }
    }

    /**
     * 注册网络监听
     */
    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, networkCallback)
            Log.d(TAG, "网络监听已注册")
        } catch (e: Exception) {
            Log.e(TAG, "注册网络监听失败", e)
        }
    }

    /**
     * 处理网络变化
     */
    private fun handleNetworkChange() {
        serviceScope.launch {
            if (webServerManager.isWifiConnected()) {
                if (!webServerManager.isRunning.value) {
                    Log.d(TAG, "WiFi重连，重启服务器")
                    startWebSync()
                }
            } else {
                if (webServerManager.isRunning.value) {
                    Log.d(TAG, "WiFi断开，停止服务器")
                    webServerManager.stop()
                    updateNotification("WiFi已断开", "服务已暂停")
                }
            }
        }
    }

    /**
     * 刷新配对码
     */
    fun refreshPairingToken(): String {
        val newToken = TokenManager.generateToken()
        _pairingToken.value = newToken

        // 更新通知
        val address = webServerManager.serverAddress.value
        if (address != null) {
            updateNotification(
                "Web同步运行中",
                "地址: https://$address\n配对码: $newToken"
            )
        }

        return newToken
    }
}
