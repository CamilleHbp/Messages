package com.anindra.messages.data

internal object ScheduledTray {
    fun messages(rows: List<ScheduledMessage>, conversationId: Long): List<ScheduledMessage> =
        rows.filter { it.conversationId == conversationId }
            .sortedWith(compareBy<ScheduledMessage> { it.timestamp }.thenBy { it.id })

    fun overflowCount(size: Int): Int = (size - 1).coerceAtLeast(0)
}
