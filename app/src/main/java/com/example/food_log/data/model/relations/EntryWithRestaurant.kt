package com.example.food_log.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.Restaurant

// This class represents the result of a JOIN between food_entries and restaurants.
// @Embedded tells Room to treat FoodEntry's columns as part of this class.
// @Relation tells Room to automatically fetch the matching Restaurant using restaurantId.
// restaurant is nullable (Restaurant?) because it's safer — if a restaurant was somehow
// deleted without removing its entries, the app won't crash.
data class EntryWithRestaurant(

    @Embedded
    val foodEntry: FoodEntry,

    @Relation(
        parentColumn = "restaurantId",
        entityColumn = "id"
    )
    val restaurant: Restaurant?,  // nullable: won't crash if restaurant is missing

    // Loads all ordered items for this specific food entry automatically
    @Relation(
        parentColumn = "id",         // id from food_entries
        entityColumn = "entryId"     // entryId from ordered_items
    )
    val orderedItems: List<com.example.food_log.data.model.OrderedItem> = emptyList()
)