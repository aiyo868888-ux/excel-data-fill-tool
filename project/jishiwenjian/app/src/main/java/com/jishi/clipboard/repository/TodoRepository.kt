package com.jishi.clipboard.repository

import com.jishi.clipboard.data.TodoDao
import com.jishi.clipboard.data.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 待办事项仓库
 */
@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()

    fun getPendingTodos(): Flow<List<TodoEntity>> =
        todoDao.getTodosByStatus("PENDING")

    fun getCompletedTodos(): Flow<List<TodoEntity>> =
        todoDao.getTodosByStatus("COMPLETED")

    fun getUpcomingTodos(): Flow<List<TodoEntity>> =
        todoDao.getUpcomingTodos()

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)

    suspend fun insertTodo(todo: TodoEntity): Long = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)

    suspend fun markAsCompleted(id: Long) {
        todoDao.updateTodoStatus(id, "COMPLETED", System.currentTimeMillis())
    }

    suspend fun markAsPending(id: Long) {
        todoDao.updateTodoStatus(id, "PENDING", null)
    }
}
