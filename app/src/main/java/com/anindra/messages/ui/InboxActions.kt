package com.anindra.messages.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anindra.messages.R
import com.anindra.messages.data.Conversation

internal fun unreadInbox(conversations: List<Conversation>) = conversations.filter {
    it.unreadCount > 0 && !it.archived && !it.blocked && it.deletedAt == 0L
}

@Composable
internal fun InboxActions(
    conversations: List<Conversation>,
    onOpenSettings: () -> Unit,
    onMarkRead: (List<Conversation>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var confirmed by remember { mutableStateOf<List<Conversation>?>(null) }
    IconButton(onClick = onOpenSettings) {
        Icon(Icons.Rounded.Settings, stringResource(R.string.settings_general))
    }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, stringResource(R.string.icon_more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val unread = remember(conversations) { unreadInbox(conversations) }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings_mark_read_title)) },
                enabled = unread.isNotEmpty(),
                onClick = { expanded = false; confirmed = unread }
            )
        }
    }
    confirmed?.let { targets ->
        AlertDialog(
            onDismissRequest = { confirmed = null },
            title = { Text(stringResource(R.string.settings_mark_read_title)) },
            text = { Text(stringResource(R.string.inbox_unread_count, targets.size)) },
            confirmButton = {
                TextButton(onClick = { confirmed = null; onMarkRead(targets) }) {
                    Text(stringResource(R.string.settings_mark_read_title))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmed = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
internal fun EmptyConversationList(archived: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(if (archived) R.string.archive_empty_title else R.string.inbox_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (archived) Text(
            stringResource(R.string.archive_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
