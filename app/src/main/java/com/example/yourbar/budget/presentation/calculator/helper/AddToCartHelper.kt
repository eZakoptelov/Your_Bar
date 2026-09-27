package com.example.yourbar.budget.presentation.calculator.helper

import android.content.SharedPreferences
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.example.yourbar.budget.domain.calculator.models.SteelType
import com.example.yourbar.budget.presentation.calculator.model.StationConfig
import com.example.yourbar.cart.domain.usecase.AddToCartUseCase

class AddToCartHelper(
    private val addToCartUseCase: AddToCartUseCase,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_ADJUSTABLE_LEG = "price_adjustable_leg"
    }

    suspend fun execute(
        name: String,
        widthMm: Int,
        depthMm: Int,
        heightMm: Int,
        steelType: SteelType,
        thicknessMm: Double,
        config: StationConfig,
        calculationResult: CalculationResult,
        pipeMeters: Double
    ) {
        val adjustableLegPricePerUnit =
            sharedPreferences.getFloat(KEY_ADJUSTABLE_LEG, 0f).toDouble()

        val sink400x400Price = sharedPreferences.getFloat("price_sink_400x400", 0f).toDouble()
        val sink400x500Price = sharedPreferences.getFloat("price_sink_400x500", 0f).toDouble()
        val sink500x500Price = sharedPreferences.getFloat("price_sink_500x500", 0f).toDouble()
        val sink500x400Price = sharedPreferences.getFloat("price_sink_500x400", 0f).toDouble()

        addToCartUseCase.execute(
            name = name,
            widthMm = widthMm,
            depthMm = depthMm,
            heightMm = heightMm,
            steelType = steelType.name,
            thicknessMm = thicknessMm,
            pocketsCount = config.pocketCount,
            calculationResult = calculationResult,
            pipeMeters = pipeMeters,
            isBlenderShelfAdded = config.isShelfAdded,
            blenderShelfWidthMm = config.blenderShelfWidthMm,
            faucetHolePricePerUnit = 0.0,
            backBoardPricePerUnit = 0.0,
            adjustableLegPricePerUnit = adjustableLegPricePerUnit,
            solidSinkType = config.solidSinkType,
            sink400x400Price = sink400x400Price,
            sink400x500Price = sink400x500Price,
            sink500x500Price = sink500x500Price,
            sink500x400Price = sink500x400Price
        )
    }
}
