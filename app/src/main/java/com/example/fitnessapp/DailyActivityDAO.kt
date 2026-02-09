package com.example.fitnessapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyActivityDao {

    // Save or update a daily activity for a specific date
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyActivity(dailyActivity: DailyActivity)


    // Get activity data for a specific date
    @Query("SELECT * FROM daily_activity_table WHERE date = :date")
    suspend fun getDailyActivity(date: String): DailyActivity?
}
