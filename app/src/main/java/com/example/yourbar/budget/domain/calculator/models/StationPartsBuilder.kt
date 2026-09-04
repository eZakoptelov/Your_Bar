package com.example.yourbar.budget.domain.calculator.models

object StationPartsBuilder {

    // Константы из CalculateBudgetUseCase (дублируем, чтобы не плодить связи)
    private const val AUTO_ADD_MM = 90
    private const val POCKET_FRONT_H = 120
    private const val POCKET_BACK_H = 260
    private const val POCKET_DEPTH = 110
    private const val POCKET_THICK = 1.5
    private const val SINK_FIXED_H = 265
    private const val SINK_REDUCE_D = 70
    private const val SINK_REDUCE_W = 70
    private const val WALL_EXTRA_H = 30
    private const val WALL_EXTRA_D = 30
    private const val INSERT_EXTRA = 35
    private const val PART_EXTRA_H = 20
    private const val PART_REDUCE_W = 220
    private const val CUTOUT_REDUCTION_MM = 190

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
                weightKg = result.countertopWeightKg
            )
        )


        // ── 2. Карман для бутылок ──
        val totalPockets = 1 + params.additionalPocketsCount
        if (totalPockets > 0) {
            val pocketWeight = (result.pocketWeightKg + result.additionalPocketsTotalWeightKg) / totalPockets
            parts.add(
                StationPart(
                    title = "Карман для бутылок",
                    dimensions = "${params.widthMm}×${POCKET_FRONT_H}×${POCKET_DEPTH} мм\n(задняя стенка ${POCKET_BACK_H} мм)",
                    steelType = "AISI 430",
                    thicknessMm = POCKET_THICK,
                    weightKg = pocketWeight,
                    quantity = totalPockets
                )
            )
        }


        // ── 3. Корпус мойки ──
        val sinkW = params.widthMm - SINK_REDUCE_W
        val sinkD = params.depthMm - SINK_REDUCE_D
        val sideH = SINK_FIXED_H + WALL_EXTRA_H
        val sideD = sinkD + WALL_EXTRA_D

        parts.add(
            StationPart(
                title = "Корпус мойки",
                dimensions = "${sinkW}×${sinkD}×${SINK_FIXED_H} мм\n(боковины ${sideH}×${sideD} мм)",
                steelType = "AISI 304",
                thicknessMm = 1.0,
                weightKg = result.sinkWeightKg
            )
        )

        // ── 4. Перфорированная вставка ──
        val insW = sinkD + INSERT_EXTRA
        val insD = sinkW + INSERT_EXTRA

        parts.add(
            StationPart(
                title = "Перфорированная вставка",
                dimensions = "${insW}×${insD} мм",
                steelType = "AISI 430",
                thicknessMm = 0.8,
                weightKg = result.insertWeightKg
            )
        )

        // ── 5. Съёмные перегородки ──
        // Перегородки 1 и 2: высота 285 мм, глубина = sinkD
        val part12H = SINK_FIXED_H + PART_EXTRA_H
        // Перегородка 3: высота 265 мм, ширина = sinkW - 220
        val part3W = sinkW - PART_REDUCE_W

        val partitionsDesc = if (part3W > 0) {
            "Перегородки 1–2: ${part12H}×${sinkD} мм\nПерегородка 3: ${SINK_FIXED_H}×${part3W} мм"
        } else {
            "Перегородки 1–2: ${part12H}×${sinkD} мм"
        }

        parts.add(
            StationPart(
                title = "Съёмные перегородки",
                dimensions = partitionsDesc,
                steelType = "AISI 430",
                thicknessMm = 0.8,
                weightKg = result.partitionsWeightKg
            )
        )

        return parts
    }
}
