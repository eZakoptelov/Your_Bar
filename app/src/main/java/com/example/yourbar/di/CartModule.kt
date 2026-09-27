package com.example.yourbar.di

import com.example.yourbar.cart.data.repository.CartRepository
import com.example.yourbar.cart.domain.usecase.AddToCartUseCase
import com.example.yourbar.db.AppDatabase
import org.koin.dsl.module

val cartModule = module {
    single { CartRepository(get()) }
    factory { AddToCartUseCase(get()) }
    single { get<AppDatabase>().cartDao() }

}
