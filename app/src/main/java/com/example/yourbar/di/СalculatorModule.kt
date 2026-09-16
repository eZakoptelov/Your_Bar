package com.example.yourbar.di

import com.example.yourbar.budget.domain.calculator.usecase.CalculateParamsUseCase
import org.koin.dsl.module

val calculatorModule = module {
    // ...
    factory { CalculateParamsUseCase(get()) }
}
