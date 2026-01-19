package com.jishi.clipboard.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 提醒设置管理
 */
class ReminderPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "reminder_settings"
        private const val KEY_TODO_REMINDER_HOUR = "todo_reminder_hour"
        private const val KEY_TODO_REMINDER_MINUTE = "todo_reminder_minute"
        private const val KEY_TODO_REMINDER_ENABLED = "todo_reminder_enabled"

        // 默认提醒时间：第二天 8:00
        const val DEFAULT_HOUR = 8
        const val DEFAULT_MINUTE = 0
    }

    /**
     * 获取待办提醒时间（小时）
     */
    fun getTodoReminderHour(): Int {
        return prefs.getInt(KEY_TODO_REMINDER_HOUR, DEFAULT_HOUR)
    }

    /**
     * 获取待办提醒时间（分钟）
     */
    fun getTodoReminderMinute(): Int {
        return prefs.getInt(KEY_TODO_REMINDER_MINUTE, DEFAULT_MINUTE)
    }

    /**
     * 设置待办提醒时间
     */
    fun setTodoReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_TODO_REMINDER_HOUR, hour)
            .putInt(KEY_TODO_REMINDER_MINUTE, minute)
            .apply()
    }

    /**
     * 获取待办提醒是否启用
     */
    fun isTodoReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_TODO_REMINDER_ENABLED, true)
    }

    /**
     * 设置待办提醒是否启用
     */
    fun setTodoReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TODO_REMINDER_ENABLED, enabled).apply()
    }

    /**
     * 获取格式化的提醒时间字符串
     */
    fun getFormattedReminderTime(): String {
        return String.format("%02d:%02d", getTodoReminderHour(), getTodoReminderMinute())
    }
}
