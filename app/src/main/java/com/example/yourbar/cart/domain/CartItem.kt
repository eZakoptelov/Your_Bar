package com.example.yourbar.cart.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
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
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    val displayName: String
        get() = if (name.isNotBlank()) name else "Станция ${widthMm}×${depthMm}×${heightMm} мм"
}
