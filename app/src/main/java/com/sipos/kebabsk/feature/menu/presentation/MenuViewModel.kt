package com.sipos.kebabsk.feature.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.usecase.GetMenusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MenuUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cashierName: String = "",
    val cashierRole: String? = null,
    val isDailySessionOpen: Boolean = false,
    val isDailySessionStatusKnown: Boolean = false,
    val dailySessionStatusLabel: String? = null,
    val dailyTargetRevenue: Long? = null,
    val dailyStockItems: List<DailyStockItem> = emptyList(),
    val menus: List<MenuItem> = emptyList(),
    val selectedCategory: String? = null
)

class MenuViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val getMenusUseCase = GetMenusUseCase(menuRepository)

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var loadedToken: String? = null
    private val menuLoadMutex = Mutex()

    fun loadMenus(token: String, forceRefresh: Boolean = false, onLoaded: () -> Unit = {}) {
        if (_uiState.value.isLoading) return
        if (!forceRefresh && loadedToken == token && _uiState.value.menus.isNotEmpty()) {
            onLoaded()
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            menuLoadMutex.withLock {
                try {
                    if (!forceRefresh && loadedToken == token && _uiState.value.menus.isNotEmpty()) {
                        onLoaded()
                        return@withLock
                    }

                    val menuResult = getMenusUseCase(token)

                    menuResult
                        .onSuccess { payload ->
                            loadedToken = token
                            _uiState.update {
                                it.copy(
                                    menus = payload.menus,
                                    cashierName = payload.user.name,
                                    cashierRole = payload.user.role,
                                    isDailySessionOpen = payload.dailySession.isOpen,
                                    isDailySessionStatusKnown = payload.dailySession.isKnown,
                                    dailySessionStatusLabel = payload.dailySession.label,
                                    dailyTargetRevenue = payload.dailySession.targetRevenue,
                                    dailyStockItems = payload.dailyStockItems,
                                    errorMessage = null
                                )
                            }
                            onLoaded()
                        }
                        .onFailure { error ->
                            loadedToken = null
                            _uiState.update {
                                it.copy(
                                    // Do not continue to trust a stale session after a failed refresh.
                                    isDailySessionOpen = false,
                                    isDailySessionStatusKnown = false,
                                    dailySessionStatusLabel = "Status sesi harian belum dapat diverifikasi",
                                    errorMessage = sanitizeUserMessage(
                                        error.message,
                                        "Menu belum bisa dimuat. Silakan coba lagi."
                                    )
                                )
                            }
                        }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
    
    fun prefetchMenusIfNeeded(token: String) {
        if (_uiState.value.menus.isNotEmpty() || _uiState.value.isLoading) return
        loadMenus(token, forceRefresh = false)
    }

    fun forceRefreshMenus(token: String) {
        loadMenus(token, forceRefresh = true)
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun clear() {
        loadedToken = null
        _uiState.value = MenuUiState()
    }
}
