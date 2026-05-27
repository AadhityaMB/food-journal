package com.example.food_log.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food_log.data.model.OrderedItem

@Dao
interface OrderedItemDao {

    @Insert
    suspend fun insert(item: OrderedItem): Long

    @Query("""
        SELECT * FROM ordered_items
        WHERE entryId = :entryId
    """)
    suspend fun getByEntry(entryId: Long): List<OrderedItem>

    @Query("""
        DELETE FROM ordered_items
        WHERE entryId = :entryId
    """)
    suspend fun deleteByEntry(entryId: Long)
}