package com.jishi.clipboard.data.json

import java.util.UUID

/**
 * 启发数据模型
 */
data class InsightItem(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
