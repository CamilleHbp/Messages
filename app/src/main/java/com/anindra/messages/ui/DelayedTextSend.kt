package com.anindra.messages.ui

internal class DelayedTextSend {
    data class Pending(val draft: String, val simId: Int, val deadlineMillis: Long) {
        val body: String get() = draft.trim()

        fun draftAfterSending(currentDraft: String): String =
            if (currentDraft == draft) "" else currentDraft
    }

    private var pending: Pending? = null
    val isPending: Boolean get() = pending != null

    fun start(draft: String, simId: Int, delaySeconds: Int, nowMillis: Long): Boolean {
        if (pending != null || draft.isBlank()) return false
        pending = Pending(draft, simId, nowMillis + delaySeconds.coerceAtLeast(1) * 1000L)
        return true
    }

    fun remainingSeconds(nowMillis: Long): Int {
        val remaining = (pending?.deadlineMillis ?: return 0) - nowMillis
        return ((remaining.coerceAtLeast(0L) + 999L) / 1000L).toInt()
    }

    fun millisUntilNextTick(nowMillis: Long): Long {
        val remaining = (pending?.deadlineMillis ?: return 0L) - nowMillis
        if (remaining <= 0L) return 0L
        return ((remaining - 1L) % 1000L) + 1L
    }

    fun cancel() {
        pending = null
    }

    fun takeIfDue(nowMillis: Long): Pending? {
        val request = pending ?: return null
        if (nowMillis < request.deadlineMillis) return null
        pending = null
        return request
    }
}
