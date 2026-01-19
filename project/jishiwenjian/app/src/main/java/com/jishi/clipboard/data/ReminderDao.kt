package com.jishi.clipboard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 提醒数据访问对象
 */
@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("SELECT * FROM reminders WHERE clipboardId = :clipboardId")
    fun getRemindersForClipboard(clipboardId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders ORDER BY timestamp ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE timestamp > :currentTime ORDER BY timestamp ASC")
    fun getUpcomingReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isNotified = 0 ORDER BY timestamp ASC")
    fun getPendingReminders(): Flow<List<Reminder>>

    @Query("UPDATE reminders SET isNotified = 1 WHERE id = :id")
    suspend fun markAsNotified(id: Long)

    @Query("DELETE FROM reminders WHERE clipboardId = :clipboardId")
    suspend fun deleteRemindersForClipboard(clipboardId: Long)
}
