package com.anindra.messages.ui

import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelCountdownLabelTest {
    @Test fun everyTranslationShowsBothTheActionAndTime() {
        val resources = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main/res") }.first { it.isDirectory }
        val files = resources.walk().filter { it.name == "strings_conversations.xml" }.toList()
        assertTrue(files.isNotEmpty())
        for (file in files) {
            val strings = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(file).getElementsByTagName("string")
            val label = (0 until strings.length).map { strings.item(it) }.single {
                it.attributes.getNamedItem("name").nodeValue == "cancel_send_seconds"
            }
            val formatted = String.format(Locale.ROOT, label.textContent, "Cancel", 29)
            assertTrue(file.path, "Cancel" in formatted && "29" in formatted)
        }
    }
}
