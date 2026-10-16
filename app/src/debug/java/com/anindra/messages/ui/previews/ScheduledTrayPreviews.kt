package com.anindra.messages.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.anindra.messages.ui.*
import com.anindra.messages.ui.theme.MessagesTheme

@PreviewLightDark
@Composable
private fun ScheduledMessageTrayPreview() {
    MessagesTheme {
        ScheduledMessageTray(listOf(sampleScheduled(1, "+15551230001", "See you at six"), sampleScheduled(2, "+15551230001", "I will bring dinner"), sampleScheduled(3, "+15551230001", "On my way")), {})
    }
}

@PreviewLightDark
@Composable
private fun ScheduledTrayRowPreview() {
    MessagesTheme { ScheduledTrayRow(sampleScheduled(1, "+15551230001", "See you at six"), {}) }
}

@PreviewLightDark
@Composable
private fun ScheduledTraySheetPreview() {
    MessagesTheme { ScheduledTraySheet(listOf(sampleScheduled(1, "+15551230001", "See you at six")), {}, {}) }
}

@PreviewLightDark
@Composable
private fun ScheduledMessageActionsPreview() {
    MessagesTheme { ScheduledMessageActions(sampleScheduled(1, "+15551230001", "See you at six"), {}, {}, {}, {}) }
}
