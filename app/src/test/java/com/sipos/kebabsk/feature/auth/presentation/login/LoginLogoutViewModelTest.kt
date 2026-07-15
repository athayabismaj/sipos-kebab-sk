package com.sipos.kebabsk.feature.auth.presentation.login

import com.sipos.kebabsk.common.SessionStore
import com.sipos.kebabsk.feature.auth.domain.model.AuthBranch
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginLogoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun logoutSuccessRevokesBackendThenClearsLocalSession() = runTest {
        val session = cashierSession()
        val store = FakeSessionStore(session)
        val repository = FakeAuthRepository(session)
        val viewModel = LoginViewModel(repository, sessionStore = store)
        testScheduler.advanceUntilIdle()

        viewModel.logout()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(session.token), repository.logoutTokens)
        assertEquals(1, store.clearCalls)
        assertNull(store.session)
        assertNull(viewModel.uiState.value.session)
        assertFalse(viewModel.uiState.value.isLoggingOut)
    }

    @Test
    fun logoutUnauthorizedOrNetworkFailureStillClearsLocalSession() = runTest {
        listOf(
            IllegalStateException("401"),
            java.net.SocketTimeoutException("timeout")
        ).forEach { failure ->
            val session = cashierSession()
            val store = FakeSessionStore(session)
            val repository = FakeAuthRepository(session, logoutResult = Result.failure(failure))
            val viewModel = LoginViewModel(repository, sessionStore = store)
            testScheduler.advanceUntilIdle()

            viewModel.logout()
            testScheduler.advanceUntilIdle()

            assertEquals(1, repository.logoutTokens.size)
            assertNull(store.session)
            assertNull(viewModel.uiState.value.session)
            assertFalse(viewModel.uiState.value.isLoggingOut)
        }
    }

    @Test
    fun doubleLogoutOnlyCallsBackendOnce() = runTest {
        val session = cashierSession()
        val store = FakeSessionStore(session)
        val repository = FakeAuthRepository(session, logoutDelayMs = 100)
        val viewModel = LoginViewModel(repository, sessionStore = store)
        testScheduler.advanceUntilIdle()

        viewModel.logout()
        viewModel.logout()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.logoutTokens.size)
        assertNull(store.session)
    }

    private fun cashierSession() = AuthSession(
        token = "fixture-token",
        displayName = "Kasir Fixture",
        username = "kasir_fixture",
        email = "kasir.fixture@example.test",
        role = "kasir",
        branch = AuthBranch(7L, "Cabang Fixture", "FIX")
    )
}

private class FakeSessionStore(initial: AuthSession?) : SessionStore {
    var session: AuthSession? = initial
    var clearCalls = 0

    override fun saveSession(session: AuthSession) {
        this.session = session
    }

    override fun loadSession(): AuthSession? = session

    override fun clearSession() {
        clearCalls += 1
        session = null
    }
}

private class FakeAuthRepository(
    private val profile: AuthSession,
    private val logoutResult: Result<Unit> = Result.success(Unit),
    private val logoutDelayMs: Long = 0
) : AuthRepository {
    val logoutTokens = mutableListOf<String>()

    override suspend fun logout(token: String): Result<Unit> {
        logoutTokens += token
        if (logoutDelayMs > 0) delay(logoutDelayMs)
        return logoutResult
    }

    override suspend fun me(token: String) = Result.success(profile)
    override suspend fun login(identifier: String, password: String) = Result.success(profile)
    override suspend fun updateProfile(token: String, name: String, username: String, email: String) = Result.success(profile)
    override suspend fun changePassword(token: String, currentPassword: String, newPassword: String) = Result.success("ok")
    override suspend fun forgotPassword(email: String) = Result.success("ok")
    override suspend fun verifyResetCode(email: String, code: String) = Result.success("ok")
    override suspend fun resetPassword(email: String, code: String, newPassword: String) = Result.success("ok")
    override suspend fun validateSessionOnServer(token: String) = Result.success(true)
}
