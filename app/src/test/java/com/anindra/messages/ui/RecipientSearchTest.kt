package com.anindra.messages.ui

import com.anindra.messages.data.ConversationList
import org.junit.Assert.*
import org.junit.Test

class RecipientSearchTest {
    @Test fun namesAndFormattedNumbersUseTheInboxRules() {
        val name = "José García"
        val number = "+1 (415) 555-0168"
        fun matches(query: String) = ConversationList.matchesRecipient(name, number, query)
        assertTrue(matches("jose"))
        assertTrue(matches("GARCIA"))
        assertTrue(matches("415555"))
        assertFalse(matches("410999"))
        assertFalse(matches("Jules"))
    }
}
