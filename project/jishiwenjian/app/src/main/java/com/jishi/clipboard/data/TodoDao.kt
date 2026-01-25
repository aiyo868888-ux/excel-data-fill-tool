package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 待办事项 DAO
 */
@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE status = :status ORDER BY createdAt DESC")
    fun getTodosByStatus(status: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE dueTimestamp IS NOT NULL AND dueTimestamp > :currentTime ORDER BY dueTimestamp ASC")
    fun getUpcomingTodos(currentTime: Long = System.currentTimeMillis()): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("UPDATE todos SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTodoStatus(id: Long, status: String, completedAt: Long? = null, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM todos WHERE status = 'COMPLETED' AND completedAt < :beforeTime")
    suspend fun deleteCompletedBefore(beforeTime: Long)
}
