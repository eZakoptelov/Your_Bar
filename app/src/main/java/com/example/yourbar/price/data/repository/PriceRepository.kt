package com.example.yourbar.budget.data.price

import android.content.Context
import android.content.SharedPreferences
import com.example.yourbar.price.domain.models.BudgetPrices
import androidx.core.content.edit

class PriceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "prices_settings"
        private const val KEY_AISI_304 = "price_aisi_304"
        private const val KEY_AISI_430 = "price_aisi_430"
        private const val KEY_PIPE_25 = "price_pipe_25"
        private const val KEY_PIPE_40 = "price_pipe_40"
        private const val KEY_INSULATION = "price_insulation"
        private const val KEY_FAUCET_HOLE = "price_faucet_hole"
        private const val KEY_BACK_BOARD = "price_back_board"
        private const val KEY_ADJUSTABLE_LEG = "price_adjustable_leg"
        private const val KEY_SINK_400_400 = "price_sink_400x400"
        private const val KEY_SINK_400_500 = "price_sink_400x500"
        private const val KEY_SINK_500_500 = "price_sink_500x500"
        private const val KEY_SINK_500_400 = "price_sink_500x400"
    }

    fun save(prices: BudgetPrices) {
        prefs.edit {
            putFloat(KEY_AISI_304, prices.aisi304PerKg.toFloat())
            putFloat(KEY_AISI_430, prices.aisi430PerKg.toFloat())
            putFloat(KEY_PIPE_25, prices.pipe25PerM.toFloat())
            putFloat(KEY_PIPE_40, prices.pipe40PerM.toFloat())
            putFloat(KEY_INSULATION, prices.insulationPerM2.toFloat())
            putFloat(KEY_FAUCET_HOLE, prices.faucetHolePerPiece.toFloat())
            putFloat(KEY_BACK_BOARD, prices.backBoardPerPiece.toFloat())
            putFloat(KEY_ADJUSTABLE_LEG, prices.adjustableLegPerPiece.toFloat())
            putFloat(KEY_SINK_400_400, prices.sink400x400PerPiece.toFloat())
            putFloat(KEY_SINK_400_500, prices.sink400x500PerPiece.toFloat())
            putFloat(KEY_SINK_500_500, prices.sink500x500PerPiece.toFloat())
            putFloat(KEY_SINK_500_400, prices.sink500x400PerPiece.toFloat())
        }
    }

    fun load(): BudgetPrices = BudgetPrices(
        aisi304PerKg = prefs.getFloat(KEY_AISI_304, 0f).toDouble(),
        aisi430PerKg = prefs.getFloat(KEY_AISI_430, 0f).toDouble(),
        pipe25PerM = prefs.getFloat(KEY_PIPE_25, 0f).toDouble(),
        pipe40PerM = prefs.getFloat(KEY_PIPE_40, 0f).toDouble(),
        insulationPerM2 = prefs.getFloat(KEY_INSULATION, 0f).toDouble(),
        faucetHolePerPiece = prefs.getFloat(KEY_FAUCET_HOLE, 0f).toDouble(),
        backBoardPerPiece = prefs.getFloat(KEY_BACK_BOARD, 0f).toDouble(),
        adjustableLegPerPiece = prefs.getFloat(KEY_ADJUSTABLE_LEG, 0f).toDouble(),
        sink400x400PerPiece = prefs.getFloat(KEY_SINK_400_400, 0f).toDouble(),
        sink400x500PerPiece = prefs.getFloat(KEY_SINK_400_500, 0f).toDouble(),
        sink500x500PerPiece = prefs.getFloat(KEY_SINK_500_500, 0f).toDouble(),
        sink500x400PerPiece = prefs.getFloat(KEY_SINK_500_400, 0f).toDouble()

    )
}
