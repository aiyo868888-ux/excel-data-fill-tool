package com.jishi.clipboard.reminder

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * 模糊时间解析器
 *
 * 支持模糊时间表达：
 * - 月底/月末/月初/月终
 * - 季度初/季度末/季度底
 * - 年底/年末/年初
 * - 周末（周六）
 * - 具体星期几（下周三等）
 */
object FuzzyTimeParser {

    /**
     * 解析模糊时间
     *
     * @param text 时间文本（如"月底"、"下周三"）
     * @param baseDate 基准日期（默认当前时间）
     * @return 时间戳（毫秒），无法解析返回 null
     */
    fun parse(text: String, baseDate: Calendar = Calendar.getInstance()): Long? {
        val cleanText = text.trim()

        // 月底/月末/月终
        if (cleanText.contains("月底") || cleanText.contains("月末") || cleanText.contains("月终")) {
            return parseEndOfMonth(baseDate, getOffset(cleanText))
        }

        // 月初
        if (cleanText.contains("月初")) {
            return parseStartOfMonth(baseDate, getOffset(cleanText))
        }

        // 季度末/季度底
        if (cleanText.contains("季度末") || cleanText.contains("季度底")) {
            return parseEndOfQuarter(baseDate, getOffset(cleanText))
        }

        // 季度初
        if (cleanText.contains("季度初")) {
            return parseStartOfQuarter(baseDate, getOffset(cleanText))
        }

        // 年底/年末
        if (cleanText.contains("年底") || cleanText.contains("年末")) {
            return parseEndOfYear(baseDate, getOffset(cleanText))
        }

        // 年初
        if (cleanText.contains("年初")) {
            return parseStartOfYear(baseDate, getOffset(cleanText))
        }

        // 周末
        if (cleanText.contains("周末")) {
            return parseWeekend(baseDate, getOffset(cleanText))
        }

        // 周初（周一）
        if (cleanText.contains("周初") || (cleanText.contains("周一") && cleanText.contains("本"))) {
            return parseStartOfWeek(baseDate, getOffset(cleanText))
        }

        // 具体星期几
        val weekdayPattern = Regex("周([一二三四五六七日天])")
        val weekdayMatch = weekdayPattern.find(cleanText)
        if (weekdayMatch != null) {
            val weekday = weekdayMatch.groupValues[1]
            val dayOfWeek = weekdayToCalendarDay(weekday)
            return parseSpecificWeekday(baseDate, dayOfWeek, getOffset(cleanText))
        }

        return null
    }

    /**
     * 获取偏移量（上/下/本）
     * @return -1 (上), 0 (本/无), 1 (下)
     */
    private fun getOffset(text: String): Int {
        return when {
            text.contains("上") -> -1
            text.contains("下") -> 1
            else -> 0
        }
    }

    /**
     * 月底（当月最后一天 23:59:59）
     */
    private fun parseEndOfMonth(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.MONTH, offset)

        // 设置为当月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 月初（当月第一天 00:00:00）
     */
    private fun parseStartOfMonth(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.MONTH, offset)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 季度末
     */
    private fun parseEndOfQuarter(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        val currentQuarter = (calendar.get(Calendar.MONTH) / 3) + 1
        var targetQuarter = currentQuarter + offset

        // 处理季度溢出
        var yearOffset = 0
        while (targetQuarter > 4) {
            targetQuarter -= 4
            yearOffset++
        }
        while (targetQuarter < 1) {
            targetQuarter += 4
            yearOffset--
        }

        calendar.add(Calendar.YEAR, yearOffset)
        val lastMonthOfQuarter = targetQuarter * 3
        calendar.set(Calendar.MONTH, lastMonthOfQuarter - 1)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 季度初
     */
    private fun parseStartOfQuarter(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        val currentQuarter = (calendar.get(Calendar.MONTH) / 3) + 1
        var targetQuarter = currentQuarter + offset

        var yearOffset = 0
        while (targetQuarter > 4) {
            targetQuarter -= 4
            yearOffset++
        }
        while (targetQuarter < 1) {
            targetQuarter += 4
            yearOffset--
        }

        calendar.add(Calendar.YEAR, yearOffset)
        val firstMonthOfQuarter = (targetQuarter - 1) * 3
        calendar.set(Calendar.MONTH, firstMonthOfQuarter)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 年底
     */
    private fun parseEndOfYear(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.YEAR, offset)

        calendar.set(Calendar.MONTH, 11) // 12月
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 年初
     */
    private fun parseStartOfYear(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.YEAR, offset)

        calendar.set(Calendar.MONTH, 0) // 1月
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 周末（周六 00:00:00）
     */
    private fun parseWeekend(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.WEEK_OF_YEAR, offset)

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSaturday = Calendar.SATURDAY - currentDay
        val daysToAdd = if (daysToSaturday <= 0) daysToSaturday + 7 else daysToSaturday

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 周初（周一 00:00:00）
     */
    private fun parseStartOfWeek(baseDate: Calendar, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.WEEK_OF_YEAR, offset)

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = Calendar.MONDAY - currentDay
        val daysToAdd = if (daysToMonday <= 0) daysToMonday + 7 else daysToMonday

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 具体星期几
     * @param weekday 1=周一, 7=周日
     */
    private fun parseSpecificWeekday(baseDate: Calendar, weekday: Int, offset: Int): Long {
        val calendar = baseDate.clone() as Calendar
        calendar.add(Calendar.WEEK_OF_YEAR, offset)

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToAdd = weekday - currentDay
        val finalDaysToAdd = if (daysToAdd <= 0) daysToAdd + 7 else daysToAdd

        calendar.add(Calendar.DAY_OF_MONTH, finalDaysToAdd)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 将中文星期几转换为 Calendar 的星期几
     * @return 1=周一, 2=周二, ..., 7=周日
     */
    private fun weekdayToCalendarDay(weekday: String): Int {
        return when (weekday) {
            "一" -> 1
            "二" -> 2
            "三" -> 3
            "四" -> 4
            "五" -> 5
            "六" -> 6
            "七", "日", "天" -> 7
            else -> 1
        }
    }
}
