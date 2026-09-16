package com.example.yourbar.cart.domain.usecase

import com.example.yourbar.cart.data.CartRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.example.yourbar.cart.domain.CartItem

class AddToCartUseCase(
    private val cartRepository: CartRepository
) {
    fun execute(
        name: String,
        widthMm: Int,
        depthMm: Int,
        heightMm: Int,
        steelType: String,
        thicknessMm: Double,
        pocketsCount: Int,
        calculationResult: CalculationResult,
        pipeMeters: Double,
        isBlenderShelfAdded: Boolean = false,
        blenderShelfWidthMm: Int = 500,
        faucetHolePricePerUnit: Double,
        backBoardPricePerUnit: Double = 0.0,
        adjustableLegPricePerUnit: Double = 0.0,
        solidSinkType: SolidSinkType = SolidSinkType.NONE,
        sink400x400Price: Double = 0.0,
        sink400x500Price: Double = 0.0,
        sink500x500Price: Double = 0.0,
        sink500x400Price: Double = 0.0
    ) {
        val solidSinkPrice = when (solidSinkType) {
            SolidSinkType.SINK_400x400 -> sink400x400Price
            SolidSinkType.SINK_400x500 -> sink400x500Price
            SolidSinkType.SINK_500x500 -> sink500x500Price
            SolidSinkType.SINK_500x400 -> sink500x400Price
            SolidSinkType.NONE -> 0.0
        }

        val item = CartItem(
            name = name,
            widthMm = widthMm,
            depthMm = depthMm,
            heightMm = heightMm,
            steelType = steelType,
            thicknessMm = thicknessMm,
            pocketsCount = pocketsCount,
            totalWeightKg = calculationResult.totalWeightKg,
            weightAisi304Kg = calculationResult.weightAisi304Kg,
            weightAisi430Kg = calculationResult.weightAisi430Kg,
            pipeMeters = pipeMeters,
            isBlenderShelfAdded = isBlenderShelfAdded,
            blenderShelfWidthMm = blenderShelfWidthMm,
            insulationAreaSqM = calculationResult.insulationAreaSqM,
            faucetHoleCount = calculationResult.faucetHoleCount,
            faucetHolePricePerUnit = faucetHolePricePerUnit,
            backBoardCount = calculationResult.backBoardCount,
            backBoardPricePerUnit = backBoardPricePerUnit,
            adjustableLegCount = calculationResult.adjustableLegCount,
            adjustableLegPricePerUnit = adjustableLegPricePerUnit,
            pocketHeightMm = calculationResult.pocketHeightMm,
            solidSinkType = solidSinkType.name,
            solidSinkPrice = solidSinkPrice
        )
        cartRepository.add(item)
    }
}
