package com.example.fitnessapp

// Handles data operations between ViewModel and DAO
class UserRepository(private val userDao: UserDao) {

    suspend fun saveUser(user: User) = userDao.saveUser(user) // Save or replace user profile

    suspend fun updateUser(user: User) = userDao.updateUser(user) // Update current user profile

    suspend fun getUser() = userDao.getUser() // Get the saved user profile
}
