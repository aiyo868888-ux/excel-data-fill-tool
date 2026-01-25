package com.jishi.clipboard.parser

/**
 * 任务提取器 - 从文本中分离任务描述、标签和时间信息
 */
object TaskExtractor {

    private val tagPattern = Regex("#([\\w\\u4e00-\\u9fa5]+)")
    private val timeKeywordPattern = Regex(
        "(明天|后天|今天|下周|本周|上周|\\d+天后?|\\d+小时后?|\\d+分钟后?|" +
        "月底|月末|月初|季度初|季度末|年底|年初|周末|周初|" +
        "周[一二三四五六七日天]|凌晨|早上|上午|中午|下午|晚上|夜里|" +
        "\\d{1,2}点|\\d{1,2}:\\d{2}|\\d{4}-\\d{1,2}-\\d{1,2})"
    )
    // 提醒触发词模式
    private val reminderTriggerPattern = Regex(
        "(喊我|叫醒我|提醒我|告诉我|通知我|叫我|叫起床|闹钟|提醒)"
    )

    /**
     * 提取任务信息
     * @return ParseResult 包含任务描述、标签、原始时间文本
     */
    fun extract(input: String): ParseResult {
        android.util.Log.d("TaskExtractor", "========== 提取任务: $input ==========")

        // 1. 提取标签
        val tags = tagPattern.findAll(input)
            .map { it.groupValues[1] }
            .toList()
        android.util.Log.d("TaskExtractor", "提取的标签: $tags")

        // 2. 移除标签，得到纯文本
        var textWithoutTags = tagPattern.replace(input, "").trim()

        // 3. 提取时间关键词（用于原始时间文本）
        val timeMatch = timeKeywordPattern.find(textWithoutTags)
        val originalTimeText = timeMatch?.value
        android.util.Log.d("TaskExtractor", "原始时间文本: $originalTimeText")

        // 4. 智能提取任务描述
        val task = extractTask(textWithoutTags, originalTimeText)
        android.util.Log.d("TaskExtractor", "任务描述: $task")

        return ParseResult(
            task = task,
            tags = tags,
            timestamp = null,  // 稍后由 DateTimeExtractor 填充
            originalTimeText = originalTimeText,
            rawContent = input
        )
    }

    /**
     * 智能提取任务描述
     * 规则：
     * 1. 如果包含提醒触发词（喊我、叫醒我等），提取触发词后的内容
     * 2. 如果包含逗号分隔符，尝试取最后一个分句
     * 3. 否则移除时间关键词后的内容
     */
    private fun extractTask(text: String, originalTimeText: String?): String {
        // 检查是否有提醒触发词
        val triggerMatch = reminderTriggerPattern.find(text)

        if (triggerMatch != null) {
            // 有触发词，提取触发词后的内容
            val afterTrigger = text.substring(triggerMatch.range.last + 1).trim()
            if (afterTrigger.isNotEmpty()) {
                android.util.Log.d("TaskExtractor", "发现提醒触发词: ${triggerMatch.value}, 提取任务: $afterTrigger")
                return afterTrigger
            }
        }

        // 检查是否有逗号分隔的多个分句
        if (text.contains("，") || text.contains(",")) {
            val parts = text.split(("[，,]").toRegex())
            if (parts.size > 1) {
                // 取最后一个分句作为任务
                val lastPart = parts.last().trim()
                android.util.Log.d("TaskExtractor", "发现逗号分隔，提取最后一个分句: $lastPart")

                // 如果最后一个分句包含时间关键词，则移除时间
                if (originalTimeText != null && lastPart.contains(originalTimeText)) {
                    return timeKeywordPattern.replace(lastPart, "").trim()
                }
                return lastPart
            }
        }

        // 默认：移除时间关键词
        return if (originalTimeText != null) {
            timeKeywordPattern.replace(text, "").trim()
        } else {
            text
        }
    }
}
