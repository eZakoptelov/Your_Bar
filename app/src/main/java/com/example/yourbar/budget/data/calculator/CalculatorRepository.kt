package com.example.yourbar.budget.data.calculator

import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.usecase.CalculateBudgetUseCase

class CalculatorRepository(
    private val useCase: CalculateBudgetUseCase = CalculateBudgetUseCase()
) {
    fun calculate(params: CalculationInputParams): CalculationResult = useCase.execute(params)
}
