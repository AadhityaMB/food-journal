package com.example.food_log.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.WouldOrderAgain
import com.example.food_log.data.model.relations.EntryWithRestaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Insert
    suspend fun insert(foodEntry: FoodEntry): Long

    // Overwrites an existing row with the same primary key (id).
    // Used when the user edits and saves an existing entry.
    @Update
    suspend fun update(foodEntry: FoodEntry)

    @Query("""
    SELECT * FROM food_entries
    ORDER BY date DESC
""")
    fun getAll(): Flow<List<FoodEntry>>

    // Fetch all entries with their restaurant in a single JOIN query.
    // @Transaction ensures Room fetches the relation in one atomic operation.
    @Transaction
    @Query("""
    SELECT * FROM food_entries
    ORDER BY date DESC
""")
    fun getAllWithRestaurant(): Flow<List<EntryWithRestaurant>>

    @Transaction
    @Query("""
    SELECT * FROM food_entries
    WHERE (:diningMode IS NULL OR diningMode = :diningMode)
      AND (:platform IS NULL OR platform = :platform)
      AND (:wouldOrderAgain IS NULL OR wouldOrderAgain = :wouldOrderAgain)
      AND (:startDate IS NULL OR date >= :startDate)
      AND (:endDate IS NULL OR date <= :endDate)
    ORDER BY date DESC
    """)
    fun getFilteredEntries(
        diningMode: DiningMode?,
        platform: Platform?,
        wouldOrderAgain: WouldOrderAgain?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<EntryWithRestaurant>>

    // All entries for a specific restaurant, newest first.
    // Used in the Restaurant Profile screen.
    @Transaction
    @Query("""
    SELECT * FROM food_entries
    WHERE restaurantId = :restaurantId
    ORDER BY date DESC
""")
    fun getEntriesForRestaurant(restaurantId: Long): Flow<List<EntryWithRestaurant>>

    // Total number of visits to a restaurant
    @Query("SELECT COUNT(*) FROM food_entries WHERE restaurantId = :restaurantId")
    fun getVisitCount(restaurantId: Long): Flow<Int>

    // Average star rating for a restaurant — returns null if no ratings exist
    @Query("SELECT AVG(rating) FROM food_entries WHERE restaurantId = :restaurantId AND rating IS NOT NULL")
    fun getAverageRating(restaurantId: Long): Flow<Double?>

    // Total amount spent at a restaurant
    @Query("SELECT SUM(amountSpent) FROM food_entries WHERE restaurantId = :restaurantId AND amountSpent IS NOT NULL")
    fun getTotalAmountSpent(restaurantId: Long): Flow<Double?>

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