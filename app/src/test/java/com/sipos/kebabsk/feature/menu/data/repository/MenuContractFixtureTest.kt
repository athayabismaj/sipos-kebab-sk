package com.sipos.kebabsk.feature.menu.data.repository

import com.google.gson.Gson
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.menu.data.remote.MenusResponse
import com.sipos.kebabsk.testutil.ContractFixtureLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MenuContractFixtureTest {
    @Test
    fun menuFixtureMapsUserVariantPriceAndNullableFields() = runTest {
        val body = Gson().fromJson(
            ContractFixtureLoader.jsonObject("menu_success.json"),
            MenusResponse::class.java
        )
        val api = FixtureMenuApiService(body)

        val result = MenuRepositoryImpl(api).getMenus(
            token = "fixture-token",
            search = null,
            categoryId = null
        ).getOrThrow()

        assertEquals("Bearer fixture-token", api.authorization)
        assertEquals(101L, result.user.id)
        assertEquals("kasir", result.user.role)
        assertEquals(1, result.menus.size)
        assertEquals("Kebab Fixture", result.menus.single().name)
        assertEquals(null, result.menus.single().description)
        assertEquals(10_000L, result.menus.single().variants.single().price)
        assertEquals(
            "https://example.test/media/menu-variants/mini.webp",
            result.menus.single().variants.single().imageUrl
        )
        assertTrue(result.menus.single().variants.single().isAvailable)
        assertEquals(301L, result.categories.single().id)
        assertEquals(1, result.pagination.currentPage)
        assertFalse(result.pagination.hasMore)
        assertFalse(result.dailySession.isOpen)
        assertFalse(result.dailySession.isKnown)
    }
}

private class FixtureMenuApiService(
    private val responseBody: MenusResponse
) : MenuApiService {
    var authorization: String? = null
    var requestedPage: Int? = null
    var requestedPerPage: Int? = null

    override suspend fun getMenus(
        authorization: String,
        search: String?,
        categoryId: Long?,
        page: Int,
        perPage: Int
    ): Response<MenusResponse> {
        this.authorization = authorization
        requestedPage = page
        requestedPerPage = perPage
        return Response.success(responseBody)
    }
}
