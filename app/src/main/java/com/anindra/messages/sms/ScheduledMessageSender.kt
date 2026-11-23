package com.anindra.messages.sms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anindra.messages.MessagesApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScheduledMessageSender : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1)
        val restore = intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!restore && id < 0) return

        val pendingResult = goAsync()
        val wakeLock = ReceiverWakeLock.acquire(context, "scheduled-send")
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (restore) restoreAlarms(context)
                else send(context, id, dueAt = System.currentTimeMillis())
            } catch (error: Exception) {
                android.util.Log.e("ScheduledMessageSender", "Scheduled SMS processing failed", error)
            } finally {
                wakeLock.safeRelease()
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "scheduled_id"
        private const val ACTION_SEND = "com.anindra.messages.SCHEDULED_SEND"

        suspend fun restoreAlarms(context: Context) {
            val repo = (context.applicationContext as MessagesApplication).repository
            repo.restoreScheduledAlarms { id, timestamp -> schedule(context, id, timestamp) }
        }

        fun schedule(context: Context, id: Long, triggerAtMillis: Long) {
            val alarmManager = checkNotNull(context.getSystemService(AlarmManager::class.java))
            // The wrapped Intent is explicit (component + package) and built with
            // plain statements in the SAME method as the AlarmManager sink, so
            // CodeQL's intra-procedural explicit-intent sanitizer applies.
            val intent = Intent(context, ScheduledMessageSender::class.java)
            intent.setPackage(context.packageName)
            intent.action = ACTION_SEND
            intent.putExtra(EXTRA_ID, id)
            val pendingIntent = PendingIntent.getBroadcast(
                context, id.toInt(), intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val exactAllowed = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            armScheduledAlarm(exactAllowed, exact = {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }, inexact = {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            })
        }

        suspend fun sendNow(context: Context, id: Long): Boolean = send(context, id)

        private suspend fun send(context: Context, id: Long, dueAt: Long? = null): Boolean =
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                val repo = (context.applicationContext as MessagesApplication).repository
                val (scheduled, stored) = repo.claimScheduledMessage(id, dueAt) ?: return@withContext false
                val accepted = runCatching {
                    cancel(context, id)
                    if (repo.isAddressBlocked(scheduled.address)) return@runCatching false
                    val recipients = repo.conversationRecipients(scheduled.conversationId)
                        .ifEmpty { listOf(scheduled.address) }
                        .filter { com.anindra.messages.ui.isPhoneNumber(it) && !repo.isAddressBlocked(it) }
                    val results = recipients.map { address ->
                        SmsSender.send(
                            context, stored.id, address, stored.body, scheduled.subId,
                            repo.settings.deliveryReportsEnabled
                        )
                    }
                    results.any { it }
                }.getOrDefault(false)
                if (!accepted) {
                    repo.markMessageStatusSuspend(stored.id, "failed")
                    NotificationHelper.showSendFailed(context, scheduled.address)
                }
                accepted
            }

        fun cancel(context: Context, id: Long) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val pendingIntent = buildPendingIntent(context, id)
            alarmManager.cancel(pendingIntent)
        }

        private fun buildPendingIntent(context: Context, id: Long): PendingIntent {
            val intent = Intent(context, ScheduledMessageSender::class.java).apply {
                setPackage(context.packageName)
                action = ACTION_SEND
                putExtra(EXTRA_ID, id)
            }
            return PendingIntent.getBroadcast(
                context, id.toInt(), intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

internal fun armScheduledAlarm(exactAllowed: Boolean, exact: () -> Unit, inexact: () -> Unit) {
    if (exactAllowed) {
        try {
            exact()
            return
        } catch (_: SecurityException) {
            // Android can revoke exact-alarm access between the check and registration.
        }
    }
    inexact()
}
