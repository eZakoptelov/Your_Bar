package com.example.yourbar.budget.domain.calculator.models

enum class SolidSinkType(
    val widthMm: Int,
    val depthMm: Int,
    val heightMm: Int,
    val label: String
) {
    NONE(0, 0, 0, "Нет"),
    SINK_400x400(400, 400, 250, "400×400"),
    SINK_400x500(400, 500, 250, "400×500"),
    SINK_500x500(500, 500, 250, "500×500"),
    SINK_500x400(500, 400, 250, "500×400")

}
