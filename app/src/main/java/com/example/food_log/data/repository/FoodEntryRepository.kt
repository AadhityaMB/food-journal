package com.example.food_log.data.repository

import com.example.food_log.data.db.dao.FoodEntryDao
import com.example.food_log.data.model.FoodEntry

class FoodEntryRepository(
    private val foodEntryDao: FoodEntryDao
) {

    suspend fun insert(entry: FoodEntry): Long {
        return foodEntryDao.insert(entry)
    }

    suspend fun getAll(): List<FoodEntry> {
        return foodEntryDao.getAll()
    }

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