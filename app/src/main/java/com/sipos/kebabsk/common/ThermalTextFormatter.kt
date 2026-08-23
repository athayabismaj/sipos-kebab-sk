package com.sipos.kebabsk.common

object ThermalTextFormatter {

    fun wrap(text: String, width: Int): List<String> {
        require(width > 0) { "Lebar baris harus lebih dari nol." }

        val words = text
            .trim()
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)

        if (words.isEmpty()) return emptyList()

        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            if (currentLine.isNotEmpty() && currentLine.length + 1 + word.length <= width) {
                currentLine += " $word"
                return@forEach
            }

            if (currentLine.isNotEmpty()) {
                lines += currentLine
                currentLine = ""
            }

            var remaining = word
            while (remaining.length > width) {
                lines += remaining.take(width)
                remaining = remaining.drop(width)
            }
            currentLine = remaining
        }

        if (currentLine.isNotEmpty()) lines += currentLine
        return lines
    }
}
