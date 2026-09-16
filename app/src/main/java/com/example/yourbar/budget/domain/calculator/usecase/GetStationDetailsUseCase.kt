package com.example.yourbar.budget.domain.calculator.usecase

import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
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
        pocketCount: Int = 0,
        pocketHeightMm: Int = 260,
        isShelfAdded: Boolean = false,
        blenderShelfWidthMm: Int = 0,
        faucetHoleCount: Int = 0,
        backBoardCount: Int = 0,
        adjustableLegCount: Int = 0,
        solidSinkType: SolidSinkType = SolidSinkType.NONE
    ): List<StationPart> {
        val params = CalculationInputParams(
            widthMm = widthMm,
            depthMm = depthMm,
            steelType = steelType,
            thicknessMm = thicknessMm,
            pocketCount = pocketCount,
            pocketHeightMm = pocketHeightMm,
            isShelfAdded = isShelfAdded,
            blenderShelfWidthMm = blenderShelfWidthMm,
            faucetHoleCount = faucetHoleCount,
            backBoardCount = backBoardCount,
            adjustableLegCount = adjustableLegCount,
            solidSinkType = solidSinkType
        )
        val result = repository.calculate(params)
        return StationPartsBuilder.build(params, result)
    }
}
