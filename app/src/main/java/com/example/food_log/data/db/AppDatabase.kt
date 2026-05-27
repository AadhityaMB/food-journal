package com.example.food_log.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.food_log.data.db.dao.FoodEntryDao
import com.example.food_log.data.db.dao.OrderedItemDao
import com.example.food_log.data.db.dao.PhotoDao
import com.example.food_log.data.db.dao.RestaurantDao
import com.example.food_log.data.model.EntryPhoto
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.OrderedItem
import com.example.food_log.data.model.Restaurant

@Database(
    entities = [
        Restaurant::class,
        FoodEntry::class,
        OrderedItem::class,
        EntryPhoto::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun orderedItemDao(): OrderedItemDao
    abstract fun photoDao(): PhotoDao
}