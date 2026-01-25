package com.jishi.clipboard.network.server

import android.content.Context
import android.util.Log
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.repository.ClipboardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Web服务器管理器
 * 负责服务器的启动、停止和生命周期管理
 */
@Singleton
class WebServerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardRepository: ClipboardRepository
) {
    companion object {
        private const val TAG = "WebServerManager"
        private const val DEFAULT_PORT = 8443
    }

    private var server: NettyApplicationEngine? = null
    private val _isRunning = MutableStateFlow(false)
    private val _serverAddress = MutableStateFlow<String?>(null)

    /**
     * 服务器运行状态
     */
    val isRunning: StateFlow<Boolean> = _isRunning

    /**
     * 服务器地址（IP:PORT）
     */
    val serverAddress: StateFlow<String?> = _serverAddress

    /**
     * 启动服务器
     * @param port 端口号，默认8443
     * @return true如果启动成功
     */
    fun start(port: Int = DEFAULT_PORT): Boolean {
        if (server != null) {
            Log.w(TAG, "服务器已在运行")
            return true
        }

        return try {
            Log.d(TAG, "正在启动服务器, 端口=$port")

            server = KtorWebServer.create(
                context = context,
                port = port,
                clipboardRepository = clipboardRepository
            ).also {
                it.start(wait = false)
            }

            // 获取本机IP地址
            val ipAddress = getLocalIpAddress()
            val address = "$ipAddress:$port"
            _serverAddress.value = address
            _isRunning.value = true

            Log.i(TAG, "✅ 服务器启动成功: https://$address")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ 服务器启动失败", e)
            _isRunning.value = false
            _serverAddress.value = null
            server = null
            false
        }
    }

    /**
     * 停止服务器
     */
    fun stop() {
        server?.let {
            Log.d(TAG, "正在停止服务器")
            try {
                it.stop(1000, 5000) // gracefulTimeout, timeout
                Log.i(TAG, "服务器已停止")
            } catch (e: Exception) {
                Log.e(TAG, "停止服务器时出错", e)
            } finally {
                server = null
                _isRunning.value = false
                _serverAddress.value = null
            }
        }
    }

    /**
     * 重启服务器
     */
    fun restart(port: Int = DEFAULT_PORT): Boolean {
        stop()
        return start(port)
    }

    /**
     * 推送剪贴板内容到所有连接的设备
     */
    suspend fun broadcastClipboard(clipboard: ClipboardEntity) {
        if (!isRunning.value) {
            Log.w(TAG, "服务器未运行，无法推送")
            return
        }

        // 获取标签
        val tags = emptyList<String>() // TODO: 从数据库获取标签

        com.jishi.clipboard.network.websocket.WebSocketManager.broadcastClipboard(
            content = clipboard.content,
            timestamp = clipboard.createdAt,
            tags = tags
        )

        Log.d(TAG, "已推送剪贴板内容到${com.jishi.clipboard.network.websocket.WebSocketManager.getConnectionCount()}台设备")
    }

    /**
     * 获取本机IP地址
     */
    private fun getLocalIpAddress(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress

            // 将int IP转换为字符串
            val bytes = byteArrayOf(
                (ipAddress and 0xFF).toByte(),
                (ipAddress shr 8 and 0xFF).toByte(),
                (ipAddress shr 16 and 0xFF).toByte(),
                (ipAddress shr 24 and 0xFF).toByte()
            )

            java.net.InetAddress.getByAddress(bytes).hostAddress ?: "0.0.0.0"
        } catch (e: Exception) {
            Log.e(TAG, "获取IP地址失败", e)
            "0.0.0.0"
        }
    }

    /**
     * 检查WiFi是否连接
     */
    fun isWifiConnected(): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            wifiManager.isWifiEnabled && wifiManager.connectionInfo.networkId != -1
        } catch (e: Exception) {
            Log.e(TAG, "检查WiFi状态失败", e)
            false
        }
    }
}
