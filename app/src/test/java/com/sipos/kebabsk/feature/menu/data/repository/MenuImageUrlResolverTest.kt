package com.sipos.kebabsk.feature.menu.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class MenuImageUrlResolverTest {
    private val apiBaseUrl = "http://192.168.1.5:8000/api/"

    @Test
    fun localhostImageUsesTheReachableApiOrigin() {
        assertEquals(
            "http://192.168.1.5:8000/media/menu-variants/menu.webp",
            resolveMenuImageUrl(
                "http://localhost/media/menu-variants/menu.webp",
                apiBaseUrl
            )
        )
    }

    @Test
    fun relativeImageUsesTheApiOriginWithoutTheApiPath() {
        assertEquals(
            "http://192.168.1.5:8000/media/menu-variants/menu.webp",
            resolveMenuImageUrl("/media/menu-variants/menu.webp", apiBaseUrl)
        )
    }

    @Test
    fun validExternalImageOriginIsPreserved() {
        assertEquals(
            "https://cdn.example.test/menu.webp",
            resolveMenuImageUrl("https://cdn.example.test/menu.webp", apiBaseUrl)
        )
    }
}
