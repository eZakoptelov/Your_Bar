package com.example.yourbar.cart.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val widthMm: Int,
    val depthMm: Int,
    val heightMm: Int,
    val steelType: String,
    val thicknessMm: Double,
    val pocketsCount: Int,
    val totalWeightKg: Double,
    val weightAisi304Kg: Double,
    val weightAisi430Kg: Double,
    val pipeMeters: Double,
    val isBlenderShelfAdded: Boolean,
    val blenderShelfWidthMm: Int,
    val insulationAreaSqM: Double,
    val faucetHoleCount: Int,
    val faucetHolePricePerUnit: Double,
    val backBoardCount: Int,
    val backBoardPricePerUnit: Double,
    val adjustableLegCount: Int,
    val adjustableLegPricePerUnit: Double,
    val pocketHeightMm: Int,
    val solidSinkType: String?,
    val solidSinkPrice: Double
)
