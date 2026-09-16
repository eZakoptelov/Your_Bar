package com.example.yourbar.di

import com.example.yourbar.db.AppDatabase
import com.example.yourbar.workprice.data.WorkPriceRepository
import com.example.yourbar.workprice.presentation.viewmodel.WorkPriceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val workPriceModule: Module = module {
    single { get<AppDatabase>().workPriceDao() }
    single { WorkPriceRepository(get()) }
    viewModel { WorkPriceViewModel(get()) }
}
