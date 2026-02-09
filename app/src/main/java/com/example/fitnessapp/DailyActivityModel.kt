package com.example.fitnessapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel for handling daily activity logic
class DailyActivityViewModel(
    private val repository: DailyActivityRepository
) : ViewModel() {

    // Save daily activity data without blocking UI
    fun saveDailyActivity(dailyActivity: DailyActivity) {
        viewModelScope.launch {
            repository.saveOrUpdate(dailyActivity)
        }
    }


    // Retrieve daily activity data for a specific date
    fun getDailyActivity(date : String, onResult: (DailyActivity?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getDailyActivity(date)
            onResult(result)
        }
    }



}










