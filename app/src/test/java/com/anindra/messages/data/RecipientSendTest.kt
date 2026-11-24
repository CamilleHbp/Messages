package com.anindra.messages.data

import org.junit.Assert.*
import org.junit.Test

class RecipientSendTest {
    @Test fun callbacksPreserveMultipartProgressAndIgnoreOldAttempts() {
        val row = RecipientSend("111", attempt = 2)
        val progress = row.result(2, 1, 2, true, true)
        assertEquals("sending", progress.status)
        assertEquals(progress, progress.result(1, 0, 2, false, false))
        assertEquals(progress, progress.result(2, 1, 2, true, true))
        val sent = progress.result(2, 0, 2, false, true)
        assertEquals("sent", sent.status)
        assertEquals(sent, sent.result(2, 0, 2, true, false))
        assertEquals("delivered", sent.result(2, 0, 2, true, true).status)
    }

    @Test fun failedRecipientsComeFirstAndKeepTheirCause() {
        val sent = RecipientSend("111").result(1, 0, 1, false, true)
        val failed = RecipientSend("222").result(1, 0, 1, false, false, "no_service")
        assertEquals("no_service", failed.failureReason)
        assertEquals(listOf(failed, sent), RecipientSend.displayOrder(listOf(sent, failed)))
        assertEquals("", sent.result(1, 0, 1, true, false, "radio_off").failureReason)
    }

    @Test fun oneFailedRecipientDoesNotEraseAnotherRecipientsResult() {
        val sent = RecipientSend("111").result(1, 0, 1, false, true)
        val failed = RecipientSend("222").result(1, 0, 1, false, false)
        assertEquals("partial", RecipientSend.aggregate(listOf(sent, failed)))
        assertEquals(failed, failed.result(1, 0, 1, false, true))
    }
}
