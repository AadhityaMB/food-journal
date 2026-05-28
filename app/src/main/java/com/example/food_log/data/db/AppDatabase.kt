package com.example.food_log.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.food_log.data.db.dao.FoodEntryDao
import com.example.food_log.data.db.dao.OrderedItemDao
import com.example.food_log.data.db.dao.PhotoDao
import com.example.food_log.data.db.dao.RestaurantDao
import com.example.food_log.data.model.EntryPhoto
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.OrderedItem
import com.example.food_log.data.model.Restaurant

// version = 2 because we added new columns to FoodEntry.
// Room requires the version number to change whenever the schema changes.
// exportSchema = false suppresses the "export schema" warning for now.
@Database(
    entities = [
        Restaurant::class,
        FoodEntry::class,
        OrderedItem::class,
        EntryPhoto::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun orderedItemDao(): OrderedItemDao
    abstract fun photoDao(): PhotoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_log.db"
                )
                    // During development, if the schema changes (new columns, etc.),
                    // just wipe and recreate the database instead of writing a migration.
                    // WARNING: this deletes all existing data on version change.
                    // Remove this before releasing to real users.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}