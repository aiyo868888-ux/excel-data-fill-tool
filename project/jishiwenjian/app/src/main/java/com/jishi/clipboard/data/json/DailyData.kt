package com.jishi.clipboard.data.json

/**
 * 每日数据容器
 * 包含当天的所有灵感、启发和待办
 */
data class DailyData(
    val date: String, // YYYY-MM-DD 格式
    val 灵感: List<InspirationItem> = emptyList(),
    val 启发: List<InsightItem> = emptyList(),
    val 待办: List<TodoItem> = emptyList()
)
