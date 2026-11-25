package com.anindra.messages.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.anindra.messages.sms.ScheduledSendResult
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anindra.messages.R
import com.anindra.messages.data.ScheduledMessage
import com.anindra.messages.data.ScheduledTray

@Composable
internal fun ScheduledMessageTray(messages: List<ScheduledMessage>, onManage: (ScheduledMessage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    if (messages.isEmpty()) return
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)) {
        val message = messages.first()
        ScheduledTrayRow(
            message,
            onManage = { onManage(message) },
            overflowCount = ScheduledTray.overflowCount(messages.size),
            onShowAll = { expanded = true }
        )
    }
    if (expanded) {
        ScheduledTraySheet(messages, onManage, onDismiss = { expanded = false })
    }
}

@Composable
private fun scheduledForLabel(message: ScheduledMessage): String {
    val context = LocalContext.current
    val timestamp = formatScheduledTime(message.timestamp, is24HourFormat(context), context.resources.configuration.locales[0])
    return stringResource(R.string.chat_scheduled_for, timestamp)
}

@Composable
internal fun ScheduledTrayRow(
    message: ScheduledMessage,
    onManage: () -> Unit,
    overflowCount: Int = 0,
    onShowAll: (() -> Unit)? = null
) {
    val manageLabel = stringResource(R.string.settings_scheduled_manage_title)
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).heightIn(min = 56.dp)
                    .clickable(role = Role.Button, onClickLabel = manageLabel, onClick = onManage)
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(message.body, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        scheduledForLabel(message),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.Schedule, contentDescription = null)
            }
            if (overflowCount > 0 && onShowAll != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onShowAll, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text(stringResource(R.string.chat_scheduled_more, overflowCount))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduledTraySheet(messages: List<ScheduledMessage>, onManage: (ScheduledMessage) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(stringResource(R.string.settings_scheduled_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { message ->
                ScheduledTrayRow(message, onManage = { onDismiss(); onManage(message) })
            }
        }
    }
}

@Composable
internal fun ScheduledMessageActions(message: ScheduledMessage, onReschedule: () -> Unit, onSendNow: ((ScheduledSendResult) -> Unit) -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
    var confirmingCancel by remember(message.id) { mutableStateOf(false) }
    var sending by remember(message.id) { mutableStateOf(false) }
    var result by remember(message.id) { mutableStateOf<ScheduledSendResult?>(null) }
    val guidance = when (result) {
        ScheduledSendResult.NoLongerScheduled -> R.string.schedule_gone
        ScheduledSendResult.CouldNotStart -> R.string.schedule_try_again
        ScheduledSendResult.Blocked -> R.string.schedule_blocked
        else -> null
    }
    LaunchedEffect(result) {
        if (result == ScheduledSendResult.Submitted || result == ScheduledSendResult.FailedInChat) onDismiss()
    }
    val dismiss = { if (confirmingCancel) confirmingCancel = false else onDismiss() }
    AlertDialog(
        onDismissRequest = dismiss,
        icon = { Icon(Icons.Rounded.Schedule, null) },
        title = { Text(stringResource(if (confirmingCancel) R.string.chat_confirm_cancel_schedule else R.string.settings_scheduled_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(scheduledForLabel(message), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(message.body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState()))
                if (guidance != null && !confirmingCancel) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(guidance), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                if (!confirmingCancel && result != ScheduledSendResult.NoLongerScheduled) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(enabled = !sending, onClick = onReschedule, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings_scheduled_reschedule)) }
                    TextButton(enabled = !sending, onClick = {
                        sending = true
                        onSendNow {
                            sending = false
                            result = it
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (sending) R.string.status_sending else R.string.chat_send_now))
                    }
                    TextButton(enabled = !sending, onClick = { confirmingCancel = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.chat_cancel_scheduled), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            if (confirmingCancel) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.chat_cancel_scheduled), color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.chat_close)) }
            }
        },
        dismissButton = {
            if (confirmingCancel) {
                TextButton(onClick = { confirmingCancel = false }) { Text(stringResource(R.string.chat_keep_scheduled)) }
            }
        }
    )
}
