package com.example.yourbar.price.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey
    val id: Int = 0, // одна строка — один набор цен
    val aisi304PerKg: Double,
    val aisi430PerKg: Double,
    val pipe25PerM: Double,
    val pipe40PerM: Double,
    val insulationPerM2: Double,
    val faucetHolePerPiece: Double,
    val backBoardPerPiece: Double,
    val adjustableLegPerPiece: Double,
    val sink400x400PerPiece: Double,
    val sink400x500PerPiece: Double,
    val sink500x500PerPiece: Double,
    val sink500x400PerPiece: Double
)
