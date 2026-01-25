package com.jishi.clipboard.repository

import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.ClipboardTagRelationDao
import com.jishi.clipboard.data.TagDao
import com.jishi.clipboard.data.TagDefinition
import com.jishi.clipboard.data.TagEntity
import com.jishi.clipboard.data.TagWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 标签管理 Repository
 */
@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao,
    private val relationDao: ClipboardTagRelationDao
) {

    // ========== 标签定义管理 ==========

    suspend fun getOrCreateTagDefinition(name: String): TagDefinition {
        val existing = tagDao.getTagDefinitionByName(name.trim())
        if (existing != null) {
            return existing
        }

        val newTag = TagDefinition(
            name = name.trim(),
            color = getRandomColor(),
            displayOrder = System.currentTimeMillis().toInt()
        )
        val id = tagDao.insertTagDefinition(newTag)
        return newTag.copy(id = id)
    }

    suspend fun updateTagDefinition(tag: TagDefinition) {
        tagDao.updateTagDefinition(tag)
    }

    suspend fun insertTagDefinition(tag: TagDefinition): Long {
        return tagDao.insertTagDefinition(tag)
    }

    suspend fun deleteTagDefinition(tagId: Long) {
        // 先删除所有关联
        relationDao.deleteRelationsForTag(tagId)
        // 再删除标签定义
        val tag = tagDao.getTagDefinitionById(tagId) ?: return
        tagDao.deleteTagDefinition(tag)
    }

    fun getAllTagDefinitions(): Flow<List<TagDefinition>> {
        return tagDao.getAllTagDefinitions()
    }

    fun getMostUsedTags(limit: Int = 10): Flow<List<TagDefinition>> {
        return tagDao.getMostUsedTags(limit)
    }

    suspend fun getTagDefinitionById(id: Long): TagDefinition? {
        return tagDao.getTagDefinitionById(id)
    }

    suspend fun getTagDefinitionByName(name: String): TagDefinition? {
        return tagDao.getTagDefinitionByName(name)
    }

    suspend fun incrementTagUseCount(tagId: Long) {
        tagDao.incrementUseCount(tagId)
    }

    // ========== 层级查询 ==========

    fun getRootTags(): Flow<List<TagDefinition>> {
        return tagDao.getRootTags()
    }

    fun getChildTags(parentId: Long): Flow<List<TagDefinition>> {
        return tagDao.getChildTags(parentId)
    }

    fun getAllTagsHierarchical(): Flow<List<TagDefinition>> {
        return tagDao.getAllTagsHierarchical()
    }

    suspend fun getChildTagsSync(parentId: Long?): List<TagDefinition> {
        return tagDao.getChildTagsSync(parentId)
    }

    // ========== 关联管理 ==========

    suspend fun getTagsForClipboard(clipboardId: Long): List<TagDefinition> {
        // 使用 kotlinx.coroutines.flow.first()
        return relationDao.getTagDefinitionsForClipboard(clipboardId)
            .first()
    }

    fun getTagsForClipboardFlow(clipboardId: Long): Flow<List<TagDefinition>> {
        return relationDao.getTagDefinitionsForClipboard(clipboardId)
    }

    fun getClipboardsByTag(tagDefinitionId: Long): Flow<List<ClipboardEntity>> {
        return relationDao.getClipboardsByTagDefinition(tagDefinitionId)
    }

    fun getAllTagsWithCount(): Flow<List<TagWithCount>> {
        return relationDao.getAllTagsWithCount()
    }

    // ========== 初始化默认标签 ==========

    suspend fun initDefaultTagsIfNeeded() {
        val existingTags = tagDao.getAllTagDefinitions()
            .first()
        if (existingTags.isEmpty()) {
            val defaultTags = listOf(
                "工作" to "#FF6B6B",
                "重要" to "#F39C12",
                "待办" to "#FF6B6B",      // 待办：红色（与图标一致）
                "灵感" to "#4ECDC4",      // 灵感：绿色（与图标一致）
                "启发" to "#45B7D1",      // 启发：蓝色（与图标一致）
                "学习" to "#3498DB",
                "生活" to "#98D8C8"
            )

            defaultTags.forEachIndexed { index, (name, color) ->
                val tag = TagDefinition(
                    name = name,
                    color = color,
                    displayOrder = index
                )
                tagDao.insertTagDefinition(tag)
            }
        }
    }

    private fun getRandomColor(): String {
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F39C12", "#9B59B6", "#3498DB")
        return colors.random()
    }

    /**
     * 获取或创建内容类型标签
     * 确保三种类型（灵感、启发、待办）的标签存在，如果不存在则创建
     */
    suspend fun getOrCreateContentTypeTag(contentType: String): TagDefinition {
        // 检查标签是否已存在
        val existing = tagDao.getTagDefinitionByName(contentType)
        if (existing != null) {
            return existing
        }

        // 根据类型确定颜色
        val color = when (contentType) {
            "灵感" -> "#4ECDC4"  // 绿色
            "启发" -> "#45B7D1"  // 蓝色
            "待办" -> "#FF6B6B"  // 红色
            else -> getRandomColor()
        }

        // 创建新标签
        val newTag = TagDefinition(
            name = contentType,
            color = color,
            displayOrder = -1  // 内容类型标签排在前面
        )
        val id = tagDao.insertTagDefinition(newTag)
        return newTag.copy(id = id)
    }
}
