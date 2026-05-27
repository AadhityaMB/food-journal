package com.example.food_log.ui.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.food_log.data.repository.FoodEntryRepository
import com.example.food_log.data.repository.RestaurantRepository

// The factory is needed because RestaurantProfileViewModel has constructor parameters
// (restaurantId, restaurantRepository, foodEntryRepository).
// Android's default ViewModelProvider doesn't know how to create ViewModels with
// custom parameters — that's what this factory is for.
class RestaurantProfileViewModelFactory(
    private val restaurantId: Long,
    private val restaurantRepository: RestaurantRepository,
    private val foodEntryRepository: FoodEntryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RestaurantProfileViewModel(
            restaurantId,
            restaurantRepository,
            foodEntryRepository
        ) as T
    }
}
