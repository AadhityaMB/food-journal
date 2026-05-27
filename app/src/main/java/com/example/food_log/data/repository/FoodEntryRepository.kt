package com.example.food_log.data.repository

import com.example.food_log.data.db.dao.FoodEntryDao
import com.example.food_log.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import com.example.food_log.data.model.relations.EntryWithRestaurant
class FoodEntryRepository(
    private val foodEntryDao: FoodEntryDao
) {

    suspend fun insert(entry: FoodEntry): Long {
        return foodEntryDao.insert(entry)
    }

    fun getAll(): Flow<List<FoodEntry>> {
        return foodEntryDao.getAll()
    }

    fun getAllWithRestaurant() = foodEntryDao.getAllWithRestaurant()

    suspend fun getByRestaurant(restaurantId: Long): List<FoodEntry> {
        return foodEntryDao.getByRestaurant(restaurantId)
    }

    suspend fun getById(id: Long): FoodEntry? {
        return foodEntryDao.getById(id)
    }

    suspend fun deleteById(id: Long) {
        foodEntryDao.deleteById(id)
    }

}