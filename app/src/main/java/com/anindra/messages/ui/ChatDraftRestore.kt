package com.anindra.messages.ui

internal object ChatDraftRestore {
    fun restore(savedDraft: String, currentDraft: String, edited: Boolean): String =
        if (edited || currentDraft.isNotEmpty()) currentDraft else savedDraft
}
