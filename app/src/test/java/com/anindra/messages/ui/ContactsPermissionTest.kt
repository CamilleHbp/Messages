package com.anindra.messages.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactsPermissionTest {
    @Test fun requestInPlaceWhenAndroidCanPrompt() {
        assertFalse(contactsNeedSettings(false, false, false))
        assertFalse(contactsNeedSettings(false, true, true))
    }

    @Test fun showSettingsOnlyAfterFinalDenial() {
        assertTrue(contactsNeedSettings(false, true, false))
        assertFalse(contactsNeedSettings(true, true, false))
    }
}
