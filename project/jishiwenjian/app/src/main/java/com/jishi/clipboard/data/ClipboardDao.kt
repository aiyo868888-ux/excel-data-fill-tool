package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 剪贴板数据 DAO
 */
@Dao
interface ClipboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(clipboard: ClipboardEntity): Long

    @Delete
    suspend fun deleteClipboard(clipboard: ClipboardEntity)

    @Query("SELECT * FROM clipboards ORDER BY createdAt DESC")
    fun getAllClipboards(): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboards WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchClipboards(query: String): Flow<List<ClipboardEntity>>
    
    @Query("SELECT * FROM clipboards WHERE type = :type ORDER BY createdAt DESC")
    fun getClipboardsByType(type: String): Flow<List<ClipboardEntity>>

    @Query("SELECT * FROM clipboards WHERE id = :id")
    suspend fun getClipboardById(id: Long): ClipboardEntity?

    @Query("SELECT * FROM clipboards ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentClipboards(limit: Int): Flow<List<ClipboardEntity>>

    @Query("DELETE FROM clipboards")
    suspend fun deleteAll()

    @Query("DELETE FROM clipboards WHERE id = :id")
    suspend fun deleteClipboardById(id: Long)

    @Query("UPDATE clipboards SET content = :content, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardContent(id: Long, content: String, timestamp: Long)
    
    @Query("UPDATE clipboards SET content = :content, type = :type, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardWithType(id: Long, content: String, type: String, timestamp: Long)
    
    @Query("UPDATE clipboards SET content = :content, images = :images, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardContentAndImages(id: Long, content: String, images: String?, timestamp: Long)
    
    @Query("UPDATE clipboards SET content = :content, type = :type, images = :images, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardWithTypeAndImages(id: Long, content: String, type: String, images: String?, timestamp: Long)

    @Query("UPDATE clipboards SET content = :content, type = :type, images = :images, metadata = :metadata, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClipboardFull(id: Long, content: String, type: String, images: String?, metadata: String, timestamp: Long)
}
