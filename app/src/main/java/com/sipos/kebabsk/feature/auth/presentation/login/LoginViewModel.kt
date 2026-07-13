package com.sipos.kebabsk.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.auth.data.repository.AuthRepositoryImpl
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import com.sipos.kebabsk.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val session: AuthSession? = null,
    /**
     * Status sinkronisasi sesi dengan server.
     * - CHECKING: Sedang memvalidasi sesi ke server (saat splash).
     * - SYNCED: Server mengkonfirmasi sesi aktif.
     * - PENDING_SYNC: Tidak bisa menghubungi server (offline), izinkan masuk sementara.
     * - DESYNCED: Token login/akun ditolak server → force clear dilakukan otomatis.
     * - IDLE: Tidak ada sesi lokal untuk divalidasi.
     */
    val sessionSyncState: SessionSyncState = SessionSyncState.IDLE
)

enum class SessionSyncState {
    IDLE,
    CHECKING,
    SYNCED,
    PENDING_SYNC,
    DESYNCED
}

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(NetworkModule.authApiService),
    private val loginUseCase: LoginUseCase = LoginUseCase(authRepository)
) : ViewModel() {
    private companion object {
        const val CASHIER_ROLE = "kasir"
        const val CASHIER_ONLY_MESSAGE =
            "Aplikasi mobile hanya dapat diakses oleh akun kasir."
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val saved = AppSessionStore.loadSession()
        if (saved != null) {
            if (saved.hasCashierRole()) {
                // Muat sesi kasir lokal, lalu validasi kembali terhadap server.
                _uiState.update { it.copy(session = saved, sessionSyncState = SessionSyncState.CHECKING) }
                validateLocalSession(saved)
            } else {
                rejectNonCashierSession()
            }
        }
    }

    /**
     * Self-Healing Logic:
     * Saat startup, verifikasi apakah sesi lokal masih diakui oleh server.
     * - `/auth/me` sukses → token login masih valid, lanjut normal (SYNCED).
     * - `/auth/me` gagal karena token invalid → interceptor global akan clear session.
     * - Network error → izinkan offline mode (PENDING_SYNC), beri indikator.
     *
     * Catatan: status sesi harian/shift tidak boleh dipakai untuk menentukan validitas login.
     * Kasir boleh tetap masuk dan melihat dashboard "Sesi Harian Ditutup".
     */
    private fun validateLocalSession(session: AuthSession) {
        viewModelScope.launch(Dispatchers.IO) {
            val profileResult = authRepository.me(session.token)
            profileResult.onSuccess { freshSession ->
                if (!freshSession.hasCashierRole()) {
                    rejectNonCashierSession()
                    return@launch
                }

                AppSessionStore.saveSession(freshSession)
                com.sipos.kebabsk.common.AuthSessionEvents.resetLogoutState()
                _uiState.update {
                    it.copy(
                        session = freshSession,
                        sessionSyncState = SessionSyncState.SYNCED
                    )
                }
            }.onFailure {
                if (AppSessionStore.loadSession() == null) {
                    _uiState.update {
                        it.copy(
                            session = null,
                            sessionSyncState = SessionSyncState.DESYNCED,
                            password = "",
                            errorMessage = null,
                            successMessage = null
                        )
                    }
                    return@launch
                }

                // Mode offline hanya boleh memakai sesi lokal yang sudah terverifikasi sebagai kasir.
                _uiState.update { it.copy(sessionSyncState = SessionSyncState.PENDING_SYNC) }
                return@launch
            }
        }
    }

    fun onIdentifierChanged(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.identifier.isBlank() || current.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email/username dan password wajib diisi")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = loginUseCase(current.identifier.trim(), current.password)

            result
                .onSuccess { session ->
                    val finalSession = authRepository.me(session.token)
                        .getOrElse { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    session = null,
                                    sessionSyncState = SessionSyncState.IDLE,
                                    password = "",
                                    errorMessage = sanitizeUserMessage(
                                        error.message,
                                        "Hak akses akun belum dapat diverifikasi. Silakan coba lagi."
                                    )
                                )
                            }
                            return@launch
                        }

                    if (!finalSession.hasCashierRole()) {
                        rejectNonCashierSession()
                        return@launch
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = finalSession,
                            sessionSyncState = SessionSyncState.SYNCED,
                            errorMessage = null,
                            successMessage = null,
                            password = ""
                        )
                    }
                    AppSessionStore.saveSession(finalSession)
                    com.sipos.kebabsk.common.AuthSessionEvents.resetLogoutState()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(error.message, "Login belum berhasil. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun refreshSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            authRepository.me(session.token)
                .onSuccess { fresh ->
                    if (!fresh.hasCashierRole()) {
                        rejectNonCashierSession()
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(session = fresh, errorMessage = null)
                    }
                    AppSessionStore.saveSession(fresh)
                }
        }
    }

    fun updateProfile(name: String, username: String, email: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(errorMessage = "Sesi login tidak ditemukan.") }
            return
        }

        if (name.isBlank() || username.isBlank() || email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama, username, dan email wajib diisi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            authRepository.updateProfile(session.token, name, username, email)
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = updated,
                            successMessage = "Profil berhasil diperbarui.",
                            errorMessage = null
                        )
                    }
                    AppSessionStore.saveSession(updated)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = null,
                            errorMessage = sanitizeUserMessage(error.message, "Profil belum berhasil diperbarui. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(errorMessage = "Sesi login tidak ditemukan.") }
            return
        }

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Semua field password wajib diisi.") }
            return
        }

        if (newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password baru minimal 6 karakter.") }
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi password tidak sama.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            authRepository.changePassword(session.token, currentPassword, newPassword)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = message,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = null,
                            errorMessage = sanitizeUserMessage(error.message, "Password belum berhasil diubah. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun clearProfileMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun logout() {
        AppSessionStore.clearSession()
        _uiState.update {
            it.copy(
                session = null,
                sessionSyncState = SessionSyncState.IDLE,
                password = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun AuthSession.hasCashierRole(): Boolean {
        return role?.trim()?.equals(CASHIER_ROLE, ignoreCase = true) == true
    }

    private fun rejectNonCashierSession() {
        AppSessionStore.clearSession()
        _uiState.update {
            it.copy(
                isLoading = false,
                session = null,
                sessionSyncState = SessionSyncState.DESYNCED,
                password = "",
                successMessage = null,
                errorMessage = CASHIER_ONLY_MESSAGE
            )
        }
    }
}
