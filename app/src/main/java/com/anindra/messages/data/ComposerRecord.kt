package com.anindra.messages.data

import org.json.JSONArray
import org.json.JSONObject

internal enum class ComposerState { DRAFT, QUEUED, REVIEW }

internal data class ComposerRecord(
    val draft: String,
    val media: List<String>,
    val simId: Int,
    val state: ComposerState = ComposerState.DRAFT,
    val deadline: Long = 0
) {
    fun remainingMillis(now: Long) = (deadline - now).coerceAtLeast(0L)
    fun restored(now: Long): ComposerRecord =
        if (state == ComposerState.QUEUED && now >= deadline) copy(state = ComposerState.REVIEW) else this

    fun encode(): String = JSONObject().apply {
        put("draft", draft)
        put("media", JSONArray(media))
        put("sim", simId)
        put("state", state.name)
        put("deadline", deadline)
    }.toString()

    companion object {
        fun decode(value: String): ComposerRecord {
            val obj = JSONObject(value)
            val photos = obj.getJSONArray("media")
            return ComposerRecord(obj.getString("draft"), List(photos.length()) { photos.getString(it) },
                obj.getInt("sim"), ComposerState.valueOf(obj.getString("state")), obj.getLong("deadline"))
        }
    }
}
