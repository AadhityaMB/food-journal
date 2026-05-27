package com.example.food_log.data.db.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food_log.data.model.FoodEntry

@Dao
interface FoodEntryDao {

    @Insert
    suspend fun insert(foodEntry: FoodEntry): Long

    @Query("""
    SELECT * FROM food_entries
    ORDER BY date DESC
""")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<FoodEntry>>

    @Query("""
        SELECT * FROM food_entries
        WHERE restaurantId = :restaurantId
        ORDER BY date DESC
    """)
    suspend fun getByRestaurant(restaurantId: Long): List<FoodEntry>

    @Query("""
        SELECT * FROM food_entries
        WHERE id = :id
    """)
    suspend fun getById(id: Long): FoodEntry?

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}