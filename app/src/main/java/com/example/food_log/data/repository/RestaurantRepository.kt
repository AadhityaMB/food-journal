package com.example.food_log.data.repository

import com.example.food_log.data.db.dao.RestaurantDao
import com.example.food_log.data.model.Restaurant

class RestaurantRepository(
    private val restaurantDao: RestaurantDao
) {

    suspend fun insert(restaurant: Restaurant): Long {
        return restaurantDao.insert(restaurant)
    }

    suspend fun getAll(): List<Restaurant> {
        return restaurantDao.getAll()
    }

    suspend fun getById(id: Long): Restaurant? {
        return restaurantDao.getById(id)
    }

    suspend fun deleteById(id: Long) {
        restaurantDao.deleteById(id)
    }
}