package com.anindra.messages.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The "Delay before sending" picker in the new Inbox settings dialog must list
 * every delay option inside the dialog window. The option rows once used
 * `fillMaxSize()`, which made each row claim the dialog's whole height and
 * pushed every choice but the first off-screen — the dialog then rendered only
 * "1s". Anchor on the dialog source so the regression cannot return.
 */
class DelayDialogOptionsTest {

    private val main: File by lazy {
        generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main") }
            .firstOrNull { File(it, "java").isDirectory }
            ?: error("app/src/main not found")
    }

    private val delayDialog by lazy {
        File(main, "java/com/anindra/messages/ui/SendDelayDialog.kt").readText()
    }

    @Test
    fun eachOptionRowFillsWidthNotHeight() {
        assertTrue(
            "delay option rows must be fillMaxWidth so they stack inside the dialog",
            delayDialog.contains(".fillMaxWidth()")
        )
        assertFalse(
            "a fillMaxSize() option row claims the whole dialog height and hides the rest",
            delayDialog.contains(".fillMaxSize()")
        )
    }

    @Test
    fun everyDelayOptionIsOffered() {
        assertTrue(
            "the 1/3/5/10/30 s choices must all be listed",
            delayDialog.contains("listOf(0, 1, 3, 5, 10, 30)")
        )
        assertTrue(
            "each choice must render its seconds label",
            delayDialog.contains("R.string.settings_countdown_placeholder")
        )
    }

    @Test
    fun theDialogExplainsTheChoice() {
        assertTrue(
            "the delay dialog must explain what the options do",
            delayDialog.contains("R.string.settings_choose_delay")
        )
    }

    @Test
    fun theWholeRowSelectsTheOption() {
        assertTrue(
            "tapping the row, not only the radio, must pick the delay",
            delayDialog.contains(".selectable(")
        )
    }
}
