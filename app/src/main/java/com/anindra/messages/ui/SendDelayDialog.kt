package com.anindra.messages.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.anindra.messages.R

@Composable
internal fun sendDelayLabel(seconds: Int): String =
    if (seconds == 0) stringResource(R.string.send_delay_off)
    else stringResource(R.string.settings_countdown_placeholder, seconds)

@Composable
internal fun SendDelayDialog(selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.send_delay)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                Text(stringResource(R.string.settings_choose_delay))
                for (seconds in listOf(0, 1, 3, 5, 10, 30)) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(selected == seconds, role = Role.RadioButton) { onSelect(seconds) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected == seconds, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(sendDelayLabel(seconds))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}
