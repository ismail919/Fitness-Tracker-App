package com.example.fitnessapp

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Creates UserViewModel and gives it to the Repository
class UserViewModelFactory (private val repository: UserRepository)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(repository) as T
    }
}