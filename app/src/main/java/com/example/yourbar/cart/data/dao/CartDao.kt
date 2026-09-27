package com.example.yourbar.cart.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.yourbar.cart.data.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY id")
    fun observeAll(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CartEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun remove(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun count(): Int
}
