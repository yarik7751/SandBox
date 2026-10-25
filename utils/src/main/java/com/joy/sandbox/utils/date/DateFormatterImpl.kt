package com.joy.sandbox.utils.date

import android.content.Context
import com.joy.sandbox.utils.R
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class DateFormatterImpl @Inject constructor(
    private val context: Context,
    private val locale: Locale,
) : DateFormatter {

    private val formatTimeOffset = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val formatDayMonthFullYear = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    private val formatDayMonthDigitsShort = DateTimeFormatter.ofPattern("dd.MM", locale)
    private val formatDayOfWeek = DateTimeFormatter.ofPattern("EEE", locale)
    private val formatDayOfMonth = DateTimeFormatter.ofPattern("dd", locale)
    private val formatTimeDayMonthYear = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy", locale)

    /**
     * Сегодня/Вчера/12.04.2024
     */
    override fun formatDayMonthYear(dateTime: OffsetDateTime): String {
        return when {
            isSameDay(
                date1 = OffsetDateTime.now(),
                date2 = dateTime
            ) -> context.getString(R.string.date_today)

            isYesterdayDay(
                date = dateTime,
            ) -> context.getString(R.string.date_yesterday)

            else -> {
                val localDateTime = dateTime
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime()

                formatDayMonthFullYear.format(localDateTime)
            }
        }
    }

    override fun formatDayMonthOrTime(dateTime: OffsetDateTime): String {
        val localDateTime = dateTime
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        return when {
            isSameDay(
                date1 = OffsetDateTime.now(),
                date2 = dateTime
            ) -> formatTimeOffset.format(localDateTime)

            else -> {
                formatDayMonthDigitsShort.format(localDateTime)
            }
        }
    }

    override fun isSameDay(date1: OffsetDateTime, date2: OffsetDateTime): Boolean {
        return date1.year == date2.year && date1.dayOfYear == date2.dayOfYear
    }

    override fun isYesterdayDay(
        date: OffsetDateTime,
        today: OffsetDateTime,
    ): Boolean {
        val dayDiff = today.dayOfYear - date.dayOfYear
        return dayDiff == 1
    }

    override fun formatHoursMinutes(date: OffsetDateTime): String {
        val localDateTime = date
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        return formatTimeOffset.format(localDateTime)
    }

    override fun formatDayMonthYearDots(date: OffsetDateTime): String {
        val localDateTime = date
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        return formatDayMonthFullYear.format(localDateTime)
    }

    override fun formatDayOfWeek(date: OffsetDateTime): String {
        val localDateTime = date
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        return formatDayOfWeek.format(localDateTime)
    }

    override fun formatDayOfWeek(date: LocalDateTime): String {
        return formatDayOfWeek.format(date)
    }

    override fun formatDayOfMonth(date: OffsetDateTime): String {
        val localDateTime = date
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

        return formatDayOfMonth.format(localDateTime)
    }

    override fun formatDayOfMonth(date: LocalDateTime): String {
        return formatDayOfMonth.format(date)
    }

    override fun formatFullDateAndTime(date: LocalDateTime): String {
        return formatTimeDayMonthYear.format(date)
    }
}
