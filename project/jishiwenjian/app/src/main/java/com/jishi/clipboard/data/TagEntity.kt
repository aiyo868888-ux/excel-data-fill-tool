package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 剪贴板-标签关联表（多对多）
 */
@Entity(
    tableName = "clipboard_tag_relations",
    indices = [Index(value = ["clipboardId"]), Index(value = ["tagDefinitionId"])]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clipboardId: Long,
    val tagDefinitionId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
