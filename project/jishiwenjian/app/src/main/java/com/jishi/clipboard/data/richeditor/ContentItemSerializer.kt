package com.jishi.clipboard.data.richeditor

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * ContentItem JSON 序列化器
 * 负责将 ContentItem 列表转换为 JSON 字符串，以及反向解析
 */
object ContentItemSerializer {
    private const val TAG = "ContentItemSerializer"

    /**
     * 将 ContentItem 列表序列化为 JSON
     * 存储在 ClipboardEntity.content 字段（替代纯文本）
     */
    fun serialize(items: List<ContentItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = when (item) {
                is ContentItem.TextItem -> {
                    JSONObject().apply {
                        put("id", item.id)
                        put("type", "text")
                        put("text", item.text)
                        put("format", JSONObject().apply {
                            put("bold", item.format.bold)
                            put("italic", item.format.italic)
                            put("fontSize", item.format.fontSize)
                            put("textColor", item.format.textColor)
                        })
                    }
                }
                is ContentItem.ImageItem -> {
                    JSONObject().apply {
                        put("id", item.id)
                        put("type", "image")
                        put("imagePath", item.imagePath)
                        put("width", item.width)
                        put("height", item.height)
                        put("caption", item.caption)
                    }
                }
                is ContentItem.VoiceItem -> {
                    JSONObject().apply {
                        put("id", item.id)
                        put("type", "voice")
                        put("voicePath", item.voicePath)
                        put("duration", item.duration)
                    }
                }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    /**
     * 从 JSON 反序列化为 ContentItem 列表
     */
    fun deserialize(json: String?): List<ContentItem> {
        if (json.isNullOrEmpty()) return emptyList()

        val items = mutableListOf<ContentItem>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val type = jsonObject.getString("type")

                val item = when (type) {
                    "text" -> {
                        val formatJson = jsonObject.getJSONObject("format")
                        ContentItem.TextItem(
                            id = jsonObject.getString("id"),
                            text = jsonObject.getString("text"),
                            format = ContentItem.TextFormat(
                                bold = formatJson.getBoolean("bold"),
                                italic = formatJson.getBoolean("italic"),
                                fontSize = formatJson.getInt("fontSize"),
                                textColor = formatJson.getString("textColor")
                            )
                        )
                    }
                    "image" -> {
                        ContentItem.ImageItem(
                            id = jsonObject.getString("id"),
                            imagePath = jsonObject.getString("imagePath"),
                            width = jsonObject.getInt("width"),
                            height = jsonObject.getInt("height"),
                            caption = jsonObject.optString("caption", "")
                        )
                    }
                    "voice" -> {
                        ContentItem.VoiceItem(
                            id = jsonObject.getString("id"),
                            voicePath = jsonObject.getString("voicePath"),
                            duration = jsonObject.getInt("duration")
                        )
                    }
                    else -> null
                }
                item?.let { items.add(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize", e)
        }
        return items
    }

    /**
     * 向后兼容：如果是纯文本，自动转换为 TextItem
     */
    fun fromPlainText(text: String): List<ContentItem> {
        return if (text.isEmpty()) {
            emptyList()
        } else {
            listOf(ContentItem.TextItem(text = text))
        }
    }
}
