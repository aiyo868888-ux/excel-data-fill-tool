package com.jishi.clipboard.utils

/**
 * 剪切板更新事件
 * 通过 EventBus 传递剪切板内容到对话框
 */
data class ClipboardUpdateEvent(val content: String)
