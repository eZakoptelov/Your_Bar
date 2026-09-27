package com.example.yourbar

import android.app.Application
import com.example.yourbar.di.budgetModule
import com.example.yourbar.di.calculatorModule
import com.example.yourbar.di.cartModule
import com.example.yourbar.di.databaseModule
import com.example.yourbar.di.priceModule
import com.example.yourbar.di.workPriceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class YourBarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@YourBarApplication)
            modules(
                databaseModule,
                budgetModule,
                cartModule,
                calculatorModule,
                workPriceModule,
                priceModule
            )
        }
    }
}
