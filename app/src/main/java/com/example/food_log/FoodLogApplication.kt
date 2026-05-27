package com.example.food_log

import android.app.Application
import com.example.food_log.data.db.AppDatabase
import com.example.food_log.data.repository.FoodEntryRepository
import com.example.food_log.data.repository.RestaurantRepository

class FoodLogApplication : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val restaurantRepository by lazy {
        RestaurantRepository(database.restaurantDao())
    }

    val foodEntryRepository by lazy {
        FoodEntryRepository(database.foodEntryDao())
    }
}