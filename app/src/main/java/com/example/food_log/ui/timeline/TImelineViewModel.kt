package com.example.food_log.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.WouldOrderAgain
import com.example.food_log.data.model.relations.EntryWithRestaurant
import com.example.food_log.data.repository.FoodEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class TimelineViewModel(
    private val repository: FoodEntryRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val filterDiningMode = MutableStateFlow<DiningMode?>(null)
    val filterPlatform = MutableStateFlow<Platform?>(null)
    val filterWouldOrderAgain = MutableStateFlow<WouldOrderAgain?>(null)
    val filterStartDate = MutableStateFlow<Long?>(null)
    val filterEndDate = MutableStateFlow<Long?>(null)

    private data class FilterState(
        val mode: DiningMode?,
        val platform: Platform?,
        val again: WouldOrderAgain?,
        val startDate: Long?,
        val endDate: Long?
    )

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    private val filteredEntriesFromDb = combine(
        filterDiningMode, filterPlatform, filterWouldOrderAgain, filterStartDate, filterEndDate
    ) { mode, platform, again, start, end ->
        FilterState(mode, platform, again, start, end)
    }.flatMapLatest { state ->
        repository.getFilteredEntries(state.mode, state.platform, state.again, state.startDate, state.endDate)
    }

    val entries: StateFlow<List<EntryWithRestaurant>> = combine(
        filteredEntriesFromDb,
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

    fun applyFilters(
        mode: DiningMode?,
        platform: Platform?,
        again: WouldOrderAgain?,
        startDate: Long?,
        endDate: Long?
    ) {
        filterDiningMode.value = mode
        filterPlatform.value = platform
        filterWouldOrderAgain.value = again
        filterStartDate.value = startDate
        filterEndDate.value = endDate
    }

    fun clearFilters() {
        filterDiningMode.value = null
        filterPlatform.value = null
        filterWouldOrderAgain.value = null
        filterStartDate.value = null
        filterEndDate.value = null
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
}