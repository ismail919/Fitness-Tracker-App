package com.example.fitnessapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Connects UI to database logic
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // Save profile to Database
    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    // Get saved profile from Database
    fun getUser(onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUser()
            onResult(user)

        }
    }

}