package com.example.yourbar.budget.domain.calculator.usecase

data class PipeInput(
    val widthMm: Int,
    val depthMm: Int,
    val heightMm: Int
)

class CalculatePipeMetersUseCase {

    /**
     * Считает погонные метры трубы 25×25 по формуле:
     * 4 ноги: высота − 60 мм
     * 4 трубы по ширине: ширина − 60 мм
     * 4 трубы по глубине: глубина − 60 мм
     */
    fun execute(input: PipeInput): Double {
        require(input.widthMm > 60) { "Ширина должна быть больше 60 мм" }
        require(input.depthMm > 60) { "Глубина должна быть больше 60 мм" }
        require(input.heightMm > 60) { "Высота должна быть больше 60 мм" }

        val legsMeters = 4 * ((input.heightMm - 60) / 1000.0)
        val widthPipesMeters = 4 * ((input.widthMm - 60) / 1000.0)
        val depthPipesMeters = 4 * ((input.depthMm - 60) / 1000.0)

        return legsMeters + widthPipesMeters + depthPipesMeters
    }
}
