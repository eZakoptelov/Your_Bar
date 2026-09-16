package com.example.yourbar.workprice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_prices")
data class WorkPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,   // "welding" или "locksmith"
    val title: String,
    val unit: String,
    var price: Int
)
