package com.example.fitnessapp

import android.content.Context
import androidx.room.Room

object AppDatabaseSingleton {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "user_database"
            )
                .fallbackToDestructiveMigration()   // optional: resets DB if schema changes
                .build()

            INSTANCE = instance
            instance
        }
    }
}
