package com.jishi.clipboard.network.dto

import kotlinx.serialization.Serializable

/**
 * 剪贴板数据传输对象
 */
@Serializable
data class ClipboardDto(
    val content: String,
    val timestamp: Long,
    val tags: List<String> = emptyList()
)

/**
 * 配对请求
 */
@Serializable
data class PairingRequest(
    val token: String
)

/**
 * API响应
 */
@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: String? = null
)

/**
 * WebSocket消息
 */
@Serializable
data class WebSocketMessage(
    val type: String,  // "clipboard_push", "ack", "error"
    val messageId: String? = null,
    val data: ClipboardDto? = null,
    val error: String? = null
)
