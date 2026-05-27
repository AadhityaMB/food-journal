package com.example.food_log.ui.restaurant

import androidx.lifecycle.ViewModel
import com.example.food_log.data.model.relations.EntryWithRestaurant
import com.example.food_log.data.repository.FoodEntryRepository
import com.example.food_log.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow

// RestaurantProfileViewModel holds all the data for the profile screen.
// It receives the restaurantId as a constructor parameter.
// Why not use SavedStateHandle? We're keeping things simple — the ID is
// passed in directly from the factory, which is beginner-friendly.
class RestaurantProfileViewModel(
    private val restaurantId: Long,
    private val restaurantRepository: RestaurantRepository,
    private val foodEntryRepository: FoodEntryRepository
) : ViewModel() {

    // Reactive stream for the restaurant's details
    // The Fragment observes this to show the restaurant name in the toolbar
    val restaurant = restaurantRepository.getAllAsFlow()
        .let { flow ->
            // We get restaurant by filtering — could also add getByIdAsFlow() to DAO
            // but this keeps changes minimal and beginner-friendly
            kotlinx.coroutines.flow.flow {
                restaurantRepository.getById(restaurantId)?.let { emit(it) }
            }
        }

    // Total number of times the user visited this restaurant
    val visitCount: Flow<Int> = foodEntryRepository.getVisitCount(restaurantId)

    // Average star rating across all visits (null if never rated)
    val averageRating: Flow<Double?> = foodEntryRepository.getAverageRating(restaurantId)

    // Total money spent at this restaurant (null if no amounts recorded)
    val totalAmountSpent: Flow<Double?> = foodEntryRepository.getTotalAmountSpent(restaurantId)

    // Full visit history — reuses the same EntryWithRestaurant structure as Timeline
    val entries: Flow<List<EntryWithRestaurant>> =
        foodEntryRepository.getEntriesForRestaurant(restaurantId)
}
