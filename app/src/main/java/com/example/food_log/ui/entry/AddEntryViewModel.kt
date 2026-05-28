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
import com.example.food_log.data.db.dao.PhotoDao
import com.example.food_log.data.model.EntryPhoto
import com.example.food_log.data.model.enums.PhotoType
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.UsedDecision
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
    private val orderedItemDao: OrderedItemDao,
    private val photoDao: PhotoDao
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

    // Holds the list of URIs the user selected from the Photo Picker
    private val _selectedPhotoUris = MutableStateFlow<List<String>>(emptyList())
    val selectedPhotoUris: StateFlow<List<String>> = _selectedPhotoUris.asStateFlow()

    fun addPhotoUri(uri: String) {
        _selectedPhotoUris.value = _selectedPhotoUris.value + uri
    }

    init {
        // If we're in edit mode, load the existing entry from the database
        // so the Fragment can pre-fill all the form fields.
        if (isEditMode) {
            viewModelScope.launch {
                val entry = foodEntryRepository.getById(entryId)
                val restaurant = entry?.let { restaurantRepository.getById(it.restaurantId) }
                if (entry != null) {
                    val entryWithRestaurant = EntryWithRestaurant(entry, restaurant)
                    // TODO: The DAO doesn't load photos or dishes in getById since it returns a bare FoodEntry.
                    // For a complete edit mode, we'd ideally load the photos here, but since the relation is
                    // on the Flow queries, we will just query them directly for edit mode.
                    val photos = photoDao.getByEntry(entryId)
                    _existingEntry.value = entryWithRestaurant
                    _selectedPhotoUris.value = photos.map { it.filePath }
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
        platform: Platform?,
        usedDecision: UsedDecision?,
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
                        platform = platform,
                        usedDecision = usedDecision,
                        createdAt = original?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                // Clear old dishes and photos to replace with the current list
                orderedItemDao.deleteByEntry(entryId)
                photoDao.deleteByEntry(entryId)
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
                        platform = platform,
                        usedDecision = usedDecision,
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

            // Insert all photos
            _selectedPhotoUris.value.forEach { uri ->
                photoDao.insert(
                    EntryPhoto(
                        entryId = finalEntryId,
                        filePath = uri,
                        photoType = PhotoType.FOOD,
                        createdAt = System.currentTimeMillis()
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
            photoDao.deleteByEntry(entryId)
            foodEntryRepository.deleteById(entryId)
            _navigateBack.tryEmit(Unit)
        }
    }
}