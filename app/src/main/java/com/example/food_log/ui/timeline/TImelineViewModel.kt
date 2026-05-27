package com.example.food_log.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_log.data.repository.FoodEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TimelineViewModel(
    private val repository: FoodEntryRepository
) : ViewModel() {

    // Holds the current text typed in the search bar
    val searchQuery = MutableStateFlow("")

    val entries = combine(
        repository.getAllWithRestaurant(),
        searchQuery
    ) { entries, query ->
        if (query.isBlank()) {
            entries
        } else {
            val lowercaseQuery = query.lowercase()
            entries.filter { entry ->
                val matchesRestaurant = entry.restaurant?.name?.lowercase()?.contains(lowercaseQuery) == true
                val matchesReview = entry.foodEntry.review?.lowercase()?.contains(lowercaseQuery) == true
                val matchesDishes = entry.orderedItems.any { it.name.lowercase().contains(lowercaseQuery) }
                
                matchesRestaurant || matchesReview || matchesDishes
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
}