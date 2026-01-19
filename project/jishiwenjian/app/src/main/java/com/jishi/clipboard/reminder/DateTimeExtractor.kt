package com.jishi.clipboard.reminder

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * 时间提取器 - 从文本中智能提取时间信息
 *
 * 支持格式：
 * - 绝对时间：2024-01-20 14:30、1月20日下午3点
 * - 相对时间：明天上午9点、下周三、3天后、2小时后
 */
object DateTimeExtractor {

    data class ExtractedDateTime(
        val timestamp: Long,
        val originalText: String,
        val confidence: Float // 0-1，置信度
    )

    /**
     * 从文本中提取时间
     *
     * 优先级：
     * 1. 用户明确指定的时间（明天早上6点）→ 使用用户的6点
     * 2. 只有时间段（明天早上）→ 使用默认时间段映射
     */
    fun extract(text: String): ExtractedDateTime? {
        android.util.Log.d("DateTimeExtractor", "========== 开始提取时间: $text ==========")

        // 1. 先尝试绝对时间匹配（含具体时间数字）
        val absoluteResult = extractAbsoluteTime(text)
        if (absoluteResult != null) {
            android.util.Log.d("DateTimeExtractor", "✅ 使用绝对时间结果: ${formatTimestamp(absoluteResult.timestamp)}")
            return absoluteResult
        }

        android.util.Log.d("DateTimeExtractor", "⚠️ 绝对时间匹配失败，尝试相对时间")

        // 2. 尝试相对时间匹配（明天早上、下午等，但没有具体时间）
        val relativeResult = extractRelativeTime(text)
        if (relativeResult != null) {
            android.util.Log.d("DateTimeExtractor", "✅ 使用相对时间结果: ${formatTimestamp(relativeResult.timestamp)}")
            return relativeResult
        }

        android.util.Log.d("DateTimeExtractor", "❌ 无法提取时间")
        return null
    }

    /**
     * 提取绝对时间
     */
    private fun extractAbsoluteTime(text: String): ExtractedDateTime? {
        android.util.Log.d("DateTimeExtractor", ">>> extractAbsoluteTime 开始: $text")

        // 1. 提取具体时间（HH:mm 或 H点，允许空格）
        // 匹配：6点、6 点、6:30、6:30 等
        // 注意：正则顺序很重要，先匹配具体的，再匹配模糊的
        val timePattern = Pattern.compile("(\\d{1,2})\\s*点\\s*(\\d{1,2})\\s*分?|(\\d{1,2}):(\\d{1,2})|(\\d{1,2})\\s*点")
        val timeMatcher = timePattern.matcher(text)

        android.util.Log.d("DateTimeExtractor", "  正则表达式: (\\d{1,2})\\s*点\\s*(\\d{1,2})\\s*分?|(\\d{1,2}):(\\d{1,2})|(\\d{1,2})\\s*点")

        // 只调用一次 find()，保存结果
        val found = timeMatcher.find()
        android.util.Log.d("DateTimeExtractor", "  匹配结果: $found")
        if (found) {
            android.util.Log.d("DateTimeExtractor", "  group(1)=${timeMatcher.group(1)}, group(2)=${timeMatcher.group(2)}, group(3)=${timeMatcher.group(3)}, group(4)=${timeMatcher.group(4)}, group(5)=${timeMatcher.group(5)}")
        }

        if (!found) {
            android.util.Log.d("DateTimeExtractor", "❌ 正则匹配失败")
            return null
        }

        android.util.Log.d("DateTimeExtractor", "✅ 正则匹配成功")

        // 提取小时和分钟
        // 新正则有三种模式：
        // 1. (\d{1,2})\s*点\s*(\d{1,2})\s*分?  → group(1)=小时, group(2)=分钟 (如: 5点30分)
        // 2. (\d{1,2}):(\d{1,2})              → group(3)=小时, group(4)=分钟 (如: 5:30)
        // 3. (\d{1,2})\s*点                 → group(5)=小时, group(6)=null (如: 5点)

        val hour: Int
        val minute: Int

        when {
            timeMatcher.group(1) != null -> {
                // 模式1: 5点30分
                hour = timeMatcher.group(1)!!.toInt()
                minute = if (timeMatcher.group(2) != null) {
                    timeMatcher.group(2)!!.toInt()
                } else {
                    0
                }
                android.util.Log.d("DateTimeExtractor", "  匹配模式1: X点Y分, hour=$hour, minute=$minute")
            }
            timeMatcher.group(3) != null -> {
                // 模式2: 5:30
                hour = timeMatcher.group(3)!!.toInt()
                minute = timeMatcher.group(4)!!.toInt()
                android.util.Log.d("DateTimeExtractor", "  匹配模式2: X:Y, hour=$hour, minute=$minute")
            }
            timeMatcher.group(5) != null -> {
                // 模式3: 5点
                hour = timeMatcher.group(5)!!.toInt()
                minute = 0
                android.util.Log.d("DateTimeExtractor", "  匹配模式3: X点, hour=$hour, minute=$minute")
            }
            else -> {
                android.util.Log.e("DateTimeExtractor", "❌ 无法提取时间")
                return null
            }
        }

        android.util.Log.d("DateTimeExtractor", "  提取到时间: $hour:$minute")

        // 2. 根据上午/下午调整小时
        var adjustedHour = hour
        when {
            text.contains("下午") || text.contains("中午") -> {
                adjustedHour = if (hour < 12) hour + 12 else hour
                android.util.Log.d("DateTimeExtractor", "  下午/中午，调整小时: $hour -> $adjustedHour")
            }
            text.contains("凌晨") || text.contains("早上") || text.contains("早晨") || text.contains("清晨") || text.contains("上午") -> {
                // 0-12点，不调整（除非用户说12点以上，按12小时制处理）
                adjustedHour = if (hour == 12) 0 else hour
                android.util.Log.d("DateTimeExtractor", "  上午时段，调整小时: $hour -> $adjustedHour")
            }
        }

        // 3. 创建新的 calendar 对象，从当前时间开始
        val calendar = Calendar.getInstance()
        android.util.Log.d("DateTimeExtractor", "  当前时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)}")

        // 4. 提取日期前缀
        when {
            // 明天
            text.contains("明天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                android.util.Log.d("DateTimeExtractor", "  日期: 明天")
            }
            // 后天
            text.contains("后天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 2)
                android.util.Log.d("DateTimeExtractor", "  日期: 后天")
            }
            // 大后天
            text.contains("大后天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 3)
                android.util.Log.d("DateTimeExtractor", "  日期: 大后天")
            }
            // 今天（不加）
            text.contains("今天") -> {
                android.util.Log.d("DateTimeExtractor", "  日期: 今天")
            }
            // 具体日期
            else -> {
                android.util.Log.d("DateTimeExtractor", "  日期: 具体日期（未实现）")
            }
        }

        // 5. 先清除秒和毫秒
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // 6. 设置时间（HOUR_OF_DAY 使用24小时制）
        calendar.set(Calendar.HOUR_OF_DAY, adjustedHour)
        calendar.set(Calendar.MINUTE, minute)

        val timestamp = calendar.timeInMillis
        android.util.Log.d("DateTimeExtractor", "  最终时间戳: $timestamp")
        android.util.Log.d("DateTimeExtractor", "  格式化: ${formatTimestamp(timestamp)}")

        // 不检查时间是否过期，直接返回
        android.util.Log.d("DateTimeExtractor", "✅ 返回绝对时间结果")
        return ExtractedDateTime(
            timestamp = timestamp,
            originalText = text.take(50),
            confidence = 0.95f
        )
    }

    /**
     * 提取相对时间（只有时间段，没有具体时间数字）
     *
     * 时间段定义（避免重合）：
     * - 凌晨：0:00-6:00 → 默认 5:00
     * - 早上：6:00-12:00 → 默认 8:00
     * - 中午：12:00-14:00 → 默认 12:30
     * - 下午：14:00-18:00 → 默认 15:00
     * - 晚上：18:00-24:00 → 默认 20:00
     */
    private fun extractRelativeTime(text: String): ExtractedDateTime? {
        val calendar = Calendar.getInstance()
        var found = false
        var confidence = 0.6f

        // 检查是否包含具体时间（如果有，应该在 extractAbsoluteTime 中处理）
        // 这里只处理时间段的情况

        // 提取时间段（不重合的定义）
        when {
            text.contains("凌晨") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 5)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
            text.contains("早上") || text.contains("早晨") || text.contains("清晨") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 8)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
            text.contains("上午") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 10)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
            text.contains("中午") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 30) // 中午12:30
                found = true
                confidence = 0.7f
            }
            text.contains("下午") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 15)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
            text.contains("傍晚") || text.contains("黄昏") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 18)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
            text.contains("晚上") || text.contains("夜里") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 0)
                found = true
                confidence = 0.7f
            }
        }

        // 相对天数
        when {
            text.contains("今天") -> {
                // 今天不加
                found = true
                confidence = 0.8f
            }
            text.contains("明天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                found = true
                confidence = 0.8f
            }
            text.contains("后天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 2)
                found = true
                confidence = 0.8f
            }
            text.contains("大后天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, 3)
                found = true
            }
            Pattern.compile("(\\d+)天后?").matcher(text).find() -> {
                val matcher = Pattern.compile("(\\d+)天后?").matcher(text)
                if (matcher.find()) {
                    calendar.add(Calendar.DAY_OF_MONTH, matcher.group(1)!!.toInt())
                    found = true
                }
            }
        }

        // 星期几
        if (!found) {
            val weekPattern = Pattern.compile("(下|本)?周([一二三四五六七日天])(上午|中午|下午|晚上|凌晨|早上)?")
            val weekMatcher = weekPattern.matcher(text)
            if (weekMatcher.find()) {
                val prefix = weekMatcher.group(1) ?: "本"
                val weekDay = weekMatcher.group(2)
                val period = weekMatcher.group(3)

                val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                val targetDay = weekDayToCalendarDay(weekDay)

                var daysToAdd = targetDay - currentDay
                if (prefix == "下" || daysToAdd <= 0) {
                    daysToAdd += 7
                }

                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
                found = true
                confidence = 0.7f

                // 设置时间段
                period?.let {
                    when (it) {
                        "凌晨" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 5)
                            calendar.set(Calendar.MINUTE, 0)
                        }
                        "早上" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 8)
                            calendar.set(Calendar.MINUTE, 0)
                        }
                        "上午" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 10)
                            calendar.set(Calendar.MINUTE, 0)
                        }
                        "中午" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 12)
                            calendar.set(Calendar.MINUTE, 30)
                        }
                        "下午" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 15)
                            calendar.set(Calendar.MINUTE, 0)
                        }
                        "晚上" -> {
                            calendar.set(Calendar.HOUR_OF_DAY, 20)
                            calendar.set(Calendar.MINUTE, 0)
                        }
                    }
                }
            }
        }

        // 小时后
        if (!found) {
            val hourPattern = Pattern.compile("(\\d+)小时后?")
            val hourMatcher = hourPattern.matcher(text)
            if (hourMatcher.find()) {
                calendar.add(Calendar.HOUR_OF_DAY, hourMatcher.group(1)!!.toInt())
                found = true
                confidence = 0.6f
            }
        }

        // 分钟后
        if (!found) {
            val minutePattern = Pattern.compile("(\\d+)分钟后?")
            val minuteMatcher = minutePattern.matcher(text)
            if (minuteMatcher.find()) {
                calendar.add(Calendar.MINUTE, minuteMatcher.group(1)!!.toInt())
                found = true
                confidence = 0.6f
            }
        }

        if (found) {
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val timestamp = calendar.timeInMillis
            if (timestamp > System.currentTimeMillis()) {
                android.util.Log.d("DateTimeExtractor", "提取相对时间: $text → ${formatTimestamp(timestamp)}")
                return ExtractedDateTime(
                    timestamp = timestamp,
                    originalText = text.take(50),
                    confidence = confidence
                )
            }
        }

        return null
    }

    /**
     * 将中文星期几转换为 Calendar 的星期几
     */
    private fun weekDayToCalendarDay(weekDay: String): Int {
        return when (weekDay) {
            "一", "天" -> Calendar.MONDAY
            "二" -> Calendar.TUESDAY
            "三" -> Calendar.WEDNESDAY
            "四" -> Calendar.THURSDAY
            "五" -> Calendar.FRIDAY
            "六" -> Calendar.SATURDAY
            "日" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }

    /**
     * 格式化时间戳为可读文本
     */
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = timestamp - now

        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return when {
            // 今天
            isSameDay(now, timestamp) -> {
                "今天 ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}"
            }
            // 明天
            isTomorrow(now, timestamp) -> {
                "明天 ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}"
            }
            // 本周
            isInWeek(now, timestamp) -> {
                val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
                val weekDayName = arrayOf("日", "一", "二", "三", "四", "五", "六")[weekDay - 1]
                "周${weekDayName} ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}"
            }
            // 其他
            else -> {
                SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(now: Long, timestamp: Long): Boolean {
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calTarget = Calendar.getInstance().apply { timeInMillis = timestamp }
        calNow.add(Calendar.DAY_OF_MONTH, 1)
        return calNow.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTarget.get(Calendar.DAY_OF_YEAR)
    }

    private fun isInWeek(now: Long, timestamp: Long): Boolean {
        val diff = timestamp - now
        return diff in 1..(7 * 24 * 60 * 60 * 1000)
    }
}
