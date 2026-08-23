package com.example.yourbar.budget.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yourbar.budget.domain.calculator.usecase.CalculatePipeMetersUseCase
import com.example.yourbar.budget.domain.calculator.usecase.PipeInput

class BudgetCalculatorViewModel(
    private val calculatePipeMetersUseCase: CalculatePipeMetersUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<BudgetUiState>(BudgetUiState.Idle)
    val uiState: LiveData<BudgetUiState> = _uiState

    fun calculate(widthMm: Int, depthMm: Int, heightMm: Int) {
        try {
            val pipeMeters = calculatePipeMetersUseCase.execute(
                PipeInput(widthMm = widthMm, depthMm = depthMm, heightMm = heightMm)
            )
            _uiState.value = BudgetUiState.Result(pipeMeters = pipeMeters)
        } catch (e: IllegalArgumentException) {
            _uiState.value = BudgetUiState.Error(e.message ?: "Ошибка расчёта")
        }
    }
}

sealed class BudgetUiState {
    object Idle : BudgetUiState()
    data class Result(val pipeMeters: Double) : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
}
