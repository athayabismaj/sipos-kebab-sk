package com.sipos.kebabsk.common

object VariantDisplayUtils {

    fun formatVariantName(
        menuName: String,
        variantName: String
    ): String {
        val menu = menuName.trim()
        val variant = variantName.trim()

        if (menu.isBlank() || variant.isBlank()) {
            return variant
        }

        if (variant.equals(menu, ignoreCase = true)) {
            return variant
        }

        if (!variant.startsWith(menu, ignoreCase = true)) {
            return variant
        }

        val remaining = variant
            .substring(menu.length)

        val hasValidBoundary =
            remaining.startsWith(" ") ||
            remaining.startsWith("-") ||
            remaining.startsWith("–") ||
            remaining.startsWith(":")

        if (!hasValidBoundary) {
            return variant
        }

        return remaining
            .trim()
            .trimStart('-', '–', ':')
            .trim()
            .ifBlank { variant }
    }
}
