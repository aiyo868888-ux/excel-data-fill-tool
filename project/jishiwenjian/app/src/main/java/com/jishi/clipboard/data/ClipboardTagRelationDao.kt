package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 剪贴板-标签关联 DAO
 */
@Dao
interface ClipboardTagRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<TagEntity>)

    @Query("DELETE FROM clipboard_tag_relations WHERE clipboardId = :clipboardId")
    suspend fun deleteRelationsForClipboard(clipboardId: Long)

    @Query("DELETE FROM clipboard_tag_relations WHERE tagDefinitionId = :tagDefinitionId")
    suspend fun deleteRelationsForTag(tagDefinitionId: Long)

    @Query("""
        SELECT td.* FROM tag_definitions td
        INNER JOIN clipboard_tag_relations ctr ON td.id = ctr.tagDefinitionId
        WHERE ctr.clipboardId = :clipboardId
        ORDER BY td.displayOrder ASC
    """)
    fun getTagDefinitionsForClipboard(clipboardId: Long): Flow<List<TagDefinition>>

    @Query("""
        SELECT c.* FROM clipboards c
        INNER JOIN clipboard_tag_relations ctr ON c.id = ctr.clipboardId
        WHERE ctr.tagDefinitionId = :tagDefinitionId
        ORDER BY c.createdAt DESC
    """)
    fun getClipboardsByTagDefinition(tagDefinitionId: Long): Flow<List<ClipboardEntity>>

    @Query("""
        SELECT td.name, COUNT(*) as count FROM tag_definitions td
        INNER JOIN clipboard_tag_relations ctr ON td.id = ctr.tagDefinitionId
        GROUP BY td.id, td.name
        ORDER BY count DESC
    """)
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>

    @Query("SELECT COUNT(*) FROM clipboard_tag_relations WHERE tagDefinitionId = :tagDefinitionId")
    suspend fun getTagUsageCount(tagDefinitionId: Long): Int

    @Query("SELECT * FROM clipboard_tag_relations")
    fun getAllRelations(): Flow<List<TagEntity>>
}
