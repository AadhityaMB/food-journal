package com.example.food_log.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food_log.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    @Insert
    suspend fun insert(restaurant: Restaurant): Long

    // Returns all restaurants, sorted A→Z
    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    suspend fun getAll(): List<Restaurant>

    // Reactive version — used for autocomplete in the Add Entry form.
    // Flow automatically emits a new list whenever the restaurants table changes.
    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    fun getAllAsFlow(): Flow<List<Restaurant>>

    // Case-insensitive exact match by name — used to prevent duplicate restaurants.
    // Returns null if no restaurant with that name exists.
    @Query("SELECT * FROM restaurants WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): Restaurant?

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?

    @Query("DELETE FROM restaurants WHERE id = :id")
    suspend fun deleteById(id: Long)
}