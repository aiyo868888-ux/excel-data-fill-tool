package com.jishi.clipboard.repository

import com.jishi.clipboard.data.ClipboardDao
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.data.TagEntity
import com.jishi.clipboard.data.TagWithCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao
) {

    suspend fun saveClipboard(content: String, tags: List<String>): Long {
        android.util.Log.d("ClipboardRepository", "saveClipboard: 内容长度=${content.length}, 标签数量=${tags.size}")
        val clipboard = ClipboardEntity(
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val result = clipboardDao.insertClipboardWithTags(clipboard, tags)
        android.util.Log.d("ClipboardRepository", "saveClipboard 完成, clipboardId=$result")
        return result
    }

    suspend fun deleteClipboard(clipboard: ClipboardEntity) {
        clipboardDao.deleteTagsForClipboard(clipboard.id)
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

    fun getTagsForClipboard(clipboardId: Long): Flow<List<TagEntity>> {
        return clipboardDao.getTagsForClipboard(clipboardId)
    }

    suspend fun getTagByName(name: String): TagEntity? {
        return clipboardDao.getTagByName(name)
    }

    fun getRecentClipboards(limit: Int): Flow<List<ClipboardEntity>> {
        return clipboardDao.getRecentClipboards(limit)
    }

    suspend fun deleteAllClipboards() {
        clipboardDao.deleteAll()
    }

    suspend fun updateClipboard(id: Long, content: String, tags: List<String>) {
        android.util.Log.d("ClipboardRepository", "updateClipboard: id=$id, 内容长度=${content.length}, 标签数量=${tags.size}")

        // 1. 删除旧的标签关联
        clipboardDao.deleteTagsForClipboard(id)
        android.util.Log.d("ClipboardRepository", "删除旧标签完成")

        // 2. 更新内容
        clipboardDao.updateClipboardContent(id, content, System.currentTimeMillis())
        android.util.Log.d("ClipboardRepository", "更新内容完成")

        // 3. 插入新的标签关联
        tags.forEach { tagName ->
            if (tagName.isNotBlank()) {
                val tag = TagEntity(
                    name = tagName.trim(),
                    clipboardId = id,
                    color = getRandomColor()
                )
                clipboardDao.insertTag(tag)
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
        return clipboardDao.getAllTagsWithCount()
    }

    fun getClipboardsByTag(tagName: String): Flow<List<ClipboardEntity>> {
        return clipboardDao.getClipboardsByTag(tagName)
    }

    suspend fun deleteTag(tagName: String) {
        clipboardDao.deleteTagByName(tagName)
    }
}
