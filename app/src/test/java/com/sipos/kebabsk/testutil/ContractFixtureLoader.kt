package com.sipos.kebabsk.testutil

import com.google.gson.JsonObject
import com.google.gson.JsonParser

object ContractFixtureLoader {
    fun jsonObject(name: String): JsonObject {
        val resource = requireNotNull(
            ContractFixtureLoader::class.java.classLoader?.getResourceAsStream("contracts/$name")
        ) { "Contract fixture not found: $name" }

        return resource.bufferedReader().use { reader ->
            JsonParser.parseReader(reader).asJsonObject
        }
    }
}
