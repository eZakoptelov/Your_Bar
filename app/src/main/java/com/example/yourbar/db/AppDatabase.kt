package com.example.yourbar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.yourbar.price.data.PriceDao
import com.example.yourbar.price.data.PriceEntity
import com.example.yourbar.workprice.data.WorkPriceDao
import com.example.yourbar.workprice.data.WorkPriceEntity

@Database(
    entities = [WorkPriceEntity::class, PriceEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workPriceDao(): WorkPriceDao
    abstract fun priceDao(): PriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yourbar.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}