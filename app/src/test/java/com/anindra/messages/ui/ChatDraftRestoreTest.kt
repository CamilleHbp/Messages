package com.anindra.messages.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatDraftRestoreTest {
    @Test
    fun restoresSavedTextWhenConversationFirstLoads() {
        assertEquals("Saved message", ChatDraftRestore.restore("Saved message", "", false))
    }

    @Test
    fun conversationLoadPreservesNewerInputAndSharedText() {
        assertEquals("New input", ChatDraftRestore.restore("Saved message", "New input", true))
        assertEquals("Shared text", ChatDraftRestore.restore("Saved message", "Shared text", false))
    }

    @Test
    fun clearingTheComposerBeforeLoadingDoesNotBringOldTextBack() {
        assertEquals("", ChatDraftRestore.restore("Saved message", "", true))
        assertEquals(" ", ChatDraftRestore.restore("Saved message", " ", true))
    }
}
