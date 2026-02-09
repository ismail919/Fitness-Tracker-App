package com.example.fitnessapp

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores one row of activity data
@Entity(tableName = "daily_activity_table")
data class DailyActivity(

    // Date used as Unique ID (one row per day)
    @PrimaryKey
    val date: String,

    // Total steps recorded for the entire day
    val steps: Int,

    // Estimated calories burned for the entire day
    val calories: Int,

    // Estimated distance traveled for the entire day
    val distance: Double,
)