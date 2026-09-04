package com.example.yourbar.budget.domain.calculator.models

data class StationPart(
    val title: String,
    val dimensions: String,
    val steelType: String,
    val thicknessMm: Double,
    val weightKg: Double,
    val quantity: Int = 1
) {
    val totalWeightKg: Double get() = weightKg * quantity
}
