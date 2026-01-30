package com.jishi.clipboard.repository

import com.google.gson.Gson
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.json.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一内容仓库
 * 为 Fragment 提供统一的数据访问接口
 * 通过 ClipboardEntity.type 字段筛选不同类型内容
 * 标签系统（TagDefinition）是独立的分类维度
 */
@Singleton
class UnifiedContentRepository @Inject constructor(
    private val clipboardRepository: ClipboardRepository
) {
    private val gson = Gson()
    /**
     * 初始化（保留标签系统兼容性）
     */
    suspend fun initialize() {
        Timber.d("UnifiedContentRepository 初始化 - 内容类型通过 type 字段筛选")
    }

    /**
     * 获取所有灵感（通过 type 字段筛选）
     */
    suspend fun getInspirations(): List<ClipboardEntity> {
        return try {
            clipboardRepository.getClipboardsByType("灵感").first()
        } catch (e: Exception) {
            Timber.e(e, "获取灵感失败")
            emptyList()
        }
    }

    /**
     * 监听所有灵感的变化（Flow）
     */
    fun observeInspirations(): Flow<List<ClipboardEntity>> {
        Timber.d("observeInspirations 被调用 - 筛选 type='灵感'")
        return clipboardRepository.getClipboardsByType("灵感")
    }

    /**
     * 获取所有启发（通过 type 字段筛选）
     */
    suspend fun getInsights(): List<ClipboardEntity> {
        return try {
            clipboardRepository.getClipboardsByType("启发").first()
        } catch (e: Exception) {
            Timber.e(e, "获取启发失败")
            emptyList()
        }
    }

    /**
     * 监听所有启发的变化（Flow）
     */
    fun observeInsights(): Flow<List<ClipboardEntity>> {
        Timber.d("observeInsights 被调用 - 筛选 type='启发'")
        return clipboardRepository.getClipboardsByType("启发")
    }

    /**
     * 获取所有待办（通过 type 字段筛选）
     */
    suspend fun getTodos(): List<ClipboardEntity> {
        return try {
            clipboardRepository.getClipboardsByType("待办").first()
        } catch (e: Exception) {
            Timber.e(e, "获取待办失败")
            emptyList()
        }
    }

    /**
     * 监听所有待办的变化（Flow）
     * 排序规则：
     * 1. 未完成按优先级分组（HIGH > MEDIUM > LOW）
     * 2. 同优先级内按创建时间降序排列（最新的在上面）
     * 3. 已完成放在最后，按创建时间降序排列（最新的在上面）
     */
    fun observeTodos(): Flow<List<ClipboardEntity>> {
        Timber.d("observeTodos 被调用 - 筛选 type='待办'")
        return clipboardRepository.getClipboardsByType("待办")
            .map { entities ->
                // 预先解析所有 metadata，避免多次解析
                entities.map { entity ->
                    val metadataMap = parseMetadata(entity.metadata)
                    val status = metadataMap["status"] as? String ?: "PENDING"
                    val priority = metadataMap["priority"] as? String ?: "MEDIUM"
                    val priorityOrder = when (priority) {
                        "HIGH" -> 0
                        "MEDIUM" -> 1
                        "LOW" -> 2
                        else -> 1
                    }
                    // Triple: (完成状态, 优先级顺序, 创建时间取反) -> entity
                    // 创建时间取反：Long.MAX_VALUE - createdAt，这样大的时间戳会变成小的值，排序时会在前面
                    Triple(
                        first = status == "COMPLETED",  // false(0) 在前, true(1) 在后
                        second = priorityOrder,            // HIGH(0) > MEDIUM(1) > LOW(2)
                        third = Long.MAX_VALUE - entity.createdAt  // 时间戳取反，新的在前
                    ) to entity
                }.sortedWith(compareBy(
                    { it.first.first },   // 完成状态（升序：未完成在前）
                    { it.first.second },  // 优先级（升序：HIGH在前）
                    { it.first.third }    // 创建时间取反（升序：新的在前）
                )).map { it.second }
            }
    }

    /**
     * 从内容中提取标签
     */
    private fun extractTagsFromContent(content: String): List<String> {
        val tagPattern = Regex("#([\\u4e00-\\u9fa5a-zA-Z0-9_]+)")
        return tagPattern.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    /**
     * 删除操作
     */
    suspend fun deleteInspiration(id: Long) {
        clipboardRepository.deleteClipboard(id)
    }

    suspend fun deleteInsight(id: Long) {
        clipboardRepository.deleteClipboard(id)
    }

    suspend fun deleteTodo(id: Long) {
        clipboardRepository.deleteClipboard(id)
    }

    /**
     * 更新待办状态
     */
    suspend fun updateTodoStatus(id: Long, status: String) {
        try {
            val entity = clipboardRepository.getClipboardById(id)
            if (entity != null) {
                // 解析现有 metadata
                val metadataMap = try {
                    gson.fromJson(entity.metadata, Map::class.java) as? Map<String, Any> ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                // 更新 status
                val updatedMetadata = metadataMap.toMutableMap()
                updatedMetadata["status"] = status
                if (status == "COMPLETED") {
                    updatedMetadata["completedAt"] = System.currentTimeMillis()
                } else {
                    updatedMetadata.remove("completedAt")
                }

                // 保存回数据库（包含 metadata）
                clipboardRepository.updateClipboard(
                    id = id,
                    content = entity.content,
                    tags = emptyList(), // 标签从内容解析
                    type = entity.type,
                    images = null, // 图片不变
                    metadata = gson.toJson(updatedMetadata)
                )

                Timber.d("更新待办状态成功: id=$id, status=$status")
            }
        } catch (e: Exception) {
            Timber.e(e, "更新待办状态失败: id=$id")
            throw e
        }
    }

    /**
     * 更新待办优先级
     */
    suspend fun updateTodoPriority(id: Long, priority: String) {
        try {
            val entity = clipboardRepository.getClipboardById(id)
            if (entity != null) {
                // 解析现有 metadata
                val metadataMap = try {
                    gson.fromJson(entity.metadata, Map::class.java) as? Map<String, Any> ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }

                // 更新 priority
                val updatedMetadata = metadataMap.toMutableMap()
                updatedMetadata["priority"] = priority

                // 保存回数据库（包含 metadata）
                clipboardRepository.updateClipboard(
                    id = id,
                    content = entity.content,
                    tags = emptyList(), // 标签从内容解析
                    type = entity.type,
                    images = null,
                    metadata = gson.toJson(updatedMetadata)
                )

                Timber.d("更新待办优先级成功: id=$id, priority=$priority")
            }
        } catch (e: Exception) {
            Timber.e(e, "更新待办优先级失败: id=$id")
            throw e
        }
    }

    /**
     * 解析 metadata JSON 字符串
     */
    private fun parseMetadata(metadata: String): Map<String, Any> {
        return try {
            gson.fromJson(metadata, Map::class.java) as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 将 ClipboardEntity 转换为 TodoItem
     */
    fun clipboardToTodoItem(entity: ClipboardEntity, tags: List<String> = emptyList()): TodoItem {
        val metadataMap = parseMetadata(entity.metadata)

        val status = metadataMap["status"] as? String ?: "PENDING"
        val priority = metadataMap["priority"] as? String ?: "MEDIUM"
        val dueTimestamp = (metadataMap["dueTimestamp"] as? Double)?.toLong()
        val completedAt = (metadataMap["completedAt"] as? Double)?.toLong()

        Timber.d("========== clipboardToTodoItem ==========")
        Timber.d("entity.metadata=${entity.metadata}")
        Timber.d("解析后: status=$status, priority=$priority, dueTimestamp=$dueTimestamp")
        Timber.d("========================================")

        // 使用 TagParser 移除内容中的标签
        val taskWithoutTags = com.jishi.clipboard.utils.TagParser.removeTags(entity.content)

        return TodoItem(
            id = entity.id.toString(),
            task = taskWithoutTags,
            tags = tags, // 使用传入的标签列表
            dueTimestamp = dueTimestamp,
            status = status,
            priority = priority,
            completedAt = completedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * 追加内容到笔记
     */
    suspend fun appendContent(id: Long, appendedContent: String) {
        val entity = clipboardRepository.getClipboardById(id) ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        val newContent = "${entity.content}\n\n---\n✍️ $timestamp\n$appendedContent"

        // 标签从内容解析
        clipboardRepository.updateClipboard(
            id = id,
            content = newContent,
            tags = emptyList(), // 标签从内容解析
            type = entity.type,
            images = com.jishi.clipboard.util.ImageUtils.parseImagesFromJson(entity.images)
        )
    }
}
