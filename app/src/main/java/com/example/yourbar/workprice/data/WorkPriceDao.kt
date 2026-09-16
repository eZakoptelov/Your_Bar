package com.example.yourbar.workprice.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkPriceDao {

    @Query("SELECT * FROM work_prices WHERE category = :category ORDER BY id")
    fun observeByCategory(category: String): Flow<List<WorkPriceEntity>>

    @Query("SELECT COUNT(*) FROM work_prices")
    suspend fun count(): Int

    @Query("INSERT INTO work_prices (category, title, unit, price) VALUES (:category, :title, :unit, :price)")
    suspend fun insert(category: String, title: String, unit: String, price: Int)

    @Update
    suspend fun update(entity: WorkPriceEntity)
}
