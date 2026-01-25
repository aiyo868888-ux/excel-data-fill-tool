package com.jishi.clipboard.network.server

import android.util.Log
import com.jishi.clipboard.network.dto.ApiResponse
import com.jishi.clipboard.network.dto.PairingRequest
import com.jishi.clipboard.network.dto.WebSocketMessage
import com.jishi.clipboard.network.security.TokenManager
import com.jishi.clipboard.network.websocket.WebSocketManager
import com.jishi.clipboard.repository.ClipboardRepository
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.http.content.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration

/**
 * Ktor Web服务器（HTTP模式）
 * 注意：生产环境应使用HTTPS
 */
object KtorWebServer {
    private const val TAG = "KtorWebServer"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 创建服务器
     */
    fun create(
        context: android.content.Context,
        port: Int,
        clipboardRepository: ClipboardRepository
    ): NettyApplicationEngine {
        Log.d(TAG, "创建Ktor服务器(HTTP模式), 端口=$port")

        return embeddedServer(Netty, port = port, host = "0.0.0.0") {
            // WebSocket插件
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
            }

            // 路由配置
            routing {
                // 配对路由
                pairingRoutes()

                // 剪贴板路由
                clipboardRoutes(clipboardRepository)

                // WebSocket路由
                webSocketRoutes()

                // 静态文件服务
                staticResources("/", "web") {
                    default("index.html")
                }
            }
        }
    }

    /**
     * 配对路由
     */
    private fun Routing.pairingRoutes() {
        route("/api") {
            post("/pair") {
                try {
                    val request = call.receive<PairingRequest>()
                    Log.d(TAG, "收到配对请求: token=${request.token}")

                    if (TokenManager.validateToken(request.token)) {
                        call.respond(ApiResponse(success = true, message = "配对成功"))
                        Log.d(TAG, "配对验证成功")
                    } else {
                        call.respond(ApiResponse(success = false, message = "配对码无效或已过期"))
                        Log.w(TAG, "配对验证失败")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "配对请求处理失败", e)
                    call.respond(ApiResponse(success = false, message = "服务器错误"))
                }
            }

            // 服务器状态
            get("/status") {
                val status = mapOf(
                    "running" to true,
                    "connections" to WebSocketManager.getConnectionCount(),
                    "activeTokens" to TokenManager.getActiveTokenCount(),
                    "mode" to "HTTP (生产环境请使用HTTPS)"
                )
                call.respond(status)
            }
        }
    }

    /**
     * 剪贴板API路由
     */
    private fun Routing.clipboardRoutes(repository: ClipboardRepository) {
        route("/api/clipboard") {
            // 获取剪贴板列表
            get("/") {
                try {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 1000
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    Log.d(TAG, "获取剪贴板列表, limit=$limit, offset=$offset")

                    // 从数据库获取剪贴板数据（使用协程）
                    val clipboards = kotlinx.coroutines.runBlocking {
                        repository.getAllClipboards().first().drop(offset).take(limit).toList()
                    }

                    // 转换为DTO格式
                    val dtos = clipboards.map { entity ->
                        mapOf(
                            "id" to entity.id,
                            "content" to entity.content,
                            "createdAt" to entity.createdAt,
                            "updatedAt" to entity.updatedAt
                        )
                    }

                    call.respond(mapOf(
                        "success" to true,
                        "data" to dtos,
                        "count" to dtos.size,
                        "total" to clipboards.size
                    ))

                } catch (e: Exception) {
                    Log.e(TAG, "获取剪贴板列表失败", e)
                    call.respond(ApiResponse(success = false, message = "获取失败: ${e.message}"))
                }
            }

            // 导出为Markdown格式
            get("/export/markdown") {
                try {
                    Log.d(TAG, "导出Markdown")

                    // 获取所有剪贴板数据
                    val clipboards = kotlinx.coroutines.runBlocking {
                        repository.getAllClipboards().first().toList()
                    }

                    // 转换为Markdown
                    val markdown = convertToMarkdown(clipboards)

                    // 返回文件
                    call.respondText(markdown, io.ktor.http.ContentType.Text.Plain)
                    call.response.headers.append(
                        io.ktor.http.HttpHeaders.ContentDisposition,
                        "attachment; filename=\"clipboard_export_${System.currentTimeMillis()}.md\""
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "导出Markdown失败", e)
                    call.respond(ApiResponse(success = false, message = "导出失败: ${e.message}"))
                }
            }
        }
    }

    /**
     * 将剪贴板数据转换为Markdown格式
     */
    private fun convertToMarkdown(clipboards: List<com.jishi.clipboard.data.ClipboardEntity>): String {
        val sb = StringBuilder()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

        // 标题
        sb.appendLine("# 剪贴板导出")
        sb.appendLine()
        sb.appendLine("**导出时间**: ${sdf.format(java.util.Date())}")
        sb.appendLine("**记录数量**: ${clipboards.size}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        // 每条记录
        clipboards.forEachIndexed { index, clipboard ->
            // 标题（使用内容的前50个字符）
            val title = clipboard.content.take(50).replace("\n", " ")
            sb.appendLine("## ${index + 1}. $title")

            // 时间戳
            val dateStr = sdf.format(java.util.Date(clipboard.createdAt))
            sb.appendLine()
            sb.appendLine("**时间**: $dateStr")

            // 内容
            sb.appendLine()
            sb.appendLine("### 内容")
            sb.appendLine()
            sb.appendLine("```")
            sb.appendLine(clipboard.content)
            sb.appendLine("```")
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * WebSocket路由
     */
    private fun Routing.webSocketRoutes() {
        webSocket("/clipboard") {
            val token = call.request.queryParameters["token"]
            Log.d(TAG, "WebSocket连接请求: token=$token")

            if (token == null || !TokenManager.validateToken(token)) {
                Log.w(TAG, "WebSocket连接被拒绝: token无效")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                return@webSocket
            }

            // 添加会话
            WebSocketManager.addSession(token, this)
            Log.d(TAG, "WebSocket连接建立: token=$token")

            try {
                // 发送欢迎消息
                val welcomeMsg = WebSocketMessage(
                    type = "connected",
                    messageId = null,
                    data = null,
                    error = null
                )
                send(Frame.Text(json.encodeToString(welcomeMsg)))

                // 监听消息
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            Log.d(TAG, "收到消息: $text")
                            try {
                                val msg = json.decodeFromString<WebSocketMessage>(text)
                                handleWebSocketMessage(msg, token)
                            } catch (e: Exception) {
                                Log.e(TAG, "解析消息失败", e)
                            }
                        }
                        is Frame.Close -> {
                            Log.d(TAG, "客户端请求关闭")
                        }
                        else -> {}
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                Log.d(TAG, "WebSocket通道关闭")
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket异常", e)
            } finally {
                WebSocketManager.removeSession(this)
                Log.d(TAG, "WebSocket已关闭: token=$token")
            }
        }
    }

    /**
     * 处理WebSocket消息
     */
    private suspend fun handleWebSocketMessage(message: WebSocketMessage, token: String) {
        when (message.type) {
            "ack" -> Log.d(TAG, "收到ACK: ${message.messageId}")
            "ping" -> {
                // 响应心跳
                val pong = WebSocketMessage(
                    type = "pong",
                    messageId = message.messageId,
                    data = null,
                    error = null
                )
                WebSocketManager.sendToToken(token, json.encodeToString(pong))
            }
            else -> Log.w(TAG, "未知消息类型: ${message.type}")
        }
    }
}
