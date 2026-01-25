package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 标签定义 DAO
 */
@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagDefinition(tag: TagDefinition): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagDefinitions(tags: List<TagDefinition>)

    @Update
    suspend fun updateTagDefinition(tag: TagDefinition)

    @Delete
    suspend fun deleteTagDefinition(tag: TagDefinition)

    @Query("SELECT * FROM tag_definitions ORDER BY displayOrder ASC, useCount DESC")
    fun getAllTagDefinitions(): Flow<List<TagDefinition>>

    @Query("SELECT * FROM tag_definitions WHERE id = :id")
    suspend fun getTagDefinitionById(id: Long): TagDefinition?

    @Query("SELECT * FROM tag_definitions WHERE name = :name LIMIT 1")
    suspend fun getTagDefinitionByName(name: String): TagDefinition?

    @Query("UPDATE tag_definitions SET useCount = useCount + 1 WHERE id = :tagId")
    suspend fun incrementUseCount(tagId: Long)

    @Query("SELECT * FROM tag_definitions ORDER BY useCount DESC LIMIT :limit")
    fun getMostUsedTags(limit: Int): Flow<List<TagDefinition>>

    @Query("DELETE FROM tag_definitions")
    suspend fun deleteAllTagDefinitions()

    // 层级查询方法

    @Query("SELECT * FROM tag_definitions WHERE parentId IS NULL ORDER BY displayOrder ASC, useCount DESC")
    fun getRootTags(): Flow<List<TagDefinition>>

    @Query("SELECT * FROM tag_definitions WHERE parentId = :parentId ORDER BY displayOrder ASC, useCount DESC")
    fun getChildTags(parentId: Long): Flow<List<TagDefinition>>

    @Query("SELECT * FROM tag_definitions ORDER BY level ASC, displayOrder ASC, useCount DESC")
    fun getAllTagsHierarchical(): Flow<List<TagDefinition>>

    @Query("UPDATE tag_definitions SET level = :level WHERE id = :tagId")
    suspend fun updateTagLevel(tagId: Long, level: Int)

    @Query("SELECT * FROM tag_definitions WHERE parentId = :parentId ORDER BY displayOrder ASC")
    suspend fun getChildTagsSync(parentId: Long?): List<TagDefinition>
}
