package com.example.yourbar.budget.domain.calculator.models

data class CalculationInputParams(
    val widthMm: Int,
    val depthMm: Int,
    val steelType: SteelType,
    val thicknessMm: Double
)
