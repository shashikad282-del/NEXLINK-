package com.example.data.model

import java.util.Calendar
import java.util.Locale

/**
 * Pure-Kotlin Gregorian date representation compatible with API 24+ (no java.time desugaring needed).
 */
data class CalendarDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<CalendarDate> {

    override fun compareTo(other: CalendarDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    /**
     * Converts to UTC epoch day (1970-01-01 == 0) using Howard Hinnant's civil-to-days algorithm.
     */
    fun toEpochDay(): Long {
        val y = year - if (month <= 2) 1 else 0
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * (month + if (month > 2) -3 else 9) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return (era * 146097 + doe - 719468).toLong()
    }

    /**
     * Day of week: 0 = Monday .. 6 = Sunday.
     */
    fun dayOfWeekMondayZero(): Int {
        return ((toEpochDay() + 3L).mod(7L)).toInt()
    }

    fun plusDays(days: Int): CalendarDate = fromEpochDay(toEpochDay() + days)

    fun plusMonths(deltaMonths: Int): CalendarDate {
        val totalMonths = year * 12 + (month - 1) + deltaMonths
        val newYear = Math.floorDiv(totalMonths, 12)
        val newMonth = Math.floorMod(totalMonths, 12) + 1
        val maxDay = daysInMonth(newYear, newMonth)
        return CalendarDate(newYear, newMonth, day.coerceAtMost(maxDay))
    }

    fun firstOfMonth(): CalendarDate = copy(day = 1)

    fun monthYearLabel(): String = "${monthName(month)} $year"

    fun shortLabel(): String = "${monthShortName(month)} $day, $year"

    fun dayOfWeekFullName(): String = when (dayOfWeekMondayZero()) {
        0 -> "Monday"
        1 -> "Tuesday"
        2 -> "Wednesday"
        3 -> "Thursday"
        4 -> "Friday"
        5 -> "Saturday"
        else -> "Sunday"
    }

    fun dayOfWeekShortName(): String = dayOfWeekFullName().take(3).uppercase(Locale.US)

    companion object {
        fun today(): CalendarDate {
            val cal = Calendar.getInstance()
            return CalendarDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromEpochDay(epochDay: Long): CalendarDate {
            val z = epochDay + 719468L
            val era = (if (z >= 0) z else z - 146096L) / 146097L
            val doe = z - era * 146097L
            val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
            val y = yoe + era * 400L
            val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
            val mp = (5L * doy + 2L) / 153L
            val d = doy - (153L * mp + 2L) / 5L + 1L
            val m = mp + if (mp < 10L) 3L else -9L
            val finalYear = y + if (m <= 2L) 1L else 0L
            return CalendarDate(finalYear.toInt(), m.toInt(), d.toInt())
        }

        fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        fun daysInMonth(year: Int, month: Int): Int = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }

        fun monthName(month: Int): String = when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }

        fun monthShortName(month: Int): String = monthName(month).take(3)

        fun formatMinutes(totalMinutes: Int, use24Hour: Boolean): String {
            val clamped = totalMinutes.coerceIn(0, 1440)
            if (clamped == 1440) {
                return if (use24Hour) "24:00" else "12:00 AM"
            }
            val hour = clamped / 60
            val minute = clamped % 60
            return if (use24Hour) {
                String.format(Locale.US, "%02d:%02d", hour, minute)
            } else {
                val period = if (hour < 12) "AM" else "PM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format(Locale.US, "%d:%02d %s", displayHour, minute, period)
            }
        }

        fun formatTimeRange(
            startMinutes: Long,
            endMinutes: Long,
            allDay: Boolean,
            use24Hour: Boolean
        ): String {
            if (allDay) return "ALL DAY"
            val start = formatMinutes(startMinutes.toInt(), use24Hour)
            val end = formatMinutes(endMinutes.toInt(), use24Hour)
            return "$start – $end"
        }
    }
}
