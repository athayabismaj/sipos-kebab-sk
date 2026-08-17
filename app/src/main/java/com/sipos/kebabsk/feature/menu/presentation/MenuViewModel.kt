package com.sipos.kebabsk.feature.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuCategory
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.usecase.GetMenusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
    val cashierName: String = "",
    val cashierRole: String? = null,
    val isDailySessionOpen: Boolean = false,
    val isDailySessionStatusKnown: Boolean = false,
    val dailySessionStatusLabel: String? = null,
    val dailyStockItems: List<DailyStockItem> = emptyList(),
    val menus: List<MenuItem> = emptyList(),
    val categories: List<MenuCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val currentPage: Int = 0,
    val hasMore: Boolean = true
) {
    val isLoading: Boolean
        get() = isInitialLoading || isRefreshing
}

class MenuViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val getMenusUseCase = GetMenusUseCase(menuRepository)

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var loadedToken: String? = null
    private var activeToken: String? = null
    private var requestGeneration = 0L
    private var firstPageJob: Job? = null
    private var loadMoreJob: Job? = null
    private var searchJob: Job? = null

    fun loadMenus(token: String, forceRefresh: Boolean = false, onLoaded: () -> Unit = {}) {
        if (token.isBlank()) return
        activeToken = token

        if (forceRefresh) {
            refreshMenus(token, onLoaded)
            return
        }

        val state = _uiState.value
        if (loadedToken == token && state.menus.isNotEmpty()) {
            onLoaded()
            return
        }
        if (state.isInitialLoading || state.isRefreshing) return

        requestFirstPage(
            token = token,
            clearItems = loadedToken != token,
            onLoaded = onLoaded
        )
    }

    fun prefetchMenusIfNeeded(token: String) {
        val state = _uiState.value
        if (state.menus.isNotEmpty() || state.isInitialLoading || state.isRefreshing) return
        loadMenus(token, forceRefresh = false)
    }

    fun forceRefreshMenus(token: String) {
        refreshMenus(token)
    }

    fun loadNextPage() {
        val token = activeToken ?: return
        val state = _uiState.value
        if (
            state.isInitialLoading ||
            state.isRefreshing ||
            state.isLoadingMore ||
            !state.hasMore ||
            state.currentPage < 1
        ) {
            return
        }

        val targetPage = state.currentPage + 1
        val generation = requestGeneration
        val categoryId = state.selectedCategoryId
        val search = state.searchQuery.trim().takeIf { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                isLoadingMore = true,
                loadMoreErrorMessage = null
            )
        }

        loadMoreJob = viewModelScope.launch {
            try {
                getMenusUseCase(
                    token = token,
                    search = search,
                    categoryId = categoryId,
                    page = targetPage,
                    perPage = PAGE_SIZE
                ).onSuccess { payload ->
                    if (generation != requestGeneration) return@onSuccess

                    loadedToken = token
                    _uiState.update { current ->
                        current.copy(
                            menus = mergeMenuPages(current.menus, payload.menus),
                            categories = payload.categories.ifEmpty { current.categories },
                            cashierName = payload.user.name,
                            cashierRole = payload.user.role,
                            isDailySessionOpen = payload.dailySession.isOpen,
                            isDailySessionStatusKnown = payload.dailySession.isKnown,
                            dailySessionStatusLabel = payload.dailySession.label,
                            dailyStockItems = payload.dailyStockItems,
                            currentPage = payload.pagination.currentPage,
                            hasMore = payload.pagination.hasMore,
                            errorMessage = null,
                            loadMoreErrorMessage = null
                        )
                    }
                }.onFailure { error ->
                    if (generation != requestGeneration) return@onFailure
                    _uiState.update {
                        it.copy(
                            loadMoreErrorMessage = sanitizeUserMessage(
                                error.message,
                                "Menu berikutnya belum bisa dimuat. Silakan coba lagi."
                            )
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                if (generation == requestGeneration) {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun retryLoadMore() {
        if (!_uiState.value.loadMoreErrorMessage.isNullOrBlank()) {
            loadNextPage()
        }
    }

    fun onCategorySelected(categoryId: Long?) {
        if (_uiState.value.selectedCategoryId == categoryId) return

        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                errorMessage = null,
                loadMoreErrorMessage = null
            )
        }
        activeToken?.let { token ->
            requestFirstPage(token = token, clearItems = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        if (_uiState.value.searchQuery == query) return

        requestGeneration += 1
        firstPageJob?.cancel()
        loadMoreJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = query,
                isInitialLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                errorMessage = null,
                loadMoreErrorMessage = null
            )
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            activeToken?.let { token ->
                requestFirstPage(token = token, clearItems = true)
            }
        }
    }

    fun clear() {
        requestGeneration += 1
        firstPageJob?.cancel()
        loadMoreJob?.cancel()
        searchJob?.cancel()
        loadedToken = null
        activeToken = null
        _uiState.value = MenuUiState()
    }

    private fun refreshMenus(token: String, onLoaded: () -> Unit = {}) {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing) return
        activeToken = token
        requestFirstPage(token = token, clearItems = false, onLoaded = onLoaded)
    }

    private fun requestFirstPage(
        token: String,
        clearItems: Boolean,
        onLoaded: () -> Unit = {}
    ) {
        requestGeneration += 1
        val generation = requestGeneration
        firstPageJob?.cancel()
        loadMoreJob?.cancel()

        val state = _uiState.value
        val showInitialLoading = clearItems || state.menus.isEmpty()
        val categoryId = state.selectedCategoryId
        val search = state.searchQuery.trim().takeIf { it.isNotEmpty() }

        _uiState.update {
            it.copy(
                isInitialLoading = showInitialLoading,
                isRefreshing = !showInitialLoading,
                isLoadingMore = false,
                menus = if (clearItems) emptyList() else it.menus,
                currentPage = if (clearItems) 0 else it.currentPage,
                hasMore = true,
                errorMessage = null,
                loadMoreErrorMessage = null
            )
        }

        firstPageJob = viewModelScope.launch {
            try {
                if (clearItems) {
                    menuRepository.getCachedMenus(
                        token = token,
                        search = search,
                        categoryId = categoryId
                    )?.let { cachedPayload ->
                        if (generation == requestGeneration) {
                            applyCachedCatalog(cachedPayload)
                        }
                    }
                }

                getMenusUseCase(
                    token = token,
                    search = search,
                    categoryId = categoryId,
                    page = 1,
                    perPage = PAGE_SIZE
                ).onSuccess { payload ->
                    if (generation != requestGeneration) return@onSuccess

                    loadedToken = token
                    applyFirstPage(payload)
                    onLoaded()
                }.onFailure { error ->
                    if (generation != requestGeneration) return@onFailure

                    loadedToken = null
                    _uiState.update {
                        it.copy(
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
                if (generation == requestGeneration) {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    private fun applyFirstPage(payload: MenuListPayload) {
        _uiState.update { current ->
            current.copy(
                menus = payload.menus,
                categories = payload.categories.ifEmpty { current.categories },
                cashierName = payload.user.name,
                cashierRole = payload.user.role,
                isDailySessionOpen = payload.dailySession.isOpen,
                isDailySessionStatusKnown = payload.dailySession.isKnown,
                dailySessionStatusLabel = payload.dailySession.label,
                dailyStockItems = payload.dailyStockItems,
                currentPage = payload.pagination.currentPage,
                hasMore = payload.pagination.hasMore,
                errorMessage = null,
                loadMoreErrorMessage = null
            )
        }
    }

    private fun applyCachedCatalog(payload: MenuListPayload) {
        _uiState.update { current ->
            current.copy(
                menus = payload.menus,
                categories = payload.categories.ifEmpty { current.categories },
                isInitialLoading = false,
                isRefreshing = true,
                currentPage = 0,
                hasMore = true
            )
        }
    }

    private fun mergeMenuPages(
        currentMenus: List<MenuItem>,
        nextMenus: List<MenuItem>
    ): List<MenuItem> {
        val merged = LinkedHashMap<Long, MenuItem>()
        currentMenus.forEach { menu -> merged[menu.id] = menu }

        nextMenus.forEach { nextMenu ->
            val current = merged[nextMenu.id]
            merged[nextMenu.id] = if (current == null) {
                nextMenu
            } else {
                current.copy(
                    variants = (current.variants + nextMenu.variants)
                        .distinctBy { it.id }
                )
            }
        }

        return merged.values.toList()
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
