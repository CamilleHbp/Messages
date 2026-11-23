package com.anindra.messages.sms

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduledAlarmAccessTest {
    @Test fun usesExactAlarmWhenAvailable() {
        val calls = mutableListOf<String>()
        armScheduledAlarm(true, { calls.add("exact") }, { calls.add("inexact") })
        assertEquals(listOf("exact"), calls)
    }

    @Test fun schedulesWithoutExactAlarmAccess() {
        val calls = mutableListOf<String>()
        armScheduledAlarm(false, { calls.add("exact") }, { calls.add("inexact") })
        assertEquals(listOf("inexact"), calls)
    }

    @Test fun revocationDuringRegistrationStillSchedulesTheMessage() {
        val calls = mutableListOf<String>()
        armScheduledAlarm(true, {
            calls.add("exact")
            throw SecurityException()
        }, { calls.add("inexact") })
        assertEquals(listOf("exact", "inexact"), calls)
    }

    @Test(expected = IllegalStateException::class)
    fun failureToRegisterEitherAlarmPropagatesForDatabaseRollback() {
        armScheduledAlarm(false, {}, { throw IllegalStateException() })
    }
}
