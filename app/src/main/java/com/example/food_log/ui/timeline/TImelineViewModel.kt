package com.example.food_log.ui.timeline

import androidx.lifecycle.ViewModel
import com.example.food_log.data.repository.FoodEntryRepository

class TimelineViewModel(
    private val repository: FoodEntryRepository
) : ViewModel() {

    val entries = repository.getAllWithRestaurant()
}