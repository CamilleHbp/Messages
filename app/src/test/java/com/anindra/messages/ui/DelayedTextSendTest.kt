package com.anindra.messages.ui

import org.junit.Assert.*
import org.junit.Test

class DelayedTextSendTest {
    @Test
    fun countdownRoundsUpAndUsesElapsedTimeInsteadOfTickCount() {
        val send = DelayedTextSend()
        send.start("Hello", 7, 5, 1000L)
        assertEquals(5, send.remainingSeconds(1000L))
        assertEquals(5, send.remainingSeconds(1001L))
        assertEquals(2, send.remainingSeconds(4500L))
        assertEquals(1, send.remainingSeconds(5999L))
        assertEquals(0, send.remainingSeconds(6000L))
        assertEquals(1000L, send.millisUntilNextTick(1000L))
        assertEquals(999L, send.millisUntilNextTick(1001L))
        assertEquals(500L, send.millisUntilNextTick(4500L))
        assertEquals(0L, send.millisUntilNextTick(6000L))
    }

    @Test
    fun repeatedSendDoesNotRestartOrReplacePendingTextAndSim() {
        val send = DelayedTextSend()
        assertTrue(send.start("  Hello  ", 7, 5, 1000L))
        assertFalse(send.start("Other", 9, 30, 2000L))
        assertNull(send.takeIfDue(5999L))
        val result = send.takeIfDue(6000L)!!
        assertEquals("Hello", result.body)
        assertEquals(7, result.simId)
        assertNull(send.takeIfDue(6000L))
        assertFalse(send.isPending)
    }

    @Test
    fun cancellationPreventsSendEvenAfterTheDeadline() {
        val send = DelayedTextSend()
        send.start("Keep this", 7, 5, 1000L)
        send.cancel()
        assertNull(send.takeIfDue(20000L))
        assertEquals(0, send.remainingSeconds(20000L))
        assertTrue(send.start("Edited text", 9, 5, 20000L))
        assertEquals("Edited text", send.takeIfDue(25000L)!!.body)
    }

    @Test
    fun completionPreservesAnyNewerComposerDraft() {
        val send = DelayedTextSend()
        send.start("  Original  ", 7, 1, 0L)
        val result = send.takeIfDue(1000L)!!
        assertEquals("", result.draftAfterSending("  Original  "))
        assertEquals("Next message", result.draftAfterSending("Next message"))
        assertEquals("Original changed", result.draftAfterSending("Original changed"))
    }

    @Test
    fun blankTextCannotBeArmedAndInvalidDelayHasACancelWindow() {
        val send = DelayedTextSend()
        assertFalse(send.start(" \n ", 7, 5, 0L))
        assertTrue(send.start("Hello", 7, 0, 0L))
        assertEquals(1, send.remainingSeconds(0L))
        assertNull(send.takeIfDue(0L))
    }
}
