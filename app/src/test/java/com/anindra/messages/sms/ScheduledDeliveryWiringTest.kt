package com.anindra.messages.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ScheduledDeliveryWiringTest {
    private val source = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "app/src/main/java/com/anindra/messages") }
        .first { it.isDirectory }
    private val sender = File(source, "sms/ScheduledMessageSender.kt").readText()
    private val repository = File(source, "data/Repository.kt").readText()
    private val viewModel = File(source, "MainActivity.kt").readText()
    private val receive = sender.substringAfter("override fun onReceive(").substringBefore("companion object")
    private val claim = repository.substringAfter("suspend fun claimScheduledMessage(")
        .substringBefore("fun updateScheduledMessage(")
    private val dispatch = sender.substringAfter("private suspend fun send(").substringBefore("fun cancel(")

    @Test fun anAlarmUsesTheDatabaseInsteadOfStaleIntentPayload() {
        assertTrue(receive.contains("intent.getLongExtra(EXTRA_ID"))
        assertFalse(receive.contains("getStringExtra"))
        assertFalse(receive.contains("getIntExtra"))
        assertTrue(receive.contains("send(context, id, dueAt = System.currentTimeMillis())"))
        assertTrue(dispatch.contains("repo.claimScheduledMessage(id, dueAt) ?: return@withContext false"))
    }

    @Test fun aRescheduledMessageCannotBeSentByItsOldAlarm() {
        assertTrue(claim.contains("row.timestamp > dueAt"))
        assertTrue(claim.contains("return@runOnIoAsync null"))
        assertTrue(sender.contains("suspend fun sendNow(context: Context, id: Long): Boolean = send(context, id)"))
    }

    @Test fun claimingAndCreatingTheOutgoingMessageShareOneTransaction() {
        val begin = claim.indexOf("database.beginTransaction()")
        val read = claim.indexOf("scheduledMessageByIdBlocking(id)")
        val store = claim.indexOf("sendText(row.conversationId, row.body, row.subId)")
        val delete = claim.indexOf("database.delete(\"scheduled_messages\"")
        val commit = claim.indexOf("database.setTransactionSuccessful()")
        val finish = claim.indexOf("database.endTransaction()")
        assertTrue(begin >= 0 && read > begin && store > read)
        assertTrue(delete > store && commit > delete && finish > commit)
        assertTrue(claim.contains("finally"))
        assertTrue(claim.contains("?: error(\"Could not store scheduled message"))
    }

    @Test fun dispatchKeepsTheOriginalConversationAndSim() {
        assertTrue(claim.contains("sendText(row.conversationId, row.body, row.subId)"))
        assertFalse(sender.contains("getOrCreateConversationBlocking"))
        assertTrue(dispatch.contains("stored.id, address, stored.body, scheduled.subId"))
    }

    @Test fun aRejectedRadioHandoffMarksTheOutgoingMessageFailed() {
        assertTrue(dispatch.contains("val accepted = runCatching"))
        assertTrue(dispatch.contains("if (!accepted)"))
        assertTrue(dispatch.contains("repo.markMessageStatusSuspend(stored.id, \"failed\")"))
        assertTrue(dispatch.contains("NotificationHelper.showSendFailed(context, scheduled.address)"))
        assertTrue(dispatch.contains("accepted"))
    }

    @Test fun cancellationRemovesTheAuthoritativeRowBeforeCancelingItsAlarm() {
        val cancel = viewModel.substringAfter("fun cancelScheduledMessage(")
            .substringBefore("fun sendScheduledMessageNow(")
        assertTrue(cancel.indexOf("repo.deleteScheduledMessage(id)") >= 0)
        assertTrue(cancel.indexOf("repo.deleteScheduledMessage(id)") <
            cancel.indexOf("ScheduledMessageSender.cancel("))
        val intent = sender.substringAfter("private fun buildPendingIntent(")
        assertTrue(intent.contains("setPackage(context.packageName)"))
    }

    @Test fun reschedulingUpdatesAnExistingRowAndCannotReinsertAConsumedOne() {
        val update = repository.substringAfter("fun updateScheduledMessage(")
            .substringBefore("fun peekBackupFormat(")
        assertTrue(update.contains("database.update("))
        assertTrue(update.contains("require(updated > 0)"))
        assertFalse(update.contains("insert"))
    }

    @Test fun scheduledGroupsAttemptEveryUnblockedPhoneRecipient() {
        assertTrue(dispatch.contains("repo.conversationRecipients(scheduled.conversationId)"))
        assertTrue(dispatch.contains("repo.isAddressBlocked(scheduled.address)"))
        assertTrue(dispatch.contains("isPhoneNumber(it) && !repo.isAddressBlocked(it)"))
        assertTrue(dispatch.contains("recipients.map { address ->"))
        assertTrue(dispatch.contains("results.any { it }"))
    }

    @Test fun cancelingTheCallingScopeCannotLoseAnAlreadyClaimedMessage() {
        assertTrue(dispatch.contains("withContext(kotlinx.coroutines.NonCancellable)"))
    }

    @Test fun aRejectedAlarmRollsBackTheScheduleMutation() {
        val add = repository.substringAfter("fun addScheduledMessage(")
            .substringBefore("fun deleteScheduledMessage(")
        val update = repository.substringAfter("fun updateScheduledMessage(")
            .substringBefore("fun peekBackupFormat(")
        for (operation in listOf(add, update)) {
            assertTrue(operation.indexOf("database.beginTransaction()") <
                operation.indexOf("armAlarm("))
            assertTrue(operation.indexOf("armAlarm(") <
                operation.indexOf("database.setTransactionSuccessful()"))
            assertTrue(operation.contains("finally"))
            assertTrue(operation.contains("database.endTransaction()"))
        }
        val reschedule = viewModel.substringAfter("fun rescheduleMessage(")
            .substringBefore("fun exportCrashReports(")
        assertFalse(reschedule.contains("ScheduledMessageSender.cancel"))
        assertTrue(sender.contains("alarmManager.canScheduleExactAlarms()"))
    }
}
