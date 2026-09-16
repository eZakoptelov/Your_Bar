package com.example.yourbar.di

import com.example.yourbar.db.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(get()) }
}
