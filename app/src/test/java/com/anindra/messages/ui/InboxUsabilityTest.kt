package com.anindra.messages.ui

import com.anindra.messages.data.Conversation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxUsabilityTest {
    @Test fun confirmationIncludesOnlyUnreadInboxConversations() {
        val unread = Conversation(1, "5551234", "Alex", "", 123, 3, false)
        val rows = listOf(unread, unread.copy(unreadCount = 0), unread.copy(archived = true),
            unread.copy(blocked = true), unread.copy(deletedAt = 100))
        assertEquals(listOf(unread), unreadInbox(rows))
    }

    @Test fun recipientActionRequiresADialableAddress() {
        assertFalse(isPhoneNumber(""))
        assertFalse(isPhoneNumber("Alex"))
        assertTrue(isPhoneNumber("+1 (555) 123-4567"))
    }
}
