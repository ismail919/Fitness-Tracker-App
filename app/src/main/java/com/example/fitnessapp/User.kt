package com.example.fitnessapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: Int = 1,        // Always 1 → only one profile stored
    val age: Int,                       // User age
    val weight: Double,                 // Weight (kg)
    val height: Double,                 // Height (cm)
    val gender: String,                 // Male / Female
    val activityLevel: Double           // Activity multiplier (1.2 - 1.9)
)

