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
import java.util.Calendar

class TimelineViewModel(
    private val repository: FoodEntryRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val filterDiningMode = MutableStateFlow<DiningMode?>(null)
    val filterPlatform = MutableStateFlow<Platform?>(null)
    val filterWouldOrderAgain = MutableStateFlow<WouldOrderAgain?>(null)
    val filterMonth = MutableStateFlow<Int>(-1) // 0-11 for Jan-Dec, -1 for Any
    val filterWeek = MutableStateFlow<Int>(-1)  // 1-5 for Week 1-5, -1 for Any

    private data class FilterState(
        val mode: DiningMode?,
        val platform: Platform?,
        val again: WouldOrderAgain?,
        val month: Int,
        val week: Int
    )

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    private val filteredEntriesFromDb = combine(
        filterDiningMode, filterPlatform, filterWouldOrderAgain, filterMonth, filterWeek
    ) { mode, platform, again, month, week ->
        FilterState(mode, platform, again, month, week)
    }.flatMapLatest { state ->
        val (start, end) = getStartAndEndMillis(state.month, state.week)
        repository.getFilteredEntries(state.mode, state.platform, state.again, start, end)
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

    private fun getStartAndEndMillis(month: Int, week: Int): Pair<Long?, Long?> {
        if (month == -1) return Pair(null, null)

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (week == -1) {
            val startMillis = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val endMillis = cal.timeInMillis - 1
            return Pair(startMillis, endMillis)
        } else {
            cal.set(Calendar.WEEK_OF_MONTH, week)
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val startMillis = cal.timeInMillis
            cal.add(Calendar.WEEK_OF_YEAR, 1)
            val endMillis = cal.timeInMillis - 1
            return Pair(startMillis, endMillis)
        }
    }

    fun applyFilters(
        mode: DiningMode?,
        platform: Platform?,
        again: WouldOrderAgain?,
        month: Int,
        week: Int
    ) {
        filterDiningMode.value = mode
        filterPlatform.value = platform
        filterWouldOrderAgain.value = again
        filterMonth.value = month
        filterWeek.value = week
    }

    fun clearFilters() {
        filterDiningMode.value = null
        filterPlatform.value = null
        filterWouldOrderAgain.value = null
        filterMonth.value = -1
        filterWeek.value = -1
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
}