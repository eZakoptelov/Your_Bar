package com.example.yourbar.di

import android.content.Context
import com.example.yourbar.budget.data.calculator.CalculatorRepository
import com.example.yourbar.budget.data.price.PriceRepository
import com.example.yourbar.budget.domain.calculator.usecase.CalculateBudgetUseCase
import com.example.yourbar.budget.domain.calculator.usecase.CalculatePipeMetersUseCase
import com.example.yourbar.budget.domain.calculator.usecase.GetStationDetailsUseCase
import com.example.yourbar.budget.presentation.calculator.helper.AddToCartHelper
import com.example.yourbar.budget.presentation.viewmodel.BudgetCalculatorViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    single { CalculateBudgetUseCase() }
    single { CalculatorRepository(get()) }
    single { CalculatePipeMetersUseCase() }
    factory { GetStationDetailsUseCase(get()) }
    single { PriceRepository(get()) }
    viewModel { BudgetCalculatorViewModel(get()) }
    single {
        AddToCartHelper(
            addToCartUseCase = get(),
            sharedPreferences = androidContext().getSharedPreferences(
                "prices_settings",
                Context.MODE_PRIVATE
            )
        )
    }

}
