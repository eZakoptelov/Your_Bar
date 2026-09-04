package com.example.yourbar.cart.domain.usecase

import com.example.yourbar.cart.data.CartRepository
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
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
        pipeMeters: Double
    ) {
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
            pipeMeters = pipeMeters
        )
        cartRepository.add(item)
    }
}
