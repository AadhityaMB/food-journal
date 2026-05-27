package com.example.food_log.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food_log.data.model.Restaurant

@Dao
interface RestaurantDao {

    @Insert
    suspend fun insert(restaurant: Restaurant): Long

    @Query("SELECT * FROM restaurants ORDER BY name ASC")
    suspend fun getAll(): List<Restaurant>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?

    @Query("DELETE FROM restaurants WHERE id = :id")
    suspend fun deleteById(id: Long)
}