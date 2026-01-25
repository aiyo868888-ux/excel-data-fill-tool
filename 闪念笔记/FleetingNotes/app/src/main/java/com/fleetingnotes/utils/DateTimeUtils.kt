package com.fleetingnotes.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeZone = TimeZone.currentSystemDefault()

    fun getCurrentLocalDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(timeZone).date
    }

    fun formatInstant(instant: Instant, pattern: String = "HH:mm"): String {
        val dateTime = instant.toLocalDateTime(timeZone)
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    fun formatLocalDate(date: LocalDate): String {
        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
    }

    fun formatTime(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}
