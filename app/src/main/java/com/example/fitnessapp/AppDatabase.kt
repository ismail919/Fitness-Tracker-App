package com.example.fitnessapp

import androidx.room.Database
import androidx.room.RoomDatabase

// Hold the database + gives access to the DAO
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
}