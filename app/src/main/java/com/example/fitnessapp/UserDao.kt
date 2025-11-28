package com.example.fitnessapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO = Data Access Object
 * This interface tells Room HOW to interact with the user_table.
 * (Insert, Update, Get user data)
 */

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)                      // Insert/replace the existing user

    @Update
    suspend fun updateUser(user: User)            // Update profile

    @Query("SELECT * FROM user_table WHERE id = 1")
    suspend fun getUser(): User?                            // Get the single stored user
}