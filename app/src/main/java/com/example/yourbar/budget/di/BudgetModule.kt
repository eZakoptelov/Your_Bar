package com.example.yourbar.budget.di

import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.domain.calculator.usecase.CalculateBudgetUseCase
import com.example.yourbar.budget.domain.calculator.usecase.CalculatePipeMetersUseCase
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    single { CalculateBudgetUseCase() }
    single { CalculatorRepository(get()) } // get() достанет UseCase
    single { CalculatePipeMetersUseCase() }

    // Добавляем регистрацию ViewModel
    viewModel { BudgetCalculatorViewModel(get()) }
}


