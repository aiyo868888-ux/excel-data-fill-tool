package com.jishi.clipboard.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.jishi.clipboard.R

/**
 * 卡片样式工具类（简化版）
 * 新设计系统已将样式内置到 XML 和 drawable 中，此类仅保留必要的辅助方法
 */
object CardStyleHelper {

    /**
     * 获取内容类型的文字颜色
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
                else -> R.color.border_color
            }
        )
    }
}
