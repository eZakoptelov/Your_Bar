package com.example.yourbar.workprice.domain

data class WorkPriceItem(
    val title: String,
    val unit: String,      // "м", "шт", "м²", "точка"
    val price: Int         // цена в рублях
)
