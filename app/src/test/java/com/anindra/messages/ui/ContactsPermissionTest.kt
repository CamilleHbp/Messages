package com.anindra.messages.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactsPermissionTest {
    @Test fun onlyNameSearchOffersContactAccess() {
        assertFalse(isContactNameQuery(""))
        assertFalse(isContactNameQuery("+1 (555) 0142"))
        assertTrue(isContactNameQuery("李"))
    }

    @Test fun requestInPlaceWhenAndroidCanPrompt() {
        assertFalse(contactsNeedSettings(false, false, false))
        assertFalse(contactsNeedSettings(false, true, true))
    }

    @Test fun showSettingsOnlyAfterFinalDenial() {
        assertTrue(contactsNeedSettings(false, true, false))
        assertFalse(contactsNeedSettings(true, true, false))
    }
}
