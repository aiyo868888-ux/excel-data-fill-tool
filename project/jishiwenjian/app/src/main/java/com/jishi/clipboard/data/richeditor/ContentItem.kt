package com.jishi.clipboard.data.richeditor

import java.util.UUID

/**
 * 富文本内容项基类
 * 使用 sealed class 确保类型安全
 */
sealed class ContentItem {
    abstract val id: String
    abstract val type: String

    /**
     * 文本项
     */
    data class TextItem(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        val format: TextFormat = TextFormat()
    ) : ContentItem() {
        override val type: String = "text"
    }

    /**
     * 图片项
     */
    data class ImageItem(
        override val id: String = UUID.randomUUID().toString(),
        val imagePath: String, // 本地存储路径
        val width: Int = 0,
        val height: Int = 0,
        val caption: String = "" // 可选图片说明
    ) : ContentItem() {
        override val type: String = "image"
    }

    /**
     * 语音项（预留）
     */
    data class VoiceItem(
        override val id: String = UUID.randomUUID().toString(),
        val voicePath: String,
        val duration: Int // 秒
    ) : ContentItem() {
        override val type: String = "voice"
    }

    /**
     * 文本格式化信息
     */
    data class TextFormat(
        val bold: Boolean = false,
        val italic: Boolean = false,
        val fontSize: Int = 16, // sp
        val textColor: String = "#1f2937"
    )
}
