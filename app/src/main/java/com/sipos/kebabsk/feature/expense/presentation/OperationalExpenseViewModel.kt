package com.sipos.kebabsk.feature.expense.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.feature.expense.data.repository.OperationalExpenseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OperationalExpenseUiState(
    val amountInput: String = "",
    val categoryInput: String = "",
    val noteInput: String = "",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class OperationalExpenseViewModel(
    private val token: String,
    private val repository: OperationalExpenseRepositoryImpl
) : ViewModel() {
    private val _uiState = MutableStateFlow(OperationalExpenseUiState())
    val uiState: StateFlow<OperationalExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChanged(value: String) {
        _uiState.update {
            it.copy(
                amountInput = value.filter { ch -> ch.isDigit() },
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onCategoryChanged(value: String) {
        _uiState.update {
            it.copy(
                categoryInput = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update {
            it.copy(
                noteInput = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val amount = state.amountInput.toDoubleOrNull()
        val category = state.categoryInput.trim()

        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Nominal pengeluaran tidak valid.") }
            return
        }
        if (category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kategori pengeluaran wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
        viewModelScope.launch {
            repository.submitExpense(
                token = token,
                amount = amount,
                source = category,
                note = state.noteInput.trim().takeIf { it.isNotBlank() }
            ).onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        amountInput = "",
                        categoryInput = "",
                        noteInput = "",
                        successMessage = message,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = null,
                        errorMessage = error.message?.takeIf { msg -> msg.isNotBlank() }
                            ?: "Pengeluaran belum berhasil disimpan. Silakan coba lagi."
                    )
                }
            }
        }
    }
}
