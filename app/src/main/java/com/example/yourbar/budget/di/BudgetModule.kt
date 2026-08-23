package com.example.yourbar.budget.di

import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.usecase.CalculateBudgetUseCase
import org.koin.dsl.module

val budgetModule = module {
    single { CalculateBudgetUseCase() }
    single { CalculatorRepository(get()) } // get() достанет UseCase
}
