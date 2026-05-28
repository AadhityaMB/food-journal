package com.example.food_log.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entry_photos")
data class EntryPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entryId: Long,

    val filePath: String,

    val photoType: com.example.food_log.data.model.enums.PhotoType,

    val createdAt: Long
)