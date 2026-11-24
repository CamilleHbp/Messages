package com.anindra.messages.ui

internal class DelayedTextSend {
    data class Pending(val draft: String, val simId: Int, val deadlineMillis: Long, val media: List<String>) {
        val body: String get() = draft.trim()
    }

    var pending: Pending? = null
        private set
    val isPending: Boolean get() = pending != null

    fun start(draft: String, simId: Int, delaySeconds: Int, nowMillis: Long, media: List<String> = emptyList()): Boolean {
        if (pending != null || (draft.isBlank() && media.isEmpty())) return false
        pending = Pending(draft, simId, nowMillis + delaySeconds.coerceAtLeast(1) * 1000L, media.toList())
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
