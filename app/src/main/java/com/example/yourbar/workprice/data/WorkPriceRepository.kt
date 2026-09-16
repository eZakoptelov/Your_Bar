package com.example.yourbar.workprice.data

import kotlinx.coroutines.flow.Flow

class WorkPriceRepository(private val dao: WorkPriceDao) {

    fun observeWelding(): Flow<List<WorkPriceEntity>> =
        dao.observeByCategory("welding")

    fun observeLocksmith(): Flow<List<WorkPriceEntity>> =
        dao.observeByCategory("locksmith")

    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return

        val welding = listOf(
            "Сварка столешницы " to "м шва",
            "Сварка кармана для бутылок" to "шт",
            "Сварка корпуса мойки" to "шт",
            "Сварка полки для блендера" to "шт",
            "Сварка цельнотянутой мойки" to "шт",
            "Сварка каркаса из 25 трубы" to "шт"
        )
        welding.forEach { (title, unit) ->
            dao.insert("welding", title, unit, 0)
        }

        val locksmith = listOf(
            "Изготовление столешницы" to "шт",
            "Изготовление сварной мойки" to "шт",
            "Изготовление полки для блендера" to "шт",
            "Отверстие под смеситель" to "шт",
            "Изготовление кармана для бутылок" to "шт",
            "Изготовление перфорированной вставки" to "шт",
            "Изготовление сьёмных перегородок" to "шт",
            "Проклейка основания (фанера)" to "шт",


        )
        locksmith.forEach { (title, unit) ->
            dao.insert("locksmith", title, unit, 0)
        }
    }


    suspend fun updatePrice(entity: WorkPriceEntity) {
        dao.update(entity)
    }
}
