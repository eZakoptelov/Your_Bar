package com.example.yourbar.price.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.yourbar.price.data.PriceEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {

    @Query("SELECT * FROM prices WHERE id = 0")
    fun getPrices(): Flow<PriceEntity?>

    @Query("SELECT * FROM prices WHERE id = 0")
    fun getPricesSync(): PriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PriceEntity)
}
