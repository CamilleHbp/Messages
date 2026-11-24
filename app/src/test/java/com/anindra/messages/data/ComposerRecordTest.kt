package com.anindra.messages.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ComposerRecordTest {
    private val record = ComposerRecord("caption", listOf("second.jpg", "first.jpg"), 2, ComposerState.QUEUED, 1000)

    @Test fun activeCountdownUsesItsOriginalDeadline() {
        assertEquals(record, record.restored(999))
        assertEquals(1L, record.remainingMillis(999))
    }

    @Test fun expiryRequiresReviewAndPreservesTheEntireMessage() {
        val review = record.copy(state = ComposerState.REVIEW)
        assertEquals(review, record.restored(1000))
        assertEquals(review, record.restored(2000))
        assertEquals(review, review.restored(500))
        assertEquals(0L, review.remainingMillis(2000))
    }

    @Test fun draftsStayEditableAcrossRestarts() {
        val draft = record.copy(state = ComposerState.DRAFT, deadline = 0)
        assertEquals(draft, draft.restored(2000))
    }
}
