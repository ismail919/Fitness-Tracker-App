package com.example.fitnessapp

// Handles data operations for DailyActivity
class DailyActivityRepository(
    private val dailyActivityDao: DailyActivityDao
) {

    // Save or update daily activity data
    suspend fun saveOrUpdate(dailyActivity: DailyActivity) {
        dailyActivityDao.saveDailyActivity(dailyActivity)
    }

    // Recover activity for a given date
    suspend fun getDailyActivity(date: String): DailyActivity? {
        return dailyActivityDao.getDailyActivity(date)
    }

}