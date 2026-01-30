package com.jishi.clipboard.data.richeditor

import com.jishi.clipboard.data.ClipboardEntity

/**
 * ClipboardEntity 扩展函数
 * 用于在数据库实体和 ContentItem 之间转换
 */

/**
 * 将 ClipboardEntity 的 content 字段解析为 ContentItem 列表
 * 自动识别纯文本和富文本格式
 */
fun ClipboardEntity.toContentItems(): List<ContentItem> {
    return try {
        // 尝试解析为富文本 JSON
        ContentItemSerializer.deserialize(this.content)
    } catch (e: Exception) {
        // 失败则当作纯文本处理
        ContentItemSerializer.fromPlainText(this.content)
    }
}

/**
 * 将 ContentItem 列表转换为 JSON 字符串
 * 用于存储到 ClipboardEntity.content 字段
 */
fun List<ContentItem>.toEntityContent(): String {
    return ContentItemSerializer.serialize(this)
}
