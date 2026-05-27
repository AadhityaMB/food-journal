package com.example.food_log.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ordered_items")
data class OrderedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entryId: Long,

    val name: String,

    val price: Double? = null,

    val notes: String? = null
)