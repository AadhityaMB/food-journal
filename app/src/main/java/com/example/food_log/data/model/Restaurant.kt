package com.example.food_log.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val address: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val createdAt: Long
)