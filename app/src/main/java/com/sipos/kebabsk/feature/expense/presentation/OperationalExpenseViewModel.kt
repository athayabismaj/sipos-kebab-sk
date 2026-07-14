package com.sipos.kebabsk.feature.expense.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.expense.domain.repository.OperationalExpenseRepository
import com.sipos.kebabsk.feature.expense.domain.validation.OperationalExpenseValidationInput
import com.sipos.kebabsk.feature.expense.domain.validation.OperationalExpenseValidationResult
import com.sipos.kebabsk.feature.expense.domain.validation.OperationalExpenseValidator
import kotlinx.coroutines.CancellationException
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
    private val repository: OperationalExpenseRepository,
    private val validator: OperationalExpenseValidator = OperationalExpenseValidator()
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
        if (state.isSaving) return

        val validation = validator.validate(
            OperationalExpenseValidationInput(
                amountInput = state.amountInput,
                categoryInput = state.categoryInput,
                noteInput = state.noteInput
            )
        )
        if (validation is OperationalExpenseValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message) }
            return
        }
        val validInput = (validation as OperationalExpenseValidationResult.Valid).value

        _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            submitExpense(
                token = token,
                amount = validInput.amount,
                category = validInput.category,
                note = validInput.note
            )
        }
    }

    private suspend fun submitExpense(
        token: String,
        amount: Long,
        category: String,
        note: String?
    ) {
        try {
            repository.submitExpense(
                token = token,
                amount = amount,
                source = category,
                note = note
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
                        errorMessage = sanitizeUserMessage(
                            error.message,
                            "Pengeluaran belum berhasil disimpan. Silakan coba lagi."
                        )
                    )
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } finally {
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
