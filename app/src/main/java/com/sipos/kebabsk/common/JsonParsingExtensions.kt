package com.sipos.kebabsk.common

import com.google.gson.JsonObject

fun JsonObject.firstString(vararg keys: String): String? {
    for (key in keys) {
        val element = get(key) ?: continue

        if (
            element.isJsonNull ||
            !element.isJsonPrimitive
        ) {
            continue
        }

        val primitive = element.asJsonPrimitive

        if (!primitive.isString && !primitive.isNumber) {
            continue
        }

        val value = runCatching {
            primitive.asString
        }.getOrNull()

        if (!value.isNullOrBlank()) {
            return value
        }
    }

    return null
}

fun JsonObject.firstLong(vararg keys: String): Long? {
    for (key in keys) {
        val element = get(key) ?: continue

        if (
            element.isJsonNull ||
            !element.isJsonPrimitive
        ) {
            continue
        }

        val primitive = element.asJsonPrimitive

        val value = when {
            primitive.isNumber -> {
                runCatching {
                    primitive.asBigDecimal
                        .takeIf { it.stripTrailingZeros().scale() <= 0 }
                        ?.longValueExact()
                }.getOrNull()
            }

            primitive.isString -> {
                primitive.asString
                    .trim()
                    .toLongOrNull()
            }

            else -> null
        }

        if (value != null) {
            return value
        }
    }

    return null
}
