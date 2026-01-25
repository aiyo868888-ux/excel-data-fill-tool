package com.jishi.clipboard.parser

import com.jishi.clipboard.data.TodoEntity
import com.jishi.clipboard.reminder.DateTimeExtractor

/**
 * 待办事项解析器 - 整合任务提取和时间解析
 */
object TodoParser {

    /**
     * 解析用户输入，生成 TodoEntity
     * @return TodoEntity? 如果解析失败返回 null
     */
    fun parse(input: String): TodoEntity? {
        if (input.isBlank()) return null

        android.util.Log.d("TodoParser", "========== 开始解析: $input ==========")

        // 1. 提取任务信息
        val extractResult = TaskExtractor.extract(input)

        // 2. 解析时间
        val timestamp: Long? = if (extractResult.originalTimeText != null) {
            val extractedTime = DateTimeExtractor.extract(extractResult.originalTimeText)
            extractedTime?.timestamp
        } else {
            null
        }

        android.util.Log.d("TodoParser", "解析完成: 任务=${extractResult.task}, 时间=$timestamp, 标签=${extractResult.tags}")

        // 3. 构造 TodoEntity
        return TodoEntity(
            task = extractResult.task,
            rawContent = input,
            dueTimestamp = timestamp,
            originalTimeText = extractResult.originalTimeText,
            status = "PENDING",
            tags = extractResult.tags.joinToString(","),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 批量解析
     */
    fun parseBatch(inputs: List<String>): List<TodoEntity> {
        return inputs.mapNotNull { parse(it) }
    }
}
