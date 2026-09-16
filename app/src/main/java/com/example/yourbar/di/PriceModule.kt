package com.example.yourbar.di

import com.example.yourbar.budget.data.price.PriceRepository
import com.example.yourbar.db.AppDatabase
import org.koin.dsl.module

val priceModule = module {
    single { get<AppDatabase>().priceDao() }
    single { PriceRepository(get()) }
}
