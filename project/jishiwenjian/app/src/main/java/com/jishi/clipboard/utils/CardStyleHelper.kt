package com.jishi.clipboard.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.jishi.clipboard.R
import com.jishi.clipboard.data.ClipboardEntity
import kotlin.random.Random

/**
 * 卡片样式工具类
 * 根据内容类型和随机变体设置卡片背景
 */
object CardStyleHelper {

    /**
     * 设置卡片样式
     * @param cardView 卡片视图
     * @param item 数据项
     */
    fun setCardStyle(cardView: MaterialCardView, item: ClipboardEntity) {
        val contentType = getContentType(item)

        // 根据内容类型选择颜色方案
        val (backgroundColor, strokeColor, textColor) = when (contentType) {
            "灵感" -> {
                val variant = getRandomVariant()
                Triple(
                    getIdeaBackgroundResource(variant),
                    R.color.idea_border,
                    R.color.idea_text
                )
            }
            "启发" -> {
                val variant = getRandomVariant()
                Triple(
                    getInsightBackgroundResource(variant),
                    R.color.insight_border,
                    R.color.insight_text
                )
            }
            "待办" -> {
                val variant = getRandomVariant()
                Triple(
                    getTodoBackgroundResource(variant),
                    R.color.todo_border,
                    R.color.todo_text
                )
            }
            else -> {
                // 默认样式
                Triple(
                    android.R.color.white,
                    R.color.border,
                    R.color.text_primary
                )
            }
        }

        // 设置背景
        cardView.setCardBackgroundColor(
            ContextCompat.getColor(cardView.context, backgroundColor)
        )

        // 设置边框颜色
        cardView.strokeColor = ContextCompat.getColor(cardView.context, strokeColor)

        // 设置内容文字颜色
        val contentView = cardView.findViewById<android.widget.TextView>(R.id.contentText)
        contentView?.setTextColor(ContextCompat.getColor(cardView.context, textColor))
    }

    /**
     * 获取内容类型
     * 当前版本：随机分配类型样式
     * TODO: 未来从 ClipboardEntity 的标签或其他属性中读取类型
     */
    private fun getContentType(item: ClipboardEntity): String {
        // 当前版本：随机分配类型（用于演示UI效果）
        val types = listOf("灵感", "启发", "待办")
        return types[Random.nextInt(types.size)]

        // 未来实现：
        // return item.tags.find { tag -> tag in setOf("灵感", "启发", "待办") } ?: ""
    }

    /**
     * 获取随机变体 (1-3)
     */
    private fun getRandomVariant(): Int = Random.nextInt(1, 4)

    /**
     * 获取灵感背景资源
     */
    private fun getIdeaBackgroundResource(variant: Int): Int = when (variant) {
        1 -> R.drawable.bg_card_idea_v1
        2 -> R.drawable.bg_card_idea_v2
        3 -> R.drawable.bg_card_idea_v3
        else -> R.drawable.bg_card_idea_v1
    }

    /**
     * 获取启发背景资源
     */
    private fun getInsightBackgroundResource(variant: Int): Int = when (variant) {
        1 -> R.drawable.bg_card_insight_v1
        2 -> R.drawable.bg_card_insight_v2
        3 -> R.drawable.bg_card_insight_v3
        else -> R.drawable.bg_card_insight_v1
    }

    /**
     * 获取待办背景资源
     */
    private fun getTodoBackgroundResource(variant: Int): Int = when (variant) {
        1 -> R.drawable.bg_card_todo_v1
        2 -> R.drawable.bg_card_todo_v2
        3 -> R.drawable.bg_card_todo_v3
        else -> R.drawable.bg_card_todo_v1
    }

    /**
     * 获取内容类型的颜色资源ID
     */
    fun getContentTextColor(context: Context, contentType: String): Int {
        return ContextCompat.getColor(
            context,
            when (contentType) {
                "灵感" -> R.color.idea_text
                "启发" -> R.color.insight_text
                "待办" -> R.color.todo_text
                else -> R.color.text_primary
            }
        )
    }

    /**
     * 获取内容类型的边框颜色
     */
    fun getContentStrokeColor(context: Context, contentType: String): Int {
        return ContextCompat.getColor(
            context,
            when (contentType) {
                "灵感" -> R.color.idea_border
                "启发" -> R.color.insight_border
                "待办" -> R.color.todo_border
                else -> R.color.border
            }
        )
    }
}
