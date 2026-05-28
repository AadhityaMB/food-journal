package com.example.food_log.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.food_log.data.db.dao.OrderedItemDao
import com.example.food_log.data.repository.FoodEntryRepository
import com.example.food_log.data.repository.RestaurantRepository

import com.example.food_log.data.db.dao.PhotoDao

// entryId = -1L for a new entry, actual ID when editing.
class AddEntryViewModelFactory(
    private val entryId: Long,
    private val restaurantRepository: RestaurantRepository,
    private val foodEntryRepository: FoodEntryRepository,
    private val orderedItemDao: OrderedItemDao,
    private val photoDao: PhotoDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddEntryViewModel(
            entryId,
            restaurantRepository,
            foodEntryRepository,
            orderedItemDao,
            photoDao
        ) as T
    }
}