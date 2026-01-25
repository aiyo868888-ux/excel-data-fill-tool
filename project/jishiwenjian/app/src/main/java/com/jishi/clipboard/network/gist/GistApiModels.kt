package com.jishi.clipboard.network.gist

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Gist API 请求模型
 */
@Serializable
data class GistRequest(
    val description: String = "jishiwenjian sync data",
    val files: Map<String, GistFile>,
    val public: Boolean = false
)

/**
 * Gist 文件内容
 */
@Serializable
data class GistFile(
    val content: String,
    val filename: String? = null
)

/**
 * Gist API 响应模型
 */
@Serializable
data class GistResponse(
    val id: String,
    val description: String? = null,
    val files: Map<String, GistFileDetail>,
    @SerialName("public")
    val isPublic: Boolean = false,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val url: String? = null
)

/**
 * Gist 文件详细信息
 */
@Serializable
data class GistFileDetail(
    val filename: String,
    val type: String? = null,
    val language: String? = null,
    val raw_url: String? = null,
    val size: Int = 0,
    val content: String? = null
)

/**
 * 同步结果封装
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class Failed(val message: String, val exception: Throwable? = null) : SyncResult()
    data class Conflict(val localTime: Long, val remoteTime: Long) : SyncResult()
    data class Cancelled(val message: String) : SyncResult()
}
