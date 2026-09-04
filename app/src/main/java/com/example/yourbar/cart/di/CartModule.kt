package com.example.yourbar.cart.di

import com.example.yourbar.cart.data.CartRepository
import com.example.yourbar.cart.domain.usecase.AddToCartUseCase
import org.koin.dsl.module

val cartModule = module {
    single { CartRepository(get()) }
    factory { AddToCartUseCase(get()) }
}
