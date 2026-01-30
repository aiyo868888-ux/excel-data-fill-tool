package com.jishi.clipboard.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * 分享工具类
 * 封装 Android 分享和复制逻辑
 */
object ShareHelper {

    /**
     * 分享文本到其他应用
     */
    fun shareText(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }

            // 创建选择器对话框
            val chooser = Intent.createChooser(intent, "分享到...")
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "❌ 未找到可分享的应用", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ 分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 分享卡片内容（带标签）
     */
    fun shareCardContent(
        context: Context,
        content: String,
        tags: List<String>
    ) {
        // 直接分享原始内容，已包含 #标签
        shareText(context, content)
    }

    /**
     * 复制到剪贴板
     */
    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("内容", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, "✅ 已复制", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ 复制失败", Toast.LENGTH_SHORT).show()
        }
    }
}
