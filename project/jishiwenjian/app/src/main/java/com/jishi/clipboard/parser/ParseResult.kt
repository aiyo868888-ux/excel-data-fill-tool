package com.jishi.clipboard.parser

/**
 * 解析结果
 */
data class ParseResult(
    val task: String,              // 提取的任务描述
    val tags: List<String>,        // 提取的标签
    val timestamp: Long?,          // 解析的时间戳（可能为 null）
    val originalTimeText: String?, // 原始时间文本
    val rawContent: String         // 原始输入
)
