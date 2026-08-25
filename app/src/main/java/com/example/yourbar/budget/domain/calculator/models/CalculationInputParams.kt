package com.example.yourbar.budget.domain.calculator.models

data class CalculationInputParams(
    val widthMm: Int,        // Ширина станции в миллиметрах
    val depthMm: Int,       // Глубина станции в миллиметрах
    val steelType: SteelType,// Тип стали (например, AISI 304 или AISI 430)
    val thicknessMm: Double, // Толщина листа металла в миллиметрах
    val additionalPocketsCount: Int = 1 // Количество дополнительных навесных карманов
)
