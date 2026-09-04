package com.example.yourbar.budget.domain.calculator.usecase

import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SteelType

class CalculateParamsUseCase(
    private val repository: CalculatorRepository
) {
    fun execute(
        widthMm: Int,
        depthMm: Int,
        steelType: SteelType,
        thicknessMm: Double,
        additionalPocketsCount: Int
    ): CalculationResult {
        val params = CalculationInputParams(
            widthMm = widthMm,
            depthMm = depthMm,
            steelType = steelType,
            thicknessMm = thicknessMm,
            additionalPocketsCount = additionalPocketsCount
        )
        return repository.calculate(params)
    }
}
