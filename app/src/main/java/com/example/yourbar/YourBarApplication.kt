package com.example.yourbar

import android.app.Application
import com.example.yourbar.di.appModule
import com.example.yourbar.budget.di.budgetModule
import com.example.yourbar.budget.di.calculatorModule
import com.example.yourbar.cart.di.cartModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class YourBarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@YourBarApplication)
            modules(
                appModule,
                budgetModule,
                cartModule,
                calculatorModule
            )
        }
    }
}
