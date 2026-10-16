package com.anindra.messages.data

import org.junit.Assert.*
import org.junit.Test

class ScheduledTrayTest {
    private fun row(id: Long, time: Long, conversation: Long = 1) =
        ScheduledMessage(id, "+15551230001", "Message $id", time, conversation, 1)

    @Test fun keepsOnlyThisConversationInStableSendOrder() {
        val rows = listOf(row(3, 200), row(4, 50, 2), row(2, 100), row(1, 100))
        assertEquals(listOf(1L, 2L, 3L), ScheduledTray.messages(rows, 1).map { it.id })
    }

    @Test fun overflowCountsOnlyMessagesBeyondTwoVisibleSlots() {
        assertEquals(0, ScheduledTray.overflowCount(0))
        assertEquals(0, ScheduledTray.overflowCount(1))
        assertEquals(0, ScheduledTray.overflowCount(2))
        assertEquals(1, ScheduledTray.overflowCount(3))
        assertEquals(98, ScheduledTray.overflowCount(100))
    }

    @Test fun deletingFirstMessagePromotesNextScheduledMessage() {
        val rows = listOf(row(1, 100), row(2, 200), row(3, 300))
        assertEquals(listOf(2L, 3L), ScheduledTray.messages(rows.drop(1), 1).take(2).map { it.id })
    }
}
