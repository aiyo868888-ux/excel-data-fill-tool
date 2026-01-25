package com.jishi.clipboard.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * 标签定义表 - 全局标签管理（支持层级结构）
 * parentId: 父标签 ID，null 表示根标签（第一层）
 */
@Entity(tableName = "tag_definitions")
@Parcelize
data class TagDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String = "#4ECDC4",
    val displayOrder: Int = 0,
    val useCount: Int = 0,
    val parentId: Long? = null,
    val level: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

