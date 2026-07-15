package com.sipos.kebabsk.feature.dailystock.data.repository

import com.google.gson.JsonObject
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.testutil.ContractFixtureLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class DailyStockContractFixtureTest {
    @Test
    fun openSessionMapsSessionAndIngredientContract() = runTest {
        val api = FixtureDailyStockApiService("stock_session_open.json")
        val result = DailyStockRepositoryImpl(api).getDailyStock("fixture-token").getOrThrow()

        assertEquals("Bearer fixture-token", api.getAuthorization)
        assertEquals(801L, result.sessionId)
        assertEquals(1, result.items.size)
        assertEquals(901L, result.items.single().ingredientId)
        assertEquals("Tortila Fixture", result.items.single().name)
        assertEquals(20.0, result.items.single().qty, 0.0)
        assertEquals(18.0, result.items.single().remainingQty ?: -1.0, 0.0)
        assertEquals("pcs", result.items.single().unit)
    }

    @Test
    fun closedSessionKeepsCurrentRepositoryGateWithoutInventingStock() = runTest {
        val api = FixtureDailyStockApiService("stock_session_closed.json")

        val result = DailyStockRepositoryImpl(api).getDailyStock("token")

        assertTrue(result.isFailure)
        assertEquals("Sesi stok harian belum dibuka oleh admin.", result.exceptionOrNull()?.message)
    }

    @Test
    fun closeSessionUsesValidEndpointBodyAndOmitsBlankNotes() = runTest {
        val api = FixtureDailyStockApiService("stock_session_open.json")
        val repository = DailyStockRepositoryImpl(api)

        val result = repository.closeSession(
            token = "fixture-token",
            remaining = mapOf(901L to 18.0, 902L to 0.0),
            notes = "  "
        )

        assertTrue(result.isSuccess)
        assertEquals("Bearer fixture-token", api.closeAuthorization)
        val body = requireNotNull(api.closeBody)
        assertFalse(body.has("session_id"))
        assertFalse(body.has("branch_id"))
        assertFalse(body.has("notes"))
        assertEquals(18.0, body.getAsJsonObject("remaining").get("901").asDouble, 0.0)
        assertEquals(0.0, body.getAsJsonObject("remaining").get("902").asDouble, 0.0)
    }
}

private class FixtureDailyStockApiService(
    private val stockFixture: String
) : DailyStockApiService {
    var getAuthorization: String? = null
    var closeAuthorization: String? = null
    var closeBody: JsonObject? = null

    override suspend fun getDailyStock(authorization: String): Response<JsonObject> {
        getAuthorization = authorization
        return Response.success(ContractFixtureLoader.jsonObject(stockFixture))
    }

    override suspend fun closeSession(
        authorization: String,
        body: JsonObject
    ): Response<JsonObject> {
        closeAuthorization = authorization
        closeBody = body
        return Response.success(JsonObject().apply {
            addProperty("success", true)
            addProperty("message", "Sesi stok harian berhasil ditutup.")
        })
    }
}
