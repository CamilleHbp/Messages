package com.anindra.messages.data

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class ReviewAvailability(val missing: Set<Int>, val active: List<SimCard>?) {
    fun inactive(sim: Int) = sim >= 0 && active != null && active.none { it.subscriptionId == sim }
    fun ready(record: ComposerRecord) = missing.isEmpty() && !inactive(record.simId) &&
        (record.media.isNotEmpty() || record.draft.isNotBlank())

    companion object {
        suspend fun inspect(context: Context, record: ComposerRecord): ReviewAvailability = withContext(Dispatchers.IO) {
            val missing = record.media.indices.filterTo(mutableSetOf()) { i ->
                try {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(record.media[i]))
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.setTargetSampleSize((maxOf(info.size.width, info.size.height) / 96).coerceAtLeast(1))
                    }.recycle()
                    false
                } catch (_: Exception) { true }
            }
            ReviewAvailability(missing, SimCards.knownActive(context))
        }
    }
}
