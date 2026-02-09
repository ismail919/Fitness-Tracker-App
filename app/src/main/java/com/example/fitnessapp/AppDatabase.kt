package com.example.fitnessapp

import androidx.room.Database
import androidx.room.RoomDatabase

// Holds the database and gives access to the Data Access Objects
@Database(
    entities = [
        User::class,
        DailyActivity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    // Access point for daily activity data
    abstract fun dailyActivityDao(): DailyActivityDao
}