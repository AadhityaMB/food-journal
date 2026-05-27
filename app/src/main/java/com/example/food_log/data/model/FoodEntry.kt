package com.example.food_log.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val restaurantId: Long,

    val date: Long,

    val amountSpent: Double? = null,

    val rating: Int? = null,

    val review: String? = null,

    val createdAt: Long,

    val updatedAt: Long
)