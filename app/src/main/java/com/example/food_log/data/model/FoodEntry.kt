package com.example.food_log.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.WouldOrderAgain

// @Entity marks this class as a Room database table called "food_entries".
// Each field becomes a column. Nullable fields (?) store NULL when not provided.
@Entity(tableName = "food_entries")
data class FoodEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Foreign key linking to the restaurants table
    val restaurantId: Long,

    // Unix timestamp (milliseconds) for when the meal happened
    val date: Long,

    // How much was spent, e.g. 350.0 for ₹350
    val amountSpent: Double? = null,

    // Rating out of 5
    val rating: Int? = null,

    // Written review or notes
    val review: String? = null,

    // Was it dine-in, delivery, or takeaway?
    // Stored as a String in the DB (Room uses enum name by default)
    val diningMode: DiningMode? = null,

    // Would the user order from here again?
    val wouldOrderAgain: WouldOrderAgain? = null,

    // Which platform was used — Swiggy, Zomato, Dine-in, etc.
    val platform: Platform? = null,

    val createdAt: Long,
    val updatedAt: Long
)