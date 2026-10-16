package com.anindra.messages.ui

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
internal fun ScheduledTrayRow(
    message: ScheduledMessage,
    onManage: () -> Unit,
    overflowCount: Int = 0,
    onShowAll: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(message.body, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    stringResource(R.string.chat_scheduled_for, formatDateTime(message.timestamp, "MMM d,", is24HourFormat(context))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onManage, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Schedule, stringResource(R.string.settings_scheduled_manage_title))
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
internal fun ScheduledMessageActions(message: ScheduledMessage, onReschedule: () -> Unit, onSendNow: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Schedule, null) },
        title = { Text(stringResource(R.string.settings_scheduled_title)) },
        text = {
            Column {
                Text(message.body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState()))
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onReschedule, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings_scheduled_reschedule)) }
                TextButton(onClick = onSendNow, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.chat_send_now)) }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.chat_cancel_scheduled), color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.chat_close)) } }
    )
}
