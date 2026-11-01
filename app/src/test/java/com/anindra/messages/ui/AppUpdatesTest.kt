package com.anindra.messages.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class AppUpdatesTest {
    private val res = File("src/main/res")
    @Test fun updateLabelsPreserveBothVersionArgumentsInEveryLocale() {
        val folders = res.listFiles()!!.filter { it.name == "values" || it.name.matches(Regex("values-[a-z]{2}(-r[A-Z]{2})?")) }
        for (folder in folders) {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(folder, "app_updates.xml"))
            val strings = doc.getElementsByTagName("string")
            val values = (0 until strings.length).associate { val e = strings.item(it) as org.w3c.dom.Element; e.getAttribute("name") to e.textContent }
            assertFalse(folder.name, values.getValue("app_check_updates").isBlank())
            assertTrue(folder.name, values.getValue("app_installed_version").contains("%1\$s"))
            assertTrue(folder.name, values.getValue("app_installed_version").contains("%2\$d"))
        }
    }
}
