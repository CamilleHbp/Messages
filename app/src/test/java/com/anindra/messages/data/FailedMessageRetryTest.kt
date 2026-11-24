package com.anindra.messages.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FailedMessageRetryTest {
    private val photo = Message(2, 3, "Original caption", 123L, true, "failed", "image", "content://photo/2", subId = 7)

    @Test fun aReplacementSimCanBeChosenEvenWhenOnlyOneLineRemains() {
        val remaining = SimCard(1, 0, null, null, null, null, false)
        assertEquals(true, SimSwitcher.canChooseRetrySim(7, listOf(remaining)))
        assertEquals(false, SimSwitcher.canChooseRetrySim(1, listOf(remaining)))
    }

    @Test fun retryReusesTheOriginalMessageAndSim() {
        assertEquals(photo, photo.forRetry(null))
        assertEquals(-1, photo.copy(subId = -1).forRetry(null).subId)
    }

    @Test fun choosingAnotherSimKeepsThePhotoCaptionAndIdentity() {
        assertEquals(photo.copy(subId = 1), photo.forRetry(1))
    }
}
