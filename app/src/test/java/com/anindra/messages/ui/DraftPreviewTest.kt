package com.anindra.messages.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DraftPreviewTest {
    @Test fun countAndCaptionFitOnOnePreviewLine() {
        assertEquals("2 photos · Where next?", photoDraftPreview("2 photos", " Where\nnext? "))
        assertEquals("1 photo", photoDraftPreview("1 photo", " \n "))
    }
}
