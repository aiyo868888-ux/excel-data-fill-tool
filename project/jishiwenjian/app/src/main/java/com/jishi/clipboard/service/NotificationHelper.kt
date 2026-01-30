package com.jishi.clipboard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.MainActivity

/**
 * 悬浮窗服务通知助手
 * 创建低优先级前台服务通知，保持悬浮窗服务不被系统杀死
 */
object NotificationHelper {
    private const val CHANNEL_ID = "floating_window_channel"
    const val NOTIFICATION_ID = 1001

    /**
     * 创建前台服务通知
     */
    fun createNotification(context: Context): android.app.Notification {
        createNotificationChannel(context)

        // 点击通知返回主界面
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("及时记")
            .setContentText("悬浮窗运行中")
            .setSmallIcon(R.drawable.ic_bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * 创建通知渠道（Android 8.0+ 需要）
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持悬浮窗服务在后台运行"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
