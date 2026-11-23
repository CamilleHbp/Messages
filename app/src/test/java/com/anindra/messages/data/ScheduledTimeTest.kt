package com.anindra.messages.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ScheduledTimeTest {

    private val chicago = ZoneId.of("America/Chicago")
    private val kolkata = ZoneId.of("Asia/Kolkata")
    private val utc = ZoneOffset.UTC

    /** Material's DatePicker hands back midnight UTC of the tapped day. */
    private fun pickerValue(date: LocalDate): Long =
        date.atStartOfDay(utc).toInstant().toEpochMilli()

    // --- the reported bug: picking "today" produced yesterday ------------

    @Test
    fun pickerValueIsReadAsTheCalendarDayNotShiftedByTheZone() {
        assertEquals(
            LocalDate.of(2026, 9, 28),
            ScheduledTime.selectedDate(pickerValue(LocalDate.of(2026, 9, 28)))
        )
    }

    @Test
    fun localStartOfDayStaysOnTheTappedDayWestOfUtc() {
        val start = ScheduledTime.localStartOfDay(pickerValue(LocalDate.of(2026, 9, 28)), chicago)
        val asLocal = java.time.Instant.ofEpochMilli(start).atZone(chicago).toLocalDate()
        assertEquals(LocalDate.of(2026, 9, 28), asLocal)
    }

    @Test
    fun localStartOfDayStaysOnTheTappedDayEastOfUtc() {
        val start = ScheduledTime.localStartOfDay(pickerValue(LocalDate.of(2026, 9, 28)), kolkata)
        val asLocal = java.time.Instant.ofEpochMilli(start).atZone(kolkata).toLocalDate()
        assertEquals(LocalDate.of(2026, 9, 28), asLocal)
    }

    @Test
    fun startOfDayIsMidnightInTheGivenZone() {
        val start = ScheduledTime.localStartOfDay(pickerValue(LocalDate.of(2026, 9, 28)), chicago)
        val local = java.time.Instant.ofEpochMilli(start).atZone(chicago)
        assertEquals(0, local.hour)
        assertEquals(0, local.minute)
    }

    // --- time-of-day -----------------------------------------------------

    @Test
    fun atTimeAppliesThePickedHourAndMinute() {
        val ts = ScheduledTime.atTime(pickerValue(LocalDate.of(2026, 9, 28)), 16, 36, chicago)
        val local = java.time.Instant.ofEpochMilli(ts).atZone(chicago)
        assertEquals(LocalDate.of(2026, 9, 28), local.toLocalDate())
        assertEquals(16, local.hour)
        assertEquals(36, local.minute)
        assertEquals(0, local.second)
    }

    @Test
    fun atTimeIsNotTheUtcMidnightThePickerReturned() {
        val picked = pickerValue(LocalDate.of(2026, 9, 28))
        assertTrue(ScheduledTime.atTime(picked, 9, 0, chicago) != picked)
    }

    @Test
    fun outOfRangeTimePartsAreClamped() {
        val ts = ScheduledTime.atTime(pickerValue(LocalDate.of(2026, 9, 28)), 99, 99, chicago)
        val local = java.time.Instant.ofEpochMilli(ts).atZone(chicago)
        assertEquals(23, local.hour)
        assertEquals(59, local.minute)
    }

    @Test fun initialPickerDayIsLocalOnBothSidesOfTheUtcDateBoundary() {
        val earlyUtc = java.time.Instant.parse("2026-09-28T02:00:00Z").toEpochMilli()
        val lateUtc = java.time.Instant.parse("2026-09-28T22:00:00Z").toEpochMilli()
        assertEquals(pickerValue(LocalDate.of(2026, 9, 27)), ScheduledTime.pickerDate(earlyUtc, chicago))
        assertEquals(pickerValue(LocalDate.of(2026, 9, 29)), ScheduledTime.pickerDate(lateUtc, kolkata))
    }

    @Test fun pickingAPastMinutePreservesTheSelectedDayForValidation() {
        val day = pickerValue(LocalDate.of(2026, 9, 28))
        val now = ScheduledTime.atTime(day, 15, 36, chicago)
        val selected = ScheduledTime.atTime(day, 9, 0, chicago)
        assertTrue(selected < now)
        assertEquals(day, ScheduledTime.pickerDate(selected, chicago))
    }
}
