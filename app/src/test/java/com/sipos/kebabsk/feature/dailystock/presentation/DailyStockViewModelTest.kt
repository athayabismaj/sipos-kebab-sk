package com.sipos.kebabsk.feature.dailystock.presentation

import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.repository.FakeDailyStockRepository
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DailyStockViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeDailyStockRepository()
    
    private val fakeAuthApiService = object : AuthApiService {
        override suspend fun login(request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun forgotPasswordAuth(request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun verifyResetCodeAuth(request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun resetPasswordAuth(request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun me(authorization: String): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun updateProfile(authorization: String, request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun changePassword(authorization: String, request: Map<String, String>): Response<com.google.gson.JsonObject> = Response.success(com.google.gson.JsonObject())
        override suspend fun sessionCurrentStatus(authorization: String): Response<com.google.gson.JsonObject> {
            val response = com.google.gson.JsonObject().apply {
                addProperty("message", "success")
                add("data", com.google.gson.JsonObject().apply {
                    addProperty("has_active_session", true)
                })
            }
            return Response.success(response)
        }
    }

    @Test
    fun fetchStockDataSuccessReturnsItems() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        
        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(items, state.items)
        assertEquals(100L, state.sessionId)
        assertNull(state.errorMessage)
    }

    @Test
    fun fetchStockDataWithNullSessionIdSetsStateToNotStarted() = runTest {
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(null, emptyList()))
        
        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.sessionId)
    }

    @Test
    fun fetchStockDataFailureSetsErrorMessage() = runTest {
        fakeRepository.shouldFail = true
        
        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Test failure", state.errorMessage)
    }

    @Test
    fun closeSessionSuccessClearsSessionData() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeRepository.closeSessionResult = Result.success("Sesi berhasil ditutup")
        
        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()
        
        val emptyRemaining: Map<Long, Double> = emptyMap()
        viewModel.closeSession(emptyRemaining, "notes")
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isClosing)
        assertEquals("Sesi berhasil ditutup", state.closeSuccessMessage)
    }

    @Test
    fun closeSessionFailureSetsErrorMessage() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeRepository.shouldFail = true
        
        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()
        
        val emptyRemaining: Map<Long, Double> = emptyMap()
        viewModel.closeSession(emptyRemaining, "notes")
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isClosing)
        assertEquals("Test failure", state.closeErrorMessage)
    }

    @Test
    fun closeSessionDoubleTapOnlyCallsRepositoryOnce() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeRepository.delayMs = 200

        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()

        viewModel.closeSession(emptyMap(), "notes")
        viewModel.closeSession(emptyMap(), "notes 2")
        testScheduler.runCurrent()

        assertEquals(1, fakeRepository.closeSessionCalls)

        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isClosing)
    }

    @Test
    fun closeSessionCancellationResetsClosingWithoutError() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = DailyStockViewModel(fakeRepository, fakeAuthApiService)
        testScheduler.advanceUntilIdle()

        fakeRepository.cancellation = CancellationException("cancelled")
        viewModel.closeSession(emptyMap(), "notes")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, fakeRepository.closeSessionCalls)
        assertFalse(state.isClosing)
        assertFalse(state.closeSuccess)
        assertNull(state.closeErrorMessage)
    }
}

