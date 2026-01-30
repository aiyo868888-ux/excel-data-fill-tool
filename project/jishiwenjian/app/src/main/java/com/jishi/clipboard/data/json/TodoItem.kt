package com.jishi.clipboard.data.json

import java.util.UUID

/**
 * 待办数据模型
 */
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val task: String,
    val tags: List<String> = emptyList(),
    val dueTimestamp: Long? = null,
    val status: String = "PENDING", // PENDING 或 COMPLETED
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
