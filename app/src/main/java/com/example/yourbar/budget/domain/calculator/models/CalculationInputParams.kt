package com.example.yourbar.budget.domain.calculator.models

data class CalculationInputParams(
    val widthMm: Int,        // Ширина станции в миллиметрах
    val depthMm: Int,       // Глубина станции в миллиметрах
    val steelType: SteelType,// Тип стали (например, AISI 304 или AISI 430)
    val thicknessMm: Double, // Толщина листа металла в миллиметрах
    val pocketCount: Int = 0,            // было: additionalPocketsCount = 1
    val pocketHeightMm: Int = 260,       // новое: 260 или 210
    val isShelfAdded: Boolean = false,
    val blenderShelfWidthMm: Int = 0, //Полка для блендера
    val faucetHoleCount: Int = 0, //Отверстия для смесителя
    val backBoardCount: Int = 0, // Борт
    val adjustableLegCount: Int = 0, //Опора регулируемая
    val solidSinkType: SolidSinkType = SolidSinkType.NONE // Цельнотянутая мойка
)
