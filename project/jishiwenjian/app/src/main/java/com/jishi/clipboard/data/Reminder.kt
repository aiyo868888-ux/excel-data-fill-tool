package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 提醒实体
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val clipboardId: Long,          // 关联的剪贴板记录ID
    val timestamp: Long,             // 提醒时间戳
    val type: String,                // 提醒类型：NOTIFICATION, ALARM, FULLSCREEN
    val originalText: String,        // 原始时间文本
    val content: String,             // 提醒内容（剪贴板内容）
    val createdAt: Long = System.currentTimeMillis(),
    val isNotified: Boolean = false  // 是否已通知
)
