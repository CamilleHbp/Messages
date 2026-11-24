package com.anindra.messages.data

import org.json.JSONArray
import org.json.JSONObject

data class RecipientSend(
    val address: String,
    val attempt: Int = 1,
    val parts: Int = 1,
    val sent: Set<Int> = emptySet(),
    val delivered: Set<Int> = emptySet(),
    val failed: Boolean = false
) {
    val status: String get() = when {
        failed -> "failed"
        delivered.size == parts -> "delivered"
        sent.size == parts -> "sent"
        else -> "sending"
    }

    fun result(generation: Int, part: Int, count: Int, receipt: Boolean, ok: Boolean): RecipientSend {
        if (attempt != generation || part !in 0 until count || failed) return this
        if (!ok) return if (receipt || part in sent) this else copy(failed = true)
        return copy(parts = count, sent = sent + part, delivered = if (receipt) delivered + part else delivered)
    }

    companion object {
        fun aggregate(rows: List<RecipientSend>): String = when {
            rows.all { it.status == "delivered" } -> "delivered"
            rows.all { it.failed } -> "failed"
            rows.any { it.failed } -> "partial"
            rows.any { it.status == "sending" } -> "sending"
            else -> "sent"
        }

        fun encode(rows: List<RecipientSend>): String = JSONArray().apply {
            rows.forEach { r -> put(JSONObject().apply {
                put("address", r.address); put("attempt", r.attempt); put("parts", r.parts)
                put("sent", JSONArray(r.sent)); put("delivered", JSONArray(r.delivered)); put("failed", r.failed)
            }) }
        }.toString()

        fun decode(raw: String): List<RecipientSend> {
            if (raw.isEmpty()) return emptyList()
            val array = JSONArray(raw)
            fun indices(a: JSONArray) = (0 until a.length()).map(a::getInt).toSet()
            return List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                RecipientSend(obj.getString("address"), obj.getInt("attempt"), obj.getInt("parts"),
                    indices(obj.getJSONArray("sent")), indices(obj.getJSONArray("delivered")), obj.getBoolean("failed"))
            }
        }
    }
}
