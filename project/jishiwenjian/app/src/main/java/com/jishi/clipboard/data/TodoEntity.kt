package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 待办事项实体
 */
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val task: String,                    // 任务描述（已提取时间信息）
    val rawContent: String,              // 原始输入（用户输入的完整文本）
    val dueTimestamp: Long?,             // 截止时间戳（可能为 null）
    val originalTimeText: String?,       // 原始时间文本（如"明天下午3点"）

    val status: String = "PENDING",      // PENDING, COMPLETED, CANCELLED
    val tags: String = "",               // 标签（逗号分隔，如"工作,紧急"）

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null        // 完成时间
)
