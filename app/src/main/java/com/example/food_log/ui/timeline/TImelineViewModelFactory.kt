package com.example.food_log.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.food_log.data.repository.FoodEntryRepository

class TimelineViewModelFactory(
    private val repository: FoodEntryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimelineViewModel(repository) as T
    }
}