package com.anindra.messages.ui

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduledTimeLabelTest {
    private val timestamp = Instant.parse("2027-01-01T00:05:00Z").toEpochMilli()

    @Test fun dateAndClockReferToTheSameLocalDayAcrossNewYear() {
        assertEquals(
            "Dec 31, 2026 • 4:05 PM",
            formatScheduledTime(timestamp, false, Locale.US, ZoneId.of("America/Los_Angeles"))
        )
    }

    @Test fun localizedDateRetainsTheYearAndRequestedClock() {
        assertEquals(
            "1 janv. 2027 • 01:05",
            formatScheduledTime(timestamp, true, Locale.FRANCE, ZoneId.of("Europe/Paris"))
        )
    }
}
