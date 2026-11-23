package com.anindra.messages.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Turning the Material date/time pickers into the instant a scheduled message
 * should fire at.
 *
 * Material's `DatePicker` works in UTC: `selectedDateMillis` is midnight **UTC**
 * of the day the user tapped. Reading that as a local instant shifts the date by
 * a day for anyone west of Greenwich, so picking "today" produced yesterday.
 */
object ScheduledTime {

    /** The calendar day the user tapped, read out of the picker's UTC stamp. */
    fun selectedDate(selectedDateMillis: Long): LocalDate =
        Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneOffset.UTC).toLocalDate()

    /** Local midnight of the day [selectedDateMillis] represents. */
    fun localStartOfDay(selectedDateMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        selectedDate(selectedDateMillis).atStartOfDay(zone).toInstant().toEpochMilli()

    /** The instant at [hour]:[minute] on the day [selectedDateMillis] represents. */
    fun atTime(
        selectedDateMillis: Long,
        hour: Int,
        minute: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = selectedDate(selectedDateMillis)
        .atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    fun pickerDate(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
