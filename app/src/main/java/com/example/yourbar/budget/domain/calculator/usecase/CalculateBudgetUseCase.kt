package com.example.yourbar.budget.domain.calculator.usecase

import com.example.yourbar.budget.domain.calculator.models.CalculationInputParams
import com.example.yourbar.budget.domain.calculator.models.CalculationResult
import com.example.yourbar.budget.domain.calculator.models.SolidSinkType
import com.example.yourbar.budget.domain.calculator.models.SteelType

class CalculateBudgetUseCase {

    companion object {
        private const val DENSITY_AISI_304 = 7900.0
        private const val DENSITY_AISI_430 = 7700.0

        private const val POCKET_FRONT_H = 120
        private const val POCKET_DEPTH = 110
        private const val POCKET_THICK = 1.5

        private const val AUTO_ADD_MM = 90
        private const val SINK_FIXED_H = 265
        private const val SINK_REDUCE_D = 70
        private const val SINK_REDUCE_W = 70
        private const val WALL_EXTRA_H = 30
        private const val WALL_EXTRA_D = 30
        private const val PART_REDUCE_H = 10
        private const val PART_REDUCE_W = 220

        private const val SHELF_DEPTH = 230
        private const val SHELF_HEIGHT = 310
        private const val SHELF_THICK = 1.5
        private const val LEG_WEIGHT_KG = 0.45
    }

    fun execute(params: CalculationInputParams): CalculationResult {
        val adjustableLegCount = params.adjustableLegCount
        val faucetHoleCount = params.faucetHoleCount
        val backBoardCount = params.backBoardCount

        // 1. Габариты столешницы
        val countertopWidthMm = params.widthMm + AUTO_ADD_MM
        val countertopDepthMm = params.depthMm + AUTO_ADD_MM

        val densityMain = when (params.steelType) {
            SteelType.AISI_304 -> DENSITY_AISI_304
            SteelType.AISI_430 -> DENSITY_AISI_430
        }

        val totalAreaM2 = (countertopWidthMm * countertopDepthMm) / 1_000_000.0
        val thicknessM = params.thicknessMm / 1000.0
        val weightMain = totalAreaM2 * thicknessM * densityMain

        // 2. Карманы
        val pocketBackH = params.pocketHeightMm

        val pocketWidthMm = if (params.isShelfAdded) {
            maxOf(params.widthMm - params.blenderShelfWidthMm, 0)
        } else {
            params.widthMm
        }

        val (weightPerPocket, totalPocketsWeightKg, additionalPocketsTotalWeightKg) = if (params.pocketCount > 0) {
            val areaFront = (pocketWidthMm / 1000.0) * (POCKET_FRONT_H / 1000.0)
            val areaBack = (pocketWidthMm / 1000.0) * (pocketBackH / 1000.0)
            val areaBottom = (pocketWidthMm / 1000.0) * (POCKET_DEPTH / 1000.0)
            val sideH = pocketBackH - 10
            val areaSides = 2 * ((sideH / 1000.0) * (POCKET_DEPTH / 1000.0))

            val totalAreaPerPocket = areaFront + areaBack + areaBottom + areaSides
            val volPerPocket = totalAreaPerPocket * (POCKET_THICK / 1000.0)
            val wp = volPerPocket * DENSITY_AISI_430

            val total = wp * params.pocketCount
            val additional = if (params.pocketCount > 1) wp * (params.pocketCount - 1) else 0.0
            Triple(wp, total, additional)
        } else {
            Triple(0.0, 0.0, 0.0)
        }

        // 3. Мойка
        val solidSink = params.solidSinkType

        val sinkW = if (solidSink != SolidSinkType.NONE) {
            params.widthMm - solidSink.widthMm - 120
        } else {
            params.widthMm - SINK_REDUCE_W
        }

        val sinkD = params.depthMm - SINK_REDUCE_D

        if (sinkW <= 0 || sinkD <= 0) throw IllegalArgumentException("Размеры слишком малы для мойки")

        val insulationAreaSqM = (sinkW / 1000.0) * (sinkD / 1000.0)

        val frontBackArea = 2 * ((SINK_FIXED_H / 1000.0) * (sinkW / 1000.0))
        val bottomArea = (sinkW / 1000.0) * (sinkD / 1000.0)

        val sideH = (SINK_FIXED_H + WALL_EXTRA_H) / 1000.0
        val sideD = (sinkD + WALL_EXTRA_D) / 1000.0
        val sidesArea = 2 * (sideH * sideD)

        val volSink = (frontBackArea + bottomArea + sidesArea) * (1.0 / 1000.0)
        val weightSink = volSink * DENSITY_AISI_304

        // 4. Вставка дренажная
        val insW = sinkW
        val insD = sinkD
        val volInsert = (insW / 1000.0) * (insD / 1000.0) * (0.8 / 1000.0)
        val weightInsert = volInsert * DENSITY_AISI_430

        // 5. Перегородки
        val part12H = (SINK_FIXED_H - PART_REDUCE_H) / 1000.0
        val part12D = sinkD / 1000.0
        val volPart12 = (part12H * part12D) * (0.8 / 1000.0)
        val weightPart12 = 2 * volPart12 * DENSITY_AISI_430

        val part3W = sinkW - PART_REDUCE_W
        if (part3W <= 0) throw IllegalArgumentException("Ширина мойки слишком мала для 3-й перегородки")

        val part3H = (SINK_FIXED_H - PART_REDUCE_H) / 1000.0
        val volPart3 = (part3H * (part3W / 1000.0)) * (0.8 / 1000.0)
        val weightPart3 = volPart3 * DENSITY_AISI_430

        val weightPartitions = weightPart12 + weightPart3

        // 6. Полка для блендера
        val shelfWidthMm = params.blenderShelfWidthMm
        val blenderShelfWeight = if (params.isShelfAdded && shelfWidthMm > 0) {
            val shelfSideH = SHELF_HEIGHT - 10   // 300
            val shelfSideD = SHELF_DEPTH - 10    // 220
            // Две треугольные боковушки, каждая — прямоугольный треугольник: (h × d) / 2
            val sideTrianglesArea = 2.0 * ((shelfSideH * shelfSideD) / 2.0)

            val shelfAreaM2 = (shelfWidthMm * SHELF_DEPTH + shelfWidthMm * SHELF_HEIGHT + sideTrianglesArea) / 1_000_000.0
            shelfAreaM2 * (SHELF_THICK / 1000.0) * DENSITY_AISI_430
        } else 0.0


        // Суммирование по маркам стали
        var total304 = 0.0
        var total430 = 0.0

        when (params.steelType) {
            SteelType.AISI_304 -> total304 += weightMain
            SteelType.AISI_430 -> total430 += weightMain
        }
        total430 += totalPocketsWeightKg + weightInsert + weightPartitions + blenderShelfWeight
        total304 += weightSink

        val totalLegWeight = adjustableLegCount * LEG_WEIGHT_KG

        return CalculationResult(
            totalWeightKg = total304 + total430,
            countertopWeightKg = weightMain,
            pocketWeightKg = if (params.pocketCount > 0) weightPerPocket else 0.0,
            additionalPocketsTotalWeightKg = additionalPocketsTotalWeightKg,
            sinkWeightKg = weightSink,
            insertWeightKg = weightInsert,
            partitionsWeightKg = weightPartitions,
            weightAisi304Kg = total304,
            weightAisi430Kg = total430,
            blenderShelfWeightKg = blenderShelfWeight,
            insulationAreaSqM = insulationAreaSqM,
            faucetHoleCount = faucetHoleCount,
            backBoardCount = backBoardCount,
            adjustableLegCount = adjustableLegCount,
            pocketHeightMm = params.pocketHeightMm,
            solidSinkType = params.solidSinkType
        )
    }
}
