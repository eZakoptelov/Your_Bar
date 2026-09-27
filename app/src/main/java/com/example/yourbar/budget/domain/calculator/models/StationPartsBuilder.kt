package com.example.yourbar.budget.domain.calculator.models

object StationPartsBuilder {

    private const val AUTO_ADD_MM = 90
    private const val POCKET_FRONT_H = 120
    private const val POCKET_BACK_H = 260
    private const val POCKET_DEPTH = 110
    private const val POCKET_THICK = 1.5
    private const val SINK_FIXED_H = 250
    private const val SINK_REDUCE_D = 70
    private const val SINK_REDUCE_W = 70
    private const val PART_ADD_H = 5
    private const val PART_REDUCE_W = 220

    private const val SHELF_DEPTH = 230
    private const val SHELF_HEIGHT = 310
    private const val SHELF_THICK = 1.5

    fun build(
        params: CalculationInputParams,
        result: CalculationResult,
        isAdmin: Boolean = true
    ): List<StationPart> {
        val parts = mutableListOf<StationPart>()
        val steel = params.steelType.displayName

        // Для обычного пользователя вес не показываем
        val w = if (isAdmin) 1.0 else 0.0

        // ── 1. Столешница ──
        val countertopW = params.widthMm + AUTO_ADD_MM
        val countertopD = params.depthMm + AUTO_ADD_MM

        parts.add(
            StationPart(
                title = "Столешница",
                dimensions = if (isAdmin) {
                    "${countertopW}×${countertopD} мм"
                } else {
                    "${params.widthMm}×${params.depthMm} мм"
                },
                steelType = steel,
                thicknessMm = params.thicknessMm,
                weightKg = result.countertopWeightKg * w,
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
                    dimensions = if (isAdmin) {
                        "${pocketWidthMm}×${POCKET_FRONT_H}×${POCKET_DEPTH} мм\n" +
                                "Задняя стенка ${params.pocketHeightMm} мм\n" +
                                "Боковые стенки: 2 шт × ${params.pocketHeightMm - 10}×${POCKET_DEPTH} мм"
                    } else {
                        "${pocketWidthMm} мм"
                    },
                    steelType = "AISI 430",
                    thicknessMm = POCKET_THICK,
                    weightKg = pocketWeight * w,
                    quantity = params.pocketCount
                )
            )
        }

        // ── 3. Ванна для льда ──
        val sinkW = if (params.solidSinkType != SolidSinkType.NONE) {
            params.widthMm - params.solidSinkType.widthMm - 120
        } else {
            params.widthMm - SINK_REDUCE_W
        }
        val sinkD = params.depthMm - SINK_REDUCE_D

        parts.add(
            StationPart(
                title = "Ванна для льда",
                dimensions = if (isAdmin) {
                    "${sinkW}×${sinkD}×${SINK_FIXED_H} мм"
                } else {
                    val userSinkW = if (params.solidSinkType != SolidSinkType.NONE) {
                        params.widthMm - params.solidSinkType.widthMm - 150
                    } else {
                        params.widthMm - 150
                    }
                    val userSinkD = params.depthMm - 100
                    "${userSinkW}×${userSinkD}×${SINK_FIXED_H} мм"
                },
                steelType = "AISI 304",
                thicknessMm = 1.0,
                weightKg = result.sinkWeightKg * w,
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
                    steelType = "—",
                    thicknessMm = 0.0,
                    weightKg = 0.0,
                    quantity = 1
                )
            )
        }

        // ── 4. Перфорированная вставка (только админ) ──
        if (isAdmin) {
            val insW = params.widthMm - 75
            val insD = params.depthMm - 80

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
        }

        // ── 5. Съёмные перегородки (только админ) ──
        if (isAdmin) {
            val part12H = SINK_FIXED_H + PART_ADD_H
            val part12D = sinkD - 32
            val part3W = sinkW - 252
            val part3H = SINK_FIXED_H + PART_ADD_H

            val partitionsDesc = if (part3W > 0) {
                "Перегородки 1–2: ${part12H}×${part12D} мм\nПерегородка 3: ${part3H}×${part3W} мм"
            } else {
                "Перегородки 1–2: ${part12H}×${part12D} мм"
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
        }

        // ── 6. Полка для блендера ──
        if (params.isShelfAdded && result.blenderShelfWeightKg > 0.0) {
            val shelfSideH = SHELF_HEIGHT - 10
            val shelfSideD = SHELF_DEPTH - 10

            parts.add(
                StationPart(
                    title = "Полка для блендера",
                    dimensions = if (isAdmin) {
                        "${params.blenderShelfWidthMm}×${SHELF_DEPTH}×${SHELF_HEIGHT} мм\n" +
                                "боковые стенки: 2 шт × ${shelfSideH}×${shelfSideD} мм (треугольник)"
                    } else {
                        "${params.blenderShelfWidthMm} мм"
                    },
                    steelType = "AISI 430",
                    thicknessMm = SHELF_THICK,
                    weightKg = result.blenderShelfWeightKg * w,
                    quantity = 1
                )
            )
        }

        // ── 7. Теплоизоляция (только админ) ──
        if (isAdmin && result.insulationAreaSqM > 0.0) {
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

        // ── 7a. Фанера (только админ) ──
        if (isAdmin) {
            val plywoodArea = (params.widthMm * params.depthMm) / 1_000_000.0

            parts.add(
                StationPart(
                    title = "Фанера",
                    dimensions = "${String.format("%.2f", plywoodArea)} м²",
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

        // ── 10. Регулируемые опоры (только админ) ──
        if (isAdmin && result.adjustableLegCount > 0) {
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
