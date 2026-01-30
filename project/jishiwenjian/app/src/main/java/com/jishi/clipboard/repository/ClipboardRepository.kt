package com.jishi.clipboard.repository

import com.jishi.clipboard.data.ClipboardDao
import com.jishi.clipboard.data.ClipboardEntity
import com.jishi.clipboard.util.ImageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao
) {

    private companion object {
        val TAG_COLORS = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
            "#98D8C8", "#F39C12", "#9B59B6", "#3498DB"
        )
    }

    suspend fun saveClipboard(
        content: String,
        tags: List<String>,
        type: String = "灵感",
        images: List<String>? = null,
        metadata: String? = null
    ): Long {
        val clipboard = ClipboardEntity(
            content = content,
            type = type,
            images = ImageUtils.convertImagesToJson(images),
            metadata = metadata ?: "{}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val clipboardId = clipboardDao.insertClipboard(clipboard)

        Timber.d("保存剪贴板成功: id=$clipboardId, type=$type, 图片数=${images?.size ?: 0}")

        return clipboardId
    }

    suspend fun deleteClipboard(clipboard: ClipboardEntity) {
        // 删除关联的图片文件
        ImageUtils.deleteImages(ImageUtils.parseImagesFromJson(clipboard.images))
        deleteClipboard(clipboard.id)
    }

    suspend fun deleteClipboard(id: Long) {
        clipboardDao.deleteClipboardById(id)
    }

    suspend fun deleteAllClipboards() {
        clipboardDao.deleteAll()
    }

    // ===== 查询方法 =====

    fun getAllClipboards(): Flow<List<ClipboardEntity>> =
        clipboardDao.getAllClipboards()

    fun searchClipboards(query: String): Flow<List<ClipboardEntity>> =
        clipboardDao.searchClipboards(query)

    fun getRecentClipboards(limit: Int): Flow<List<ClipboardEntity>> =
        clipboardDao.getRecentClipboards(limit)

    suspend fun getClipboardById(id: Long): ClipboardEntity? =
        clipboardDao.getClipboardById(id)

    fun getClipboardsByType(type: String): Flow<List<ClipboardEntity>> =
        clipboardDao.getClipboardsByType(type)

    suspend fun updateClipboard(
        id: Long,
        content: String,
        tags: List<String>,
        type: String? = null,
        images: List<String>? = null,
        metadata: String? = null
    ) {

        val imagesJson = ImageUtils.convertImagesToJson(images)

        if (type != null) {
            if (metadata != null) {
                clipboardDao.updateClipboardFull(id, content, type, imagesJson, metadata, System.currentTimeMillis())
            } else {
                clipboardDao.updateClipboardWithTypeAndImages(id, content, type, imagesJson, System.currentTimeMillis())
            }
        } else {
            clipboardDao.updateClipboardContentAndImages(id, content, imagesJson, System.currentTimeMillis())
        }

        Timber.d("更新剪贴板成功: id=$id, type=$type, 图片数=${images?.size ?: 0}")
    }

    // ===== 同步方法（用于 Markdown 导出）=====

    suspend fun getAllClipboardsSync(): List<ClipboardEntity> =
        clipboardDao.getAllClipboards().first()
}
