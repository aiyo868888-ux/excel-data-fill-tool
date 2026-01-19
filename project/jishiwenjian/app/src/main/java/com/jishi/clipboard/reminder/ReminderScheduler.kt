package com.jishi.clipboard.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.jishi.clipboard.data.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 提醒调度器 - 使用 AlarmManager 设置系统闹铃
 */
class ReminderScheduler(private val context: Context) {

    companion object {
        private const val TAG = "ReminderScheduler"
        private const val ACTION_REMINDER = "com.jishi.clipboard.ACTION_REMINDER"
    }

    /**
     * 调度提醒
     */
    fun schedule(reminder: Reminder): Boolean {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 检查精确闹钟权限（Android 12+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "❌ 无法调度提醒：缺少精确闹钟权限")
                    return false
                }
            }

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra("reminder_id", reminder.id)
                putExtra("content", reminder.content)
                putExtra("type", reminder.type)
                putExtra("clipboard_id", reminder.clipboardId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // 计算延迟时间
            val now = System.currentTimeMillis()
            val delay = reminder.timestamp - now
            val delayMinutes = delay / (1000 * 60)

            Log.d(TAG, "⏰ 调度提醒: id=${reminder.id}")
            Log.d(TAG, "   提醒时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(reminder.timestamp))}")
            Log.d(TAG, "   当前时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))}")
            Log.d(TAG, "   延迟: ${delayMinutes}分钟")

            // 使用 setExactAndAllowWhileIdle 确保在 Doze 模式下也能触发
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.timestamp,
                pendingIntent
            )

            Log.d(TAG, "✅ 成功调度提醒")
            return true

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 调度提醒失败：权限不足", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "❌ 调度提醒失败", e)
            return false
        }
    }

    /**
     * 取消提醒
     */
    fun cancel(reminderId: Long) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            Log.d(TAG, "取消提醒: id=$reminderId")
        } catch (e: Exception) {
            Log.e(TAG, "取消提醒失败", e)
        }
    }

    /**
     * 检查是否有精确闹钟权限
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * 获取请求精确闹钟权限的 Intent
     */
    fun createExactAlarmPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent()
        }
    }
}
