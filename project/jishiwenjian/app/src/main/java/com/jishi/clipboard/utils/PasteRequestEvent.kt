package com.jishi.clipboard.utils

/**
 * 粘贴请求事件
 * 由悬浮窗触发，对话框接收并处理
 */
data class PasteRequestEvent(
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
