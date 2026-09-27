package com.example.yourbar.cart.data.repository

import android.util.Log
import com.example.yourbar.cart.data.dao.CartDao
import com.example.yourbar.cart.data.mapper.toDomain
import com.example.yourbar.cart.data.mapper.toEntity
import com.example.yourbar.cart.domain.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val dao: CartDao) {

    // Теперь items — это Flow из Room, всегда актуальный
    val items: Flow<List<CartItem>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun add(item: CartItem) {
        Log.d("CART_REPO", "add: name='${item.name}', displayName='${item.displayName}'")
        dao.insert(item.toEntity())
    }

    suspend fun remove(id: String) {
        dao.remove(id)
    }

    suspend fun clear() {
        dao.clear()
    }

    suspend fun totalWeight(): Double {
        // Если нужен — можно добавить @Query в DAO
        return 0.0
    }

    suspend fun itemCount(): Int = dao.count()
}
