package com.example.yourbar.budget.data.price

import com.example.yourbar.price.data.repository.PriceDao
import com.example.yourbar.price.data.PriceEntity
import com.example.yourbar.price.domain.models.BudgetPrices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PriceRepository(
    private val dao: PriceDao
) {
    fun observe(): Flow<BudgetPrices?> {
        return dao.getPrices().map { entity ->
            entity?.toBudgetPrices()
        }
    }

    fun load(): BudgetPrices {
        return dao.getPricesSync()?.toBudgetPrices() ?: BudgetPrices()
    }

    suspend fun save(prices: BudgetPrices) {
        dao.insert(prices.toEntity())
    }

    private fun PriceEntity.toBudgetPrices() = BudgetPrices(
        aisi304PerKg = aisi304PerKg,
        aisi430PerKg = aisi430PerKg,
        pipe25PerM = pipe25PerM,
        pipe40PerM = pipe40PerM,
        insulationPerM2 = insulationPerM2,
        plywoodPerM2 = plywoodPerM2,
        adjustableLegPerPiece = adjustableLegPerPiece,
        sink400x400PerPiece = sink400x400PerPiece,
        sink400x500PerPiece = sink400x500PerPiece,
        sink500x500PerPiece = sink500x500PerPiece,
        sink500x400PerPiece = sink500x400PerPiece
    )

    private fun BudgetPrices.toEntity() = PriceEntity(
        id = 0,
        aisi304PerKg = aisi304PerKg,
        aisi430PerKg = aisi430PerKg,
        pipe25PerM = pipe25PerM,
        pipe40PerM = pipe40PerM,
        insulationPerM2 = insulationPerM2,
        plywoodPerM2 = plywoodPerM2,
        adjustableLegPerPiece = adjustableLegPerPiece,
        sink400x400PerPiece = sink400x400PerPiece,
        sink400x500PerPiece = sink400x500PerPiece,
        sink500x500PerPiece = sink500x500PerPiece,
        sink500x400PerPiece = sink500x400PerPiece
    )
}
