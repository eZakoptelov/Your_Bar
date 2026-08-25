package com.example.yourbar.budget.domain.calculator.usecase

import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SteelType

class CalculateBudgetUseCase {

    companion object {
        private const val DENSITY_AISI_304 = 7900.0
        private const val DENSITY_AISI_430 = 7700.0

        // Размеры кармана
        private const val POCKET_FRONT_H = 120
        private const val POCKET_BACK_H = 260
        private const val POCKET_DEPTH = 110
        private const val POCKET_THICK = 1.5

        // Константы мойки, вставки, перегородок
        private const val AUTO_ADD_MM = 90
        private const val SINK_FIXED_H = 265
        private const val SINK_REDUCE_D = 70
        private const val SINK_REDUCE_W = 70
        private const val WALL_EXTRA_H = 30
        private const val WALL_EXTRA_D = 30
        private const val INSERT_EXTRA = 35
        private const val PART_EXTRA_H = 20
        private const val PART_REDUCE_W = 220

        // Вырез: из габаритов столешницы (уже с +90 мм) вычитаем по 190 мм
        private const val CUTOUT_REDUCTION_MM = 190
    }

    fun execute(params: CalculationInputParams): CalculationResult {
        // 1. Габариты столешницы (пользователь + 90 мм)
        val countertopWidthMm = params.widthMm + AUTO_ADD_MM
        val countertopDepthMm = params.depthMm + AUTO_ADD_MM

        // 2. Габариты выреза: из столешницы вычитаем по 190 мм
        val cutoutWidthMm = countertopWidthMm - CUTOUT_REDUCTION_MM
        val cutoutDepthMm = countertopDepthMm - CUTOUT_REDUCTION_MM

        if (cutoutWidthMm <= 0 || cutoutDepthMm <= 0) {
            throw IllegalArgumentException("Размеры слишком малы для формирования выреза (вырез ≤ 0 мм)")
        }

        // Плотность основной столешницы
        val densityMain = when (params.steelType) {
            SteelType.AISI_304 -> DENSITY_AISI_304
            SteelType.AISI_430 -> DENSITY_AISI_430
        }

        // Вес столешницы: полный лист минус вырез
        val totalAreaM2 = (countertopWidthMm * countertopDepthMm) / 1_000_000.0
        val cutoutAreaM2 = (cutoutWidthMm * cutoutDepthMm) / 1_000_000.0
        val netCountertopAreaM2 = maxOf(0.0, totalAreaM2 - cutoutAreaM2)

        val thicknessM = params.thicknessMm / 1000.0
        val weightMain = netCountertopAreaM2 * thicknessM * densityMain

        // 3. Карманы (базовый + дополнительные)
        val areaFront = (countertopWidthMm / 1000.0) * (POCKET_FRONT_H / 1000.0)
        val areaBack = (countertopWidthMm / 1000.0) * (POCKET_BACK_H / 1000.0)
        val areaBottom = (countertopWidthMm / 1000.0) * (POCKET_DEPTH / 1000.0)
        val areaSides = 2 * ((POCKET_BACK_H / 1000.0) * (POCKET_DEPTH / 1000.0))

        val totalAreaPerPocket = areaFront + areaBack + areaBottom + areaSides
        val volPerPocket = totalAreaPerPocket * (POCKET_THICK / 1000.0)
        val weightPerPocket = volPerPocket * DENSITY_AISI_430

        val additionalPocketsCount = params.additionalPocketsCount
        val additionalPocketsTotalWeightKg = weightPerPocket * additionalPocketsCount
        val totalPocketsWeightKg = weightPerPocket + additionalPocketsTotalWeightKg

        // 4. Мойка (всегда AISI 304)
        val sinkW = params.widthMm - SINK_REDUCE_W
        val sinkD = params.depthMm - SINK_REDUCE_D

        if (sinkW <= 0 || sinkD <= 0) throw IllegalArgumentException("Размеры слишком малы для мойки")

        val frontBackArea = 2 * ((SINK_FIXED_H / 1000.0) * (sinkW / 1000.0))
        val bottomArea = (sinkW / 1000.0) * (sinkD / 1000.0)

        val sideH = (SINK_FIXED_H + WALL_EXTRA_H) / 1000.0
        val sideD = (sinkD + WALL_EXTRA_D) / 1000.0
        val sidesArea = 2 * (sideH * sideD)

        val volSink = (frontBackArea + bottomArea + sidesArea) * (1.0 / 1000.0) // толщина 1 мм
        val weightSink = volSink * DENSITY_AISI_304

        // 5. Вставка (AISI 430)
        val insW = sinkD + INSERT_EXTRA
        val insD = sinkW + INSERT_EXTRA
        val volInsert = (insW / 1000.0) * (insD / 1000.0) * (0.8 / 1000.0)
        val weightInsert = volInsert * DENSITY_AISI_430

        // 6. Перегородки (AISI 430)
        val part12H = (SINK_FIXED_H + PART_EXTRA_H) / 1000.0
        val part12D = sinkD / 1000.0
        val volPart12 = (part12H * part12D) * (0.8 / 1000.0)
        val weightPart12 = 2 * volPart12 * DENSITY_AISI_430 // 2 шт

        val part3W = sinkW - PART_REDUCE_W
        if (part3W <= 0) throw IllegalArgumentException("Ширина мойки слишком мала для 3-й перегородки")

        val volPart3 = ((SINK_FIXED_H / 1000.0) * (part3W / 1000.0)) * (0.8 / 1000.0)
        val weightPart3 = volPart3 * DENSITY_AISI_430

        val weightPartitions = weightPart12 + weightPart3

        // Суммирование по маркам стали
        var total304 = 0.0
        var total430 = 0.0

        when (params.steelType) {
            SteelType.AISI_304 -> total304 += weightMain
            SteelType.AISI_430 -> total430 += weightMain
        }
        // Карманы, вставка и перегородки — всегда AISI 430
        total430 += totalPocketsWeightKg + weightInsert + weightPartitions
        // Мойка — всегда AISI 304
        total304 += weightSink

        return CalculationResult(
            totalWeightKg = total304 + total430,
            countertopWeightKg = weightMain,
            pocketWeightKg = weightPerPocket,
            additionalPocketsTotalWeightKg = additionalPocketsTotalWeightKg,
            sinkWeightKg = weightSink,
            insertWeightKg = weightInsert,
            partitionsWeightKg = weightPartitions,
            weightAisi304Kg = total304,
            weightAisi430Kg = total430,
            // Если нужно показывать размеры выреза в UI, можно вернуть их:
            // sinkCutoutWidthMm = cutoutWidthMm,
            // sinkCutoutDepthMm = cutoutDepthMm,
        )
    }
}
