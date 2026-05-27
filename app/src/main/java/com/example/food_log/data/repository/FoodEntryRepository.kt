package com.example.food_log.data.repository

import com.example.food_log.data.db.dao.FoodEntryDao
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.relations.EntryWithRestaurant
import kotlinx.coroutines.flow.Flow

class FoodEntryRepository(
    private val foodEntryDao: FoodEntryDao
) {

    suspend fun insert(entry: FoodEntry): Long {
        return foodEntryDao.insert(entry)
    }

    // Updates an existing entry — used when the user saves edits
    suspend fun update(entry: FoodEntry) {
        foodEntryDao.update(entry)
    }

    fun getAll(): Flow<List<FoodEntry>> {
        return foodEntryDao.getAll()
    }

    // Used by Timeline to reactively display all entries with restaurant names
    fun getAllWithRestaurant(): Flow<List<EntryWithRestaurant>> {
        return foodEntryDao.getAllWithRestaurant()
    }

    // Used by Restaurant Profile to show entries for one specific restaurant
    fun getEntriesForRestaurant(restaurantId: Long): Flow<List<EntryWithRestaurant>> {
        return foodEntryDao.getEntriesForRestaurant(restaurantId)
    }

    // Stats for Restaurant Profile
    fun getVisitCount(restaurantId: Long): Flow<Int> {
        return foodEntryDao.getVisitCount(restaurantId)
    }

    fun getAverageRating(restaurantId: Long): Flow<Double?> {
        return foodEntryDao.getAverageRating(restaurantId)
    }

    fun getTotalAmountSpent(restaurantId: Long): Flow<Double?> {
        return foodEntryDao.getTotalAmountSpent(restaurantId)
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