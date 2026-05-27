package com.example.food_log.data.repository

import com.example.food_log.data.db.dao.RestaurantDao
import com.example.food_log.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

class RestaurantRepository(
    private val restaurantDao: RestaurantDao
) {

    suspend fun insert(restaurant: Restaurant): Long {
        return restaurantDao.insert(restaurant)
    }

    suspend fun getAll(): List<Restaurant> {
        return restaurantDao.getAll()
    }

    // Reactive stream — the UI observes this to show autocomplete suggestions.
    fun getAllAsFlow(): Flow<List<Restaurant>> {
        return restaurantDao.getAllAsFlow()
    }

    suspend fun getById(id: Long): Restaurant? {
        return restaurantDao.getById(id)
    }

    // Case-insensitive search for a restaurant by name.
    suspend fun findByName(name: String): Restaurant? {
        return restaurantDao.findByName(name)
    }

    // The key function: find a restaurant by name first.
    // If it already exists → return its ID.
    // If it doesn't exist → insert a new one and return the new ID.
    // This prevents duplicate restaurant rows in the database.
    suspend fun getOrCreate(name: String): Long {
        val existing = restaurantDao.findByName(name.trim())
        if (existing != null) {
            return existing.id
        }
        val newRestaurant = Restaurant(
            name = name.trim(),
            createdAt = System.currentTimeMillis()
        )
        return restaurantDao.insert(newRestaurant)
    }

    suspend fun deleteById(id: Long) {
        restaurantDao.deleteById(id)
    }
}