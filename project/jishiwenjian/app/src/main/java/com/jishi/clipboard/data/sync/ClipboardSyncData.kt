package com.jishi.clipboard.data.sync

import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.data.TagEntity
import com.jishi.clipboard.data.Reminder
import kotlinx.serialization.Serializable

/**
 * 剪贴板同步数据根对象
 */
@Serializable
data class ClipboardSyncData(
    val version: String = "1.0",
    val exportedAt: Long = System.currentTimeMillis(),
    val deviceId: String,
    val deviceName: String,
    val entities: SyncEntities,
    val metadata: SyncMetadata
)

/**
 * 同步实体集合
 */
@Serializable
data class SyncEntities(
    val clipboards: List<ClipboardEntityDto>,
    val tagDefinitions: List<TagDefinitionDto>,
    val reminders: List<ReminderDto>,
    val relations: List<TagRelationDto>
)

/**
 * 同步元数据
 */
@Serializable
data class SyncMetadata(
    val dbVersion: Int,
    val appVersion: String,
    val syncChecksum: String,
    val lastModified: Long
)

/**
 * 剪贴板实体 DTO
 */
@Serializable
data class ClipboardEntityDto(
    val id: Long,
    val content: String,
    val type: String = "灵感", // 内容类型：灵感、启发、待办
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 标签定义 DTO
 */
@Serializable
data class TagDefinitionDto(
    val id: Long,
    val name: String,
    val color: String,
    val displayOrder: Int,
    val useCount: Int,
    val createdAt: Long
)

/**
 * 提醒实体 DTO
 */
@Serializable
data class ReminderDto(
    val id: Long,
    val clipboardId: Long,
    val timestamp: Long,
    val type: String,
    val originalText: String,
    val content: String,
    val createdAt: Long,
    val isNotified: Boolean
)

/**
 * 标签关联 DTO
 */
@Serializable
data class TagRelationDto(
    val id: Long,
    val clipboardId: Long,
    val tagDefinitionId: Long,
    val createdAt: Long
)

/**
 * 数据转换扩展函数
 */

// 从实体转换为 DTO
fun ClipboardEntity.toDto() = ClipboardEntityDto(
    id = id,
    content = content,
    type = type,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TagDefinition.toDto() = TagDefinitionDto(
    id = id,
    name = name,
    color = color,
    displayOrder = displayOrder,
    useCount = useCount,
    createdAt = createdAt
)

fun Reminder.toDto() = ReminderDto(
    id = id,
    clipboardId = clipboardId,
    timestamp = timestamp,
    type = type,
    originalText = originalText,
    content = content,
    createdAt = createdAt,
    isNotified = isNotified
)

fun TagEntity.toDto() = TagRelationDto(
    id = id,
    clipboardId = clipboardId,
    tagDefinitionId = tagDefinitionId,
    createdAt = createdAt
)

// 从 DTO 转换为实体
fun ClipboardEntityDto.toEntity() = ClipboardEntity(
    id = id,
    content = content,
    type = type,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TagDefinitionDto.toEntity() = TagDefinition(
    id = id,
    name = name,
    color = color,
    displayOrder = displayOrder,
    useCount = useCount,
    createdAt = createdAt
)

fun ReminderDto.toEntity() = Reminder(
    id = id,
    clipboardId = clipboardId,
    timestamp = timestamp,
    type = type,
    originalText = originalText,
    content = content,
    createdAt = createdAt,
    isNotified = isNotified
)

fun TagRelationDto.toEntity() = TagEntity(
    id = id,
    clipboardId = clipboardId,
    tagDefinitionId = tagDefinitionId,
    createdAt = createdAt
)
