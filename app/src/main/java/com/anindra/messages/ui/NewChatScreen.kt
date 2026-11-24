package com.anindra.messages.ui

import android.content.pm.PackageManager
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TextButton
import com.anindra.messages.data.ConversationList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anindra.messages.R

data class Contact(val name: String, val number: String, val workProfile: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(vm: com.anindra.messages.AppViewModel, onBack: () -> Unit, onPick: (String, String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    androidx.activity.compose.BackHandler(onBack = onBack)
    val contacts by vm.contacts.collectAsState()
    val recipient = query.trim()
    val context = LocalContext.current
    val permission = android.Manifest.permission.READ_CONTACTS
    fun hasContactsAccess() = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    var allowed by remember { mutableStateOf(hasContactsAccess()) }
    val activity = generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
        .filterIsInstance<android.app.Activity>().firstOrNull()
    val prefs = remember { context.getSharedPreferences("recipient_permissions", android.content.Context.MODE_PRIVATE) }
    fun needsSettings() = contactsNeedSettings(
        hasContactsAccess(), prefs.getBoolean("asked_contacts", false),
        activity?.shouldShowRequestPermissionRationale(permission) == true
    )
    var settingsRequired by remember { mutableStateOf(needsSettings()) }
    val requestContacts = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        prefs.edit().putBoolean("asked_contacts", true).apply()
        allowed = granted
        settingsRequired = needsSettings()
        if (granted) vm.reloadContacts()
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = hasContactsAccess()
                settingsRequired = needsSettings()
                if (allowed) vm.reloadContacts()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val filtered = remember(allowed, contacts, recipient) {
        if (!allowed) emptyList() else contacts.filter {
            ConversationList.matchesRecipient(it.name, it.number, recipient)
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.new_chat_title)) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.icon_back)) }
        }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.new_chat_hint)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.focusRequester(focusRequester).fillMaxWidth()
            )
            Spacer(Modifier.height(SettingsLayout.ROW_GAP))

            if (isPhoneNumber(recipient)) {
                GroupedRowCard(
                    position = RowPosition.SINGLE,
                    modifier = Modifier.padding(horizontal = SettingsLayout.SCREEN_PADDING)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onPick(recipient, recipient) }
                            .padding(SettingsLayout.ROW_CONTENT_PADDING)
                    ) {
                        PersonAvatar(recipient, size = 40.dp)
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(R.string.new_chat_send_to, recipient),
                            fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(SettingsLayout.GROUP_GAP))
            }
            if (!allowed) {
                if (settingsRequired) Text(stringResource(R.string.contacts_settings_hint),
                    modifier = Modifier.padding(horizontal = SettingsLayout.SCREEN_PADDING))
                TextButton(onClick = {
                    if (!settingsRequired) requestContacts.launch(permission)
                    else context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)))
                }) { Text(stringResource(if (settingsRequired) R.string.conversations_open_settings else R.string.recipient_contacts_access)) }
            } else if (!isPhoneNumber(recipient) && recipient.isNotEmpty() && filtered.isEmpty()) {
                Text(
                    stringResource(R.string.recipient_no_match, recipient),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = SettingsLayout.SCREEN_PADDING)
                )
            }

            LazyColumn {
                items(filtered, key = { it.number }) { contact ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(contact.number, contact.name) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        PersonAvatar(contact.number, size = 40.dp)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.name, fontWeight = FontWeight.Medium)
                                if (contact.workProfile) {
                                    Spacer(Modifier.width(6.dp))
                                    WorkProfileBadge()
                                }
                            }
                            Text(formatPhoneNumber(contact.number),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

internal fun contactsNeedSettings(granted: Boolean, previouslyAsked: Boolean, canAskAgain: Boolean) =
    !granted && previouslyAsked && !canAskAgain
