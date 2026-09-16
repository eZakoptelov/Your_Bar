package com.example.yourbar.cart.domain

import android.os.Parcelable
import java.util.UUID
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val widthMm: Int,
    val depthMm: Int,
    val heightMm: Int,
    val steelType: String,
    val thicknessMm: Double,
    val pocketsCount: Int,
    val totalWeightKg: Double,
    val weightAisi304Kg: Double = 0.0,
    val weightAisi430Kg: Double = 0.0,
    val pipeMeters: Double,
    val isBlenderShelfAdded: Boolean = false,
    val blenderShelfWidthMm: Int = 0,
    val insulationAreaSqM: Double = 0.0,
    val faucetHoleCount: Int = 0,
    val faucetHolePricePerUnit: Double = 0.0,
    val backBoardCount: Int = 0,
    val backBoardPricePerUnit: Double = 0.0,
    val adjustableLegCount: Int = 0,                 // ← новая опора
    val adjustableLegPricePerUnit: Double = 0.0,  // ← цена за штуку
    val pocketHeightMm: Int = 260,
    val solidSinkType: String = "NONE",
    val solidSinkPrice: Double = 0.0
) : Parcelable {
    val displayName: String
        get() = if (name.isNotBlank()) name else "Станция ${widthMm}×${depthMm}×${heightMm} мм"

    val faucetHoleTotalPrice: Double
        get() = faucetHoleCount * faucetHolePricePerUnit

    val backBoardTotalPrice: Double
        get() = backBoardCount * backBoardPricePerUnit

    val adjustableLegTotalPrice: Double              // ← общая цена опор
        get() = adjustableLegCount * adjustableLegPricePerUnit
}
