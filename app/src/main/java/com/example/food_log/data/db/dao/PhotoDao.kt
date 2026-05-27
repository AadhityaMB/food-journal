package com.example.food_log.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.food_log.data.model.EntryPhoto

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: EntryPhoto): Long

    @Query("""
        SELECT * FROM entry_photos
        WHERE entryId = :entryId
    """)
    suspend fun getByEntry(entryId: Long): List<EntryPhoto>

    @Query("""
        DELETE FROM entry_photos
        WHERE entryId = :entryId
    """)
    suspend fun deleteByEntry(entryId: Long)
}