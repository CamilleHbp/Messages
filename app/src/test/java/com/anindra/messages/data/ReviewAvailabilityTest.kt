package com.anindra.messages.data

import org.junit.Assert.*
import org.junit.Test

class ReviewAvailabilityTest {
    private val record = ComposerRecord("caption", listOf("a", "b"), 7, ComposerState.REVIEW)
    @Test fun repairStartsWithTheEarliestUnavailablePhoto() {
        assertEquals(1, ReviewAvailability(setOf(5, 1), null).firstMissing)
        assertNull(ReviewAvailability(emptySet(), null).firstMissing)
    }

    @Test fun everyPhotoMustBeReadable() {
        for (missing in listOf(setOf(0), setOf(1), setOf(0, 1))) {
            assertFalse(ReviewAvailability(missing, null).ready(record))
        }
        assertTrue(ReviewAvailability(emptySet(), null).ready(record))
        assertFalse(ReviewAvailability(emptySet(), null).ready(record.copy(draft = "", media = emptyList())))
    }

    @Test fun confirmedInactiveSimBlocksButUnknownInventoryDoesNot() {
        assertFalse(ReviewAvailability(emptySet(), emptyList()).ready(record))
        assertFalse(ReviewAvailability(emptySet(), SimCards.debugFake().take(1)).ready(record))
        assertTrue(ReviewAvailability(emptySet(), SimCards.debugFake()).ready(record))
        assertTrue(ReviewAvailability(emptySet(), null).ready(record))
    }
}
