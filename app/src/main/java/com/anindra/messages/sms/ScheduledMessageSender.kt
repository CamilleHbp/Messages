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
        if (id < 0) return

        val pendingResult = goAsync()
        val wakeLock = ReceiverWakeLock.acquire(context, "scheduled-send")
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                send(context, id, dueAt = System.currentTimeMillis())
            } catch (_: Exception) {
            } finally {
                wakeLock.safeRelease()
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "scheduled_id"
        const val EXTRA_ADDRESS = "scheduled_address"
        const val EXTRA_BODY = "scheduled_body"
        const val EXTRA_SUB_ID = "scheduled_sub_id"
        private const val ACTION_SEND = "com.anindra.messages.SCHEDULED_SEND"

        fun schedule(context: Context, id: Long, address: String, body: String, subId: Int, triggerAtMillis: Long) {
            val alarmManager = checkNotNull(context.getSystemService(AlarmManager::class.java))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                check(alarmManager.canScheduleExactAlarms()) { "Exact alarm access is required" }
            }
            // The wrapped Intent is explicit (component + package) and built with
            // plain statements in the SAME method as the AlarmManager sink, so
            // CodeQL's intra-procedural explicit-intent sanitizer applies.
            val intent = Intent(context, ScheduledMessageSender::class.java)
            intent.setPackage(context.packageName)
            intent.action = ACTION_SEND
            if (address.isNotEmpty()) intent.putExtra(EXTRA_ADDRESS, address)
            if (body.isNotEmpty()) intent.putExtra(EXTRA_BODY, body)
            if (subId != -1) intent.putExtra(EXTRA_SUB_ID, subId)
            intent.putExtra(EXTRA_ID, id)
            val pendingIntent = PendingIntent.getBroadcast(
                context, id.toInt(), intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            // Preserve the existing user-visible alarm and its delivery in Doze.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = Intent(context, com.anindra.messages.MainActivity::class.java)
                showIntent.setPackage(context.packageName)
                val showPi = PendingIntent.getActivity(
                    context, id.toInt(), showIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPi)
                alarmManager.setAlarmClock(info, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
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
            val pendingIntent = buildPendingIntent(context, id, "", "", -1)
            alarmManager.cancel(pendingIntent)
        }

        /** PendingIntent equality is based on the Intent's component + action
         *  + data + type (not extras), so [cancel] can pass placeholder extras
         *  and still match the same alarm that [schedule] registered. */
        private fun buildPendingIntent(
            context: Context, id: Long, address: String, body: String, subId: Int
        ): PendingIntent {
            val intent = Intent(context, ScheduledMessageSender::class.java).apply {
                setPackage(context.packageName)
                action = ACTION_SEND
                if (address.isNotEmpty()) putExtra(EXTRA_ADDRESS, address)
                if (body.isNotEmpty()) putExtra(EXTRA_BODY, body)
                if (subId != -1) putExtra(EXTRA_SUB_ID, subId)
                putExtra(EXTRA_ID, id)
            }
            return PendingIntent.getBroadcast(
                context, id.toInt(), intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
