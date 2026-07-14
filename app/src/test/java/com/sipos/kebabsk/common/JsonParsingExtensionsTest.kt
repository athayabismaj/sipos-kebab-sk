package com.sipos.kebabsk.common

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonParsingExtensionsTest {

    @Test
    fun `firstString returns null when field is missing`() {
        val obj = JsonObject()
        assertNull(obj.firstString("name", "title"))
    }

    @Test
    fun `firstString returns null when field is json null`() {
        val obj = JsonObject().apply { add("name", com.google.gson.JsonNull.INSTANCE) }
        assertNull(obj.firstString("name"))
    }

    @Test
    fun `firstString returns string when field is string`() {
        val obj = JsonObject().apply { addProperty("name", "Kebab") }
        assertEquals("Kebab", obj.firstString("name"))
    }

    @Test
    fun `firstString returns string when field is number`() {
        val obj = JsonObject().apply { addProperty("qty", 15) }
        assertEquals("15", obj.firstString("qty"))
    }

    @Test
    fun `firstString ignores object and array without crashing`() {
        val obj = JsonObject().apply {
            add("child", JsonObject())
            add("list", JsonArray())
            addProperty("valid", "Kebab")
        }
        assertNull(obj.firstString("child"))
        assertNull(obj.firstString("list"))
        assertEquals("Kebab", obj.firstString("child", "list", "valid"))
    }

    @Test
    fun `firstString ignores boolean`() {
        val obj = JsonObject().apply { addProperty("is_active", true) }
        assertNull(obj.firstString("is_active"))
    }

    @Test
    fun `firstLong parses integer json to Long`() {
        val obj = JsonObject().apply { addProperty("id", 1500) }
        assertEquals(1500L, obj.firstLong("id"))
    }

    @Test
    fun `firstLong parses string containing integer to Long`() {
        val obj = JsonObject().apply { addProperty("id", " 1500 ") }
        assertEquals(1500L, obj.firstLong("id"))
    }

    @Test
    fun `firstLong returns null for decimal json`() {
        val obj = JsonObject().apply { addProperty("qty", 15.5) }
        assertNull(obj.firstLong("qty"))
    }

    @Test
    fun `firstLong returns null for string containing decimal`() {
        val obj = JsonObject().apply { addProperty("qty", "15.5") }
        assertNull(obj.firstLong("qty"))
    }

    @Test
    fun `firstLong returns null on overflow`() {
        val obj = JsonObject().apply { addProperty("big", "999999999999999999999") }
        assertNull(obj.firstLong("big"))
    }

    @Test
    fun `firstLong continues to next key if first is invalid`() {
        val obj = JsonObject().apply {
            addProperty("id1", "invalid")
            add("id2", JsonObject())
            addProperty("id3", 15.5)
            addProperty("id4", 42L)
        }
        assertEquals(42L, obj.firstLong("id1", "id2", "id3", "id4"))
    }
}
