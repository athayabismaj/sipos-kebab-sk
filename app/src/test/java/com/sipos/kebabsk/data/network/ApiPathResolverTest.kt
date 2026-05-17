package com.sipos.kebabsk.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiPathResolverTest {
    @Test
    fun `strips duplicated api prefix when base url already targets api`() {
        assertEquals(
            "operational-expenses",
            ApiPathResolver.resolve(
                baseUrl = "https://example.com/api/",
                endpoint = "api/operational-expenses"
            )
        )
    }

    @Test
    fun `keeps endpoint unchanged when base url does not include api path`() {
        assertEquals(
            "api/operational-expenses",
            ApiPathResolver.resolve(
                baseUrl = "https://example.com/",
                endpoint = "api/operational-expenses"
            )
        )
    }

    @Test
    fun `trims leading slash for dynamic retrofit urls`() {
        assertEquals(
            "cashier/daily-stock",
            ApiPathResolver.resolve(
                baseUrl = "https://example.com/api/",
                endpoint = "/cashier/daily-stock"
            )
        )
    }
}
