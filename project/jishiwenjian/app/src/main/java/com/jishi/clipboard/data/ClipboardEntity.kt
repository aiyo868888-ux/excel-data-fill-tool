package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboards")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val type: String = "灵感", // 内容类型：灵感、启发、待办
    val images: String? = null, // JSON数组存储图片路径列表
    val metadata: String = "{}", // JSON格式存储额外信息（如待办的priority、status等）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
