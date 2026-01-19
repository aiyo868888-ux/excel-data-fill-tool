package com.jishi.clipboard.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jishi.clipboard.ui.MainActivity
import com.jishi.clipboard.R

/**
 * 提醒接收器 - 接收闹铃广播并触发提醒
 *
 * 重要：BroadcastReceiver 不能使用 Hilt 注入，必须手动获取依赖
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
        private const val CHANNEL_ID = "reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "✅ 收到提醒广播")

        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val content = intent.getStringExtra("content") ?: ""
        val type = intent.getStringExtra("type") ?: "NOTIFICATION"
        val clipboardId = intent.getLongExtra("clipboard_id", -1L)

        Log.d(TAG, "提醒详情: id=$reminderId, content=$content, type=$type")

        when (type) {
            "NOTIFICATION" -> showNotification(context, reminderId, content, clipboardId)
            "ALARM" -> showAlarm(context, reminderId, content, clipboardId)
            "FULLSCREEN" -> showFullscreenDialog(context, reminderId, content, clipboardId)
        }

        // TODO: 标记为已通知（需要在后台线程执行）
        // 由于不能用 Hilt，暂时跳过数据库更新
    }

    /**
     * 显示通知
     */
    private fun showNotification(context: Context, reminderId: Long, content: String, clipboardId: Long) {
        createNotificationChannel(context)

        val notificationId = reminderId.toInt()

        // 点击通知打开应用
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("clipboard_id", clipboardId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("剪贴板提醒")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "✅ 显示通知: id=$notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 显示通知失败：权限不足", e)
        }
    }

    /**
     * 显示闹铃（带声音和震动）
     */
    private fun showAlarm(context: Context, reminderId: Long, content: String, clipboardId: Long) {
        createNotificationChannel(context)

        val notificationId = reminderId.toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("clipboard_id", clipboardId)
            putExtra("dismiss_alarm", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 使用系统闹铃声音
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 提醒")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true) // 不可滑动删除，必须点击才能关闭
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000)) // 更强的震动
            .setSound(alarmSound)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)

            // 同时也直接播放声音和震动，确保用户能听到/感觉到
            try {
                val ringtone = RingtoneManager.getRingtone(context, alarmSound)
                ringtone.play()
            } catch (e: Exception) {
                Log.e(TAG, "❌ 播放铃声失败", e)
            }

            Log.d(TAG, "✅ 显示闹铃: id=$notificationId")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 显示闹铃失败：权限不足", e)
        }
    }

    /**
     * 显示全屏对话框（需要额外权限）
     */
    private fun showFullscreenDialog(context: Context, reminderId: Long, content: String, clipboardId: Long) {
        // 全屏对话框需要 USE_FULL_SCREEN_INTENT 权限
        // 这里简化为高优先级通知
        showAlarm(context, reminderId, content, clipboardId)
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "剪贴板提醒通知"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true) // 绕过勿扰模式
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)

                // 设置铃声为闹铃声音
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(alarmSound, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "✅ 通知渠道已创建")
        }
    }
}
