package com.joy.sandbox.utils.date

import java.time.LocalDateTime
import java.time.OffsetDateTime

interface DateFormatter {

    fun formatDayMonthYear(dateTime: OffsetDateTime): String

    fun formatDayMonthOrTime(dateTime: OffsetDateTime): String

    fun isSameDay(date1: OffsetDateTime, date2: OffsetDateTime): Boolean

    fun isYesterdayDay(
        date: OffsetDateTime,
        today: OffsetDateTime = OffsetDateTime.now(),
    ): Boolean

    fun formatHoursMinutes(date: OffsetDateTime): String

    fun formatDayMonthYearDots(date: OffsetDateTime): String

    fun formatDayOfWeek(date: OffsetDateTime): String

    fun formatDayOfWeek(date: LocalDateTime): String

    fun formatDayOfMonth(date: OffsetDateTime): String

    fun formatDayOfMonth(date: LocalDateTime): String

    fun formatFullDateAndTime(date: LocalDateTime): String
}
