package com.example.yourbar.budget.domain.calculator.models

object StationPartsBuilder {

    private const val AUTO_ADD_MM = 90
    private const val POCKET_FRONT_H = 120
    private const val POCKET_BACK_H = 260
    private const val POCKET_DEPTH = 110
    private const val POCKET_THICK = 1.5
    private const val SINK_FIXED_H = 265
    private const val SINK_REDUCE_D = 70
    private const val SINK_REDUCE_W = 70
    private const val PART_REDUCE_H = 10
    private const val PART_REDUCE_W = 220

    // Полка для блендера: глубина и высота фиксированы, ширина берётся из params
    private const val SHELF_DEPTH = 230
    private const val SHELF_HEIGHT = 310
    private const val SHELF_THICK = 1.5

    fun build(
        params: CalculationInputParams,
        result: CalculationResult
    ): List<StationPart> {
        val parts = mutableListOf<StationPart>()
        val steel = params.steelType.displayName

        // ── 1. Столешница ──
        val countertopW = params.widthMm + AUTO_ADD_MM
        val countertopD = params.depthMm + AUTO_ADD_MM

        parts.add(
            StationPart(
                title = "Столешница",
                dimensions = "${countertopW}×${countertopD} мм",
                steelType = steel,
                thicknessMm = params.thicknessMm,
                weightKg = result.countertopWeightKg,
                quantity = 1
            )
        )

        // ── 2. Карман для бутылок ──
        if (params.pocketCount > 0) {
            val pocketWidthMm = if (params.isShelfAdded) {
                maxOf(params.widthMm - params.blenderShelfWidthMm, 0)
            } else {
                params.widthMm
            }

            val pocketWeight = (result.pocketWeightKg + result.additionalPocketsTotalWeightKg) / params.pocketCount

            parts.add(
                StationPart(
                    title = "Карман для бутылок",
                    dimensions = "${pocketWidthMm}×${POCKET_FRONT_H}×${POCKET_DEPTH} мм\n(задняя стенка ${params.pocketHeightMm} мм)",
                    steelType = "AISI 430",
                    thicknessMm = POCKET_THICK,
                    weightKg = pocketWeight,
                    quantity = params.pocketCount
                )
            )
        }

        // ── 3. Корпус мойки ──
        val sinkW = if (params.solidSinkType != SolidSinkType.NONE) {
            params.widthMm - params.solidSinkType.widthMm - 100
        } else {
            params.widthMm - SINK_REDUCE_W
        }
        val sinkD = params.depthMm - SINK_REDUCE_D

        parts.add(
            StationPart(
                title = "Корпус мойки",
                dimensions = "${sinkW}×${sinkD}×${SINK_FIXED_H} мм",
                steelType = "AISI 304",
                thicknessMm = 1.0,
                weightKg = result.sinkWeightKg,
                quantity = 1
            )
        )

        // ── 3a. Цельнотянутая мойка ──
        if (params.solidSinkType != SolidSinkType.NONE) {
            val ss = params.solidSinkType
            parts.add(
                StationPart(
                    title = "Цельнотянутая мойка",
                    dimensions = "${ss.widthMm}×${ss.depthMm}×${ss.heightMm} мм",
                    steelType = "AISI 304",
                    thicknessMm = 1.0,
                    weightKg = 0.0,
                    quantity = 1
                )
            )
        }

        // ── 4. Перфорированная вставка ──
        val insW = params.widthMm - SINK_REDUCE_W
        val insD = params.depthMm - SINK_REDUCE_D

        parts.add(
            StationPart(
                title = "Перфорированная вставка",
                dimensions = "${insW}×${insD} мм",
                steelType = "AISI 430",
                thicknessMm = 0.8,
                weightKg = result.insertWeightKg,
                quantity = 1
            )
        )

        // ── 5. Съёмные перегородки ──
        val part12H = SINK_FIXED_H - PART_REDUCE_H
        val part3W = sinkW - PART_REDUCE_W
        val part3H = SINK_FIXED_H - PART_REDUCE_H

        val partitionsDesc = if (part3W > 0) {
            "Перегородки 1–2: ${part12H}×${sinkD} мм\nПерегородка 3: ${part3H}×${part3W} мм"
        } else {
            "Перегородки 1–2: ${part12H}×${sinkD} мм"
        }

        parts.add(
            StationPart(
                title = "Съёмные перегородки",
                dimensions = partitionsDesc,
                steelType = "AISI 430",
                thicknessMm = 0.8,
                weightKg = result.partitionsWeightKg,
                quantity = 1
            )
        )

        // ── 6. Полка для блендера ──
        if (params.isShelfAdded && result.blenderShelfWeightKg > 0.0) {
            parts.add(
                StationPart(
                    title = "Полка для блендера",
                    dimensions = "${params.blenderShelfWidthMm}×${SHELF_DEPTH}×${SHELF_HEIGHT} мм",
                    steelType = "AISI 430",
                    thicknessMm = SHELF_THICK,
                    weightKg = result.blenderShelfWeightKg,
                    quantity = 1
                )
            )
        }

        // ── 7. Теплоизоляция ──
        if (result.insulationAreaSqM > 0.0) {
            parts.add(
                StationPart(
                    title = "Теплоизоляция",
                    dimensions = "${String.format("%.2f", result.insulationAreaSqM)} м²",
                    steelType = "—",
                    thicknessMm = 0.0,
                    weightKg = 0.0,
                    quantity = 1
                )
            )
        }

        // ── 8. Отверстие для смесителя ──
        if (result.faucetHoleCount > 0) {
            parts.add(
                StationPart(
                    title = "Отверстие для смесителя",
                    dimensions = "${result.faucetHoleCount} шт",
                    steelType = "—",
                    thicknessMm = 0.0,
                    weightKg = 0.0,
                    quantity = result.faucetHoleCount
                )
            )
        }

        // ── 9. Задний борт ──
        if (result.backBoardCount > 0) {
            parts.add(
                StationPart(
                    title = "Задний борт",
                    dimensions = "${result.backBoardCount} шт",
                    steelType = "—",
                    thicknessMm = 0.0,
                    weightKg = 0.0,
                    quantity = result.backBoardCount
                )
            )
        }

        // ── 10. Регулируемые опоры ──
        if (result.adjustableLegCount > 0) {
            parts.add(
                StationPart(
                    title = "Регулируемая опора",
                    dimensions = "${result.adjustableLegCount} шт",
                    steelType = "—",
                    thicknessMm = 0.0,
                    weightKg = result.adjustableLegCount * 0.45,
                    quantity = result.adjustableLegCount
                )
            )
        }

        return parts
    }
}
