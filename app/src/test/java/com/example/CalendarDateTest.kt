package com.example

import com.example.data.model.CalendarDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarDateTest {
    @Test
    fun epochDay_roundTripsAccurately() {
        val samples = listOf(
            CalendarDate(1970, 1, 1) to 0L,
            CalendarDate(2024, 2, 29) to 19782L,
            CalendarDate(2026, 11, 25) to 20782L
        )
        for ((date, expectedEpoch) in samples) {
            assertEquals(expectedEpoch, date.toEpochDay())
            assertEquals(date, CalendarDate.fromEpochDay(expectedEpoch))
        }
    }

    @Test
    fun dayOfWeek_andLeapYear_areAccurate() {
        // 2026-11-25 is Wednesday (Monday = 0 -> Wednesday = 2)
        assertEquals(2, CalendarDate(2026, 11, 25).dayOfWeekMondayZero())
        // 1970-01-01 was Thursday (Monday = 0 -> Thursday = 3)
        assertEquals(3, CalendarDate(1970, 1, 1).dayOfWeekMondayZero())

        assertTrue(CalendarDate.isLeapYear(2024))
        assertFalse(CalendarDate.isLeapYear(2100))
        assertTrue(CalendarDate.isLeapYear(2000))
        assertEquals(29, CalendarDate.daysInMonth(2024, 2))
        assertEquals(28, CalendarDate.daysInMonth(2026, 2))
    }

    @Test
    fun formatMinutes_handles12And24Hour() {
        assertEquals("09:05", CalendarDate.formatMinutes(545, use24Hour = true))
        assertEquals("9:05 AM", CalendarDate.formatMinutes(545, use24Hour = false))
        assertEquals("12:00 PM", CalendarDate.formatMinutes(720, use24Hour = false))
        assertEquals("12:30 AM", CalendarDate.formatMinutes(30, use24Hour = false))
        assertEquals("ALL DAY", CalendarDate.formatTimeRange(0, 1440, allDay = true, use24Hour = false))
    }
}
