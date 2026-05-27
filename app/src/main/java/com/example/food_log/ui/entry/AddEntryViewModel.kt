package com.example.food_log.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.OrderedItem
import com.example.food_log.data.model.Restaurant
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.WouldOrderAgain
import com.example.food_log.data.model.relations.EntryWithRestaurant
import com.example.food_log.data.repository.FoodEntryRepository
import com.example.food_log.data.repository.RestaurantRepository
import com.example.food_log.data.db.dao.OrderedItemDao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val entryId: Long,          // -1L = new entry, anything else = editing
    private val restaurantRepository: RestaurantRepository,
    private val foodEntryRepository: FoodEntryRepository,
    private val orderedItemDao: OrderedItemDao
) : ViewModel() {

    // True when we opened this screen to EDIT an existing entry.
    // False when we're creating a brand new entry.
    val isEditMode: Boolean = entryId != -1L

    // All restaurants from the DB — used to power the autocomplete dropdown.
    val restaurantSuggestions: StateFlow<List<Restaurant>> =
        restaurantRepository.getAllAsFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // In edit mode, this holds the existing entry data so the Fragment can
    // pre-fill all the form fields. Null until loaded from the database.
    private val _existingEntry = MutableStateFlow<EntryWithRestaurant?>(null)
    val existingEntry: StateFlow<EntryWithRestaurant?> = _existingEntry.asStateFlow()

    // A "one-shot event" flow — the Fragment observes this and navigates back
    // when it fires. Using SharedFlow (not StateFlow) so the event fires exactly
    // once and doesn't replay on screen rotation.
    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        // If we're in edit mode, load the existing entry from the database
        // so the Fragment can pre-fill all the form fields.
        if (isEditMode) {
            viewModelScope.launch {
                val entry = foodEntryRepository.getById(entryId)
                val restaurant = entry?.let { restaurantRepository.getById(it.restaurantId) }
                if (entry != null) {
                    _existingEntry.value = EntryWithRestaurant(entry, restaurant)
                }
            }
        }
    }

    // Called when the user taps Save.
    // In add mode → INSERT a new row.
    // In edit mode → UPDATE the existing row (preserving its id and createdAt).
    fun saveEntry(
        restaurantName: String,
        review: String,
        amountSpent: Double?,
        rating: Int?,
        diningMode: DiningMode?,
        wouldOrderAgain: WouldOrderAgain?,
        date: Long,
        dishes: List<Pair<String, Double?>> // Passed from Fragment as name/price pairs
    ) {
        viewModelScope.launch {
            val restaurantId = restaurantRepository.getOrCreate(restaurantName)
            val finalEntryId: Long

            if (isEditMode) {
                finalEntryId = entryId
                val original = _existingEntry.value?.foodEntry
                foodEntryRepository.update(
                    FoodEntry(
                        id = entryId,
                        restaurantId = restaurantId,
                        date = date,
                        amountSpent = amountSpent,
                        rating = rating,
                        review = review.trim().takeIf { it.isNotBlank() },
                        diningMode = diningMode,
                        wouldOrderAgain = wouldOrderAgain,
                        createdAt = original?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                // Clear old dishes to replace with the current list
                orderedItemDao.deleteByEntry(entryId)
            } else {
                finalEntryId = foodEntryRepository.insert(
                    FoodEntry(
                        restaurantId = restaurantId,
                        date = date,
                        amountSpent = amountSpent,
                        rating = rating,
                        review = review.trim().takeIf { it.isNotBlank() },
                        diningMode = diningMode,
                        wouldOrderAgain = wouldOrderAgain,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            // Insert all the dishes into the database linked to this entry
            dishes.forEach { (name, price) ->
                orderedItemDao.insert(
                    OrderedItem(
                        entryId = finalEntryId,
                        name = name.trim(),
                        price = price
                    )
                )
            }

            _navigateBack.tryEmit(Unit)
        }
    }

    // Called when the user taps Delete Entry (only visible in edit mode).
    fun deleteEntry() {
        viewModelScope.launch {
            // Room does not have CASCADE delete set up, so delete children first
            orderedItemDao.deleteByEntry(entryId)
            foodEntryRepository.deleteById(entryId)
            _navigateBack.tryEmit(Unit)
        }
    }
}