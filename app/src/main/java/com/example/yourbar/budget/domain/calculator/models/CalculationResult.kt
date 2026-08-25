package com.example.yourbar.budget.domain.calculator.models

data class CalculationResult(
    val totalWeightKg: Double,        // Общая масса всей станции
    val countertopWeightKg: Double,  // Масса столешницы
    val pocketWeightKg: Double,      // Масса кармана для бутылок
    val additionalPocketsTotalWeightKg: Double, // Масса всех дополнительных навесных карманов
    val sinkWeightKg: Double,        // Масса мойки
    val insertWeightKg: Double,      // Масса вставки
    val partitionsWeightKg: Double,  // Масса перегородок
    val weightAisi304Kg: Double,     // Масса деталей из AISI 304
    val weightAisi430Kg: Double      // Масса деталей из AISI 430
)
