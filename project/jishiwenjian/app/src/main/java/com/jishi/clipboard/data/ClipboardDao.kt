package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(clipboard: ClipboardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteClipboard(clipboard: ClipboardEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM clipboards ORDER BY createdAt DESC")
    fun getAllClipboards(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboards WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchClipboards(query: String): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboards WHERE id = :id")
    suspend fun getClipboardById(id: Long): ClipboardEntity?

    @Query("SELECT * FROM tags WHERE clipboardId = :clipboardId")
    fun getTagsForClipboard(clipboardId: Long): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("DELETE FROM tags WHERE clipboardId = :clipboardId")
    suspend fun deleteTagsForClipboard(clipboardId: Long)

    @Query("SELECT * FROM clipboards ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentClipboards(limit: Int): Flow<List<ClipboardEntity>>

    @Query("DELETE FROM clipboards")
    suspend fun deleteAll()

    @Query("UPDATE clipboards SET content = :content, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardContent(id: Long, content: String, timestamp: Long)

    @Query("SELECT name, COUNT(*) as count FROM tags GROUP BY name ORDER BY count DESC")
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>

    @Query("""
        SELECT c.* FROM clipboards c
        INNER JOIN tags t ON c.id = t.clipboardId
        WHERE t.name = :tagName
        ORDER BY c.createdAt DESC
    """)
    fun getClipboardsByTag(tagName: String): Flow<List<ClipboardEntity>>

    @Query("DELETE FROM tags WHERE name = :tagName")
    suspend fun deleteTagByName(tagName: String)

    @Transaction
    suspend fun insertClipboardWithTags(clipboard: ClipboardEntity, tags: List<String>): Long {
        val clipboardId = insertClipboard(clipboard)
        tags.forEach { tagName ->
            if (tagName.isNotBlank()) {
                val tag = TagEntity(
                    name = tagName.trim(),
                    clipboardId = clipboardId,
                    color = getRandomColor()
                )
                insertTag(tag)
            }
        }
        return clipboardId
    }

    private fun getRandomColor(): String {
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F39C12", "#9B59B6", "#3498DB")
        return colors.random()
    }
}
