package com.jishi.clipboard.network.websocket

import android.util.Log
import com.jishi.clipboard.network.dto.WebSocketMessage
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket连接管理器
 * 管理所有活跃的WebSocket连接
 */
object WebSocketManager {
    private const val TAG = "WebSocketManager"
    private val json = Json { ignoreUnknownKeys = true }

    // 存储Token对应的WebSocket会话
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    // 存储WebSocket会话对应的Token（反向映射）
    private val tokenMap = ConcurrentHashMap<WebSocketSession, String>()

    /**
     * 添加新的WebSocket连接
     */
    fun addSession(token: String, session: WebSocketSession) {
        sessions[token] = session
        tokenMap[session] = token
        Log.d(TAG, "新增WebSocket连接: token=$token, 当前连接数=${sessions.size}")
    }

    /**
     * 移除WebSocket连接
     */
    fun removeSession(session: WebSocketSession) {
        val token = tokenMap.remove(session)
        if (token != null) {
            sessions.remove(token)
            Log.d(TAG, "移除WebSocket连接: token=$token, 剩余连接数=${sessions.size}")
        }
    }

    /**
     * 广播消息到所有连接的设备
     */
    suspend fun broadcast(message: String) {
        var successCount = 0
        var failureCount = 0

        sessions.values.forEach { session ->
            try {
                session.send(Frame.Text(message))
                successCount++
            } catch (e: ClosedSendChannelException) {
                Log.w(TAG, "WebSocket已关闭，移除连接")
                removeSession(session)
                failureCount++
            } catch (e: Exception) {
                Log.e(TAG, "发送消息失败", e)
                failureCount++
            }
        }

        Log.d(TAG, "广播完成: 成功=$successCount, 失败=$failureCount")
    }

    /**
     * 发送剪贴板推送消息到所有设备
     */
    suspend fun broadcastClipboard(content: String, timestamp: Long, tags: List<String>) {
        val message = WebSocketMessage(
            type = "clipboard_push",
            messageId = java.util.UUID.randomUUID().toString(),
            data = com.jishi.clipboard.network.dto.ClipboardDto(
                content = content,
                timestamp = timestamp,
                tags = tags
            )
        )

        val jsonMessage = json.encodeToString(message)
        Log.d(TAG, "广播剪贴板内容: ${content.take(50)}...")
        broadcast(jsonMessage)
    }

    /**
     * 发送消息到指定Token的设备
     */
    suspend fun sendToToken(token: String, message: String): Boolean {
        val session = sessions[token] ?: run {
            Log.w(TAG, "Token对应的会话不存在: $token")
            return false
        }

        return try {
            session.send(Frame.Text(message))
            Log.d(TAG, "消息已发送到token=$token")
            true
        } catch (e: Exception) {
            Log.e(TAG, "发送消息失败: token=$token", e)
            removeSession(session)
            false
        }
    }

    /**
     * 获取当前连接数
     */
    fun getConnectionCount(): Int = sessions.size

    /**
     * 获取所有活跃的Token
     */
    fun getActiveTokens(): Set<String> = sessions.keys

    /**
     * 检查指定Token是否有活跃连接
     */
    fun hasActiveConnection(token: String): Boolean = sessions.containsKey(token)
}
