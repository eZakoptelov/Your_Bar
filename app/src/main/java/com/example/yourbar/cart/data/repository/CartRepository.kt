package com.example.yourbar.cart.data

import android.content.Context
import android.util.Log
import com.example.yourbar.cart.domain.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "cart_items_json"

    private val _items = MutableStateFlow<List<CartItem>>(loadItems())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun add(item: CartItem) {
        Log.d("CART_REPO", "add: name='${item.name}', displayName='${item.displayName}'")
        _items.value += item
        saveItems()
        Log.d("CART_REPO", "Всего элементов: ${_items.value.size}")
    }

    fun remove(id: String) {
        _items.value = _items.value.filterNot { it.id == id }
        saveItems()
    }

    fun clear() {
        _items.value = emptyList()
        saveItems()
    }

    fun totalWeight(): Double = _items.value.sumOf { it.totalWeightKg }
    fun totalPipeMeters(): Double = _items.value.sumOf { it.pipeMeters }
    fun itemCount(): Int = _items.value.size

    // ── Сохранение в SharedPreferences ──
    private fun saveItems() {
        val json = gson.toJson(_items.value)
        prefs.edit().putString(key, json).apply()
    }

    // ── Загрузка из SharedPreferences ──
    private fun loadItems(): List<CartItem> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson<List<CartItem>>(json, type) ?: emptyList()
        }.getOrElse {
            Log.e("CART_REPO", "Ошибка загрузки корзины", it)
            emptyList()
        }
    }
}
