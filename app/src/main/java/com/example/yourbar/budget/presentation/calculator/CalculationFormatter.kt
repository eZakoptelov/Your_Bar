package com.example.yourbar.budget.presentation.calculator

import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import java.text.DecimalFormat

object CalculationFormatter {

    private val df = DecimalFormat("0.##")

    data class FormattedResult(
        val totalWeight: String,
        val aisi304: String,
        val aisi430: String,
        val countertop: String,
        val pocket: String,
        val sink: String,
        val insert: String,
        val partitions: String,
        val faucetHoles: String,
        val backBoard: String
    )

    fun format(result: CalculationResult, totalPocketsCount: Int): FormattedResult {
        val pocketWeight = result.pocketWeightKg + result.additionalPocketsTotalWeightKg
        val pocketLabel = when (totalPocketsCount) {
            1 -> "Карман для бутылок (1 шт):"
            2 -> "Карман для бутылок (2 шт):"
            else -> "Карман для бутылок ($totalPocketsCount шт):"
        }

        val faucetHolesText = if (result.faucetHoleCount > 0) {
            "Отверстия для смесителя: ${result.faucetHoleCount} шт"
        } else {
            "Отверстия для смесителя: нет"
        }

        val backBoardText = if (result.backBoardCount > 0) {
            "Задний борт: ${result.backBoardCount} шт"
        } else {
            "Задний борт: нет"
        }

        return FormattedResult(
            totalWeight = "Общий вес: ${df.format(result.totalWeightKg)} кг",
            aisi304 = "Aisi 304: ${df.format(result.weightAisi304Kg)} кг",
            aisi430 = "Aisi 430: ${df.format(result.weightAisi430Kg)} кг",
            countertop = "Столешница: ${df.format(result.countertopWeightKg)} кг",
            pocket = "$pocketLabel ${df.format(pocketWeight)} кг",
            sink = "Ванна для льда: ${df.format(result.sinkWeightKg)} кг",
            insert = "Перфорированная вставка: ${df.format(result.insertWeightKg)} кг",
            partitions = "Съёмные перегородки: ${df.format(result.partitionsWeightKg)} кг",
            faucetHoles = faucetHolesText,
            backBoard = backBoardText
        )
    }
}
