package com.jishi.clipboard.repository

import com.jishi.clipboard.data.Reminder
import com.jishi.clipboard.data.ReminderDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提醒数据仓库
 */
@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    fun getRemindersForClipboard(clipboardId: Long): Flow<List<Reminder>> {
        return reminderDao.getRemindersForClipboard(clipboardId)
    }

    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)
    }

    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    fun getUpcomingReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<Reminder>> {
        return reminderDao.getUpcomingReminders(currentTime)
    }

    fun getPendingReminders(): Flow<List<Reminder>> {
        return reminderDao.getPendingReminders()
    }

    suspend fun markAsNotified(id: Long) {
        reminderDao.markAsNotified(id)
    }

    suspend fun deleteRemindersForClipboard(clipboardId: Long) {
        reminderDao.deleteRemindersForClipboard(clipboardId)
    }
}
