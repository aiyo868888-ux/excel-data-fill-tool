package com.jishi.clipboard.data.json

import java.util.UUID

/**
 * 灵感数据模型
 */
data class InspirationItem(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
