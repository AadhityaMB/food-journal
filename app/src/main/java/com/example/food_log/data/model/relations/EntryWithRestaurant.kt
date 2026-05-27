package com.example.food_log.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.Restaurant

data class EntryWithRestaurant(

    @Embedded
    val foodEntry: FoodEntry,

    @Relation(
        parentColumn = "restaurantId",
        entityColumn = "id"
    )
    val restaurant: Restaurant
)