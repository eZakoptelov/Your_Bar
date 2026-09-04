package com.example.yourbar.budget.domain.calculator.usecase

import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.StationPart
import com.example.yourbar.budget.domain.calculator.models.StationPartsBuilder
import com.example.yourbar.budget.domain.calculator.models.SteelType

class GetStationDetailsUseCase(
    private val repository: CalculatorRepository
) {
    fun execute(
        widthMm: Int,
        depthMm: Int,
        steelType: SteelType,
        thicknessMm: Double,
        additionalPocketsCount: Int
    ): List<StationPart> {
        val params = CalculationInputParams(
            widthMm = widthMm,
            depthMm = depthMm,
            steelType = steelType,
            thicknessMm = thicknessMm,
            additionalPocketsCount = additionalPocketsCount
        )
        val result = repository.calculate(params)
        return StationPartsBuilder.build(params, result)
    }
}
