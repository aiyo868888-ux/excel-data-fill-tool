package com.jishi.clipboard.repository

import com.jishi.clipboard.data.ClipboardDao
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

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao,
    private val tagDao: TagDao,
    private val relationDao: ClipboardTagRelationDao
) {

    suspend fun saveClipboard(content: String, tags: List<String>): Long {
        android.util.Log.d("ClipboardRepository", "saveClipboard: 内容长度=${content.length}, 标签数量=${tags.size}")
        val clipboard = ClipboardEntity(
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val clipboardId = clipboardDao.insertClipboard(clipboard)

        // 处理标签（统一策略：自动创建标签）
        tags.forEach { tagName ->
            if (tagName.isNotBlank()) {
                // 获取或创建标签定义
                var tagDef = tagDao.getTagDefinitionByName(tagName.trim())
                if (tagDef == null) {
                    val newTagDef = TagDefinition(
                        name = tagName.trim(),
                        color = getRandomColor()
                    )
                    val tagDefId = tagDao.insertTagDefinition(newTagDef)
                    tagDef = newTagDef.copy(id = tagDefId)
                    android.util.Log.d("ClipboardRepository", "创建新标签: $tagName")
                }

                // 创建关联
                val relation = TagEntity(
                    clipboardId = clipboardId,
                    tagDefinitionId = tagDef.id
                )
                relationDao.insertRelation(relation)

                // 增加使用次数
                tagDao.incrementUseCount(tagDef.id)
            }
        }

        android.util.Log.d("ClipboardRepository", "saveClipboard 完成, clipboardId=$clipboardId")
        return clipboardId
    }

    suspend fun deleteClipboard(clipboard: ClipboardEntity) {
        relationDao.deleteRelationsForClipboard(clipboard.id)
        clipboardDao.deleteClipboard(clipboard)
    }

    fun getAllClipboards(): Flow<List<ClipboardEntity>> {
        return clipboardDao.getAllClipboards()
    }

    fun searchClipboards(query: String): Flow<List<ClipboardEntity>> {
        return clipboardDao.searchClipboards(query)
    }

    suspend fun getClipboardById(id: Long): ClipboardEntity? {
        return clipboardDao.getClipboardById(id)
    }

    suspend fun getTagsForClipboard(clipboardId: Long): List<TagDefinition> {
        return relationDao.getTagDefinitionsForClipboard(clipboardId)
            .first()
    }

    fun getTagsForClipboardFlow(clipboardId: Long): Flow<List<TagDefinition>> {
        return relationDao.getTagDefinitionsForClipboard(clipboardId)
    }

    fun getRecentClipboards(limit: Int): Flow<List<ClipboardEntity>> {
        return clipboardDao.getRecentClipboards(limit)
    }

    suspend fun deleteAllClipboards() {
        // 删除所有剪贴板会级联删除关联（通过 Room 的外键或手动删除）
        relationDao.deleteRelationsForClipboard(-1) // 删除所有关联
        clipboardDao.deleteAll()
    }

    suspend fun updateClipboard(id: Long, content: String, tags: List<String>) {
        android.util.Log.d("ClipboardRepository", "updateClipboard: id=$id, 内容长度=${content.length}, 标签数量=${tags.size}")

        // 1. 删除旧的标签关联
        relationDao.deleteRelationsForClipboard(id)
        android.util.Log.d("ClipboardRepository", "删除旧标签完成")

        // 2. 更新内容
        clipboardDao.updateClipboardContent(id, content, System.currentTimeMillis())
        android.util.Log.d("ClipboardRepository", "更新内容完成")

        // 3. 插入新的标签关联
        tags.forEach { tagName ->
            if (tagName.isNotBlank()) {
                // 获取或创建标签定义
                var tagDef = tagDao.getTagDefinitionByName(tagName.trim())
                if (tagDef == null) {
                    val newTagDef = TagDefinition(
                        name = tagName.trim(),
                        color = getRandomColor()
                    )
                    val tagDefId = tagDao.insertTagDefinition(newTagDef)
                    tagDef = newTagDef.copy(id = tagDefId)
                }

                // 创建关联
                val relation = TagEntity(
                    clipboardId = id,
                    tagDefinitionId = tagDef.id
                )
                relationDao.insertRelation(relation)
                android.util.Log.d("ClipboardRepository", "插入标签: $tagName")
            }
        }
        android.util.Log.d("ClipboardRepository", "updateClipboard 完成")
    }

    private fun getRandomColor(): String {
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F39C12", "#9B59B6", "#3498DB")
        return colors.random()
    }

    fun getAllTags(): Flow<List<TagWithCount>> {
        return relationDao.getAllTagsWithCount()
    }

    suspend fun getTagDefinitionByName(name: String): TagDefinition? {
        return tagDao.getTagDefinitionByName(name)
    }

    fun getClipboardsByTagDefinition(tagDefinitionId: Long): Flow<List<ClipboardEntity>> {
        return relationDao.getClipboardsByTagDefinition(tagDefinitionId)
    }

    /**
     * 更新剪贴板的标签关联（不更新内容）
     * @param clipboardId 剪贴板ID
     * @param newTags 新的标签列表（TagDefinition对象）
     */
    suspend fun updateClipboardTags(clipboardId: Long, newTags: List<TagDefinition>) {
        android.util.Log.d("ClipboardRepository", "updateClipboardTags: id=$clipboardId, 新标签数量=${newTags.size}")

        // 1. 删除旧的标签关联
        relationDao.deleteRelationsForClipboard(clipboardId)

        // 2. 添加新的标签关联
        newTags.forEach { tagDef ->
            val relation = TagEntity(
                clipboardId = clipboardId,
                tagDefinitionId = tagDef.id
            )
            relationDao.insertRelation(relation)
            android.util.Log.d("ClipboardRepository", "添加标签: ${tagDef.name}")
        }

        android.util.Log.d("ClipboardRepository", "updateClipboardTags 完成")
    }

    // ===== 同步方法（用于 Markdown 导出）=====

    /**
     * 同步获取所有剪贴板记录
     */
    suspend fun getAllClipboardsSync(): List<ClipboardEntity> {
        return clipboardDao.getAllClipboards().first()
    }

    /**
     * 同步获取所有标签定义
     */
    suspend fun getAllTagDefinitionsSync(): List<TagDefinition> {
        return tagDao.getAllTagDefinitions().first()
    }

    /**
     * 同步获取所有标签关联
     */
    suspend fun getAllTagsSync(): List<TagEntity> {
        return relationDao.getAllRelations().first()
    }
}
