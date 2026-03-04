package com.example.fitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var todayActivity: TextView
    private lateinit var stepsText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var distanceText: TextView
    private lateinit var maintenanceText: TextView
    private lateinit var xpText: TextView
    private lateinit var xpProgressBar: ProgressBar  // NEW

    // ViewModels
    private lateinit var viewModel: UserViewModel
    private lateinit var dailyActivityViewModel: DailyActivityViewModel

    // Default user values
    private var userAge = 20
    private var userWeight = 70.0
    private var userHeight = 175.0
    private var userActivityLevel = 1.4

    // XP + Steps
    private var totalSteps = 0f
    private var previousSteps = 0f
    private var xp = 0
    private var level = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TEXT VIEWS
        stepsText = findViewById(R.id.stepsText)
        caloriesText = findViewById(R.id.caloriesText)
        distanceText = findViewById(R.id.distanceText)
        maintenanceText = findViewById(R.id.maintenanceText)
        xpText = findViewById(R.id.xpText)
        todayActivity = findViewById(R.id.todayActivityText)
        xpProgressBar = findViewById(R.id.xpProgressBar)  // NEW

        // OPEN PROFILE BUTTON
        val profileBtn = findViewById<Button>(R.id.openProfileButton)
        profileBtn.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // ROOM DATABASE SINGLETON
        val db = AppDatabaseSingleton.getDatabase(this)

        val repository = UserRepository(db.userDao())
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        // DailyActivity ViewModel setup
        val dailyRepository = DailyActivityRepository(db.dailyActivityDao())
        dailyActivityViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return DailyActivityViewModel(dailyRepository) as T
                }
            }
        )[DailyActivityViewModel::class.java]

        // REQUEST ACTIVITY RECOGNITION PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1
            )
        }

        // SENSOR SETUP
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()

        // Register step sensor
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Reload user profile from Room and recalculate maintenance calories
        viewModel.getUser { user ->
            if (user != null) {
                userAge = user.age
                userWeight = user.weight
                userHeight = user.height
                userActivityLevel = user.activityLevel
            }

            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            maintenanceText.text = "Maintenance: ${(bmr * userActivityLevel).toInt()} kcal"
        }

        // Load today's saved activity from Room
        loadTodayActivity()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // Save today's activity to Room
    private fun saveTodayActivity(steps: Int, calories: Int, distance: Double) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyActivity = DailyActivity(
            date = today,
            steps = steps,
            calories = calories,
            distance = distance
        )
        dailyActivityViewModel.saveDailyActivity(dailyActivity)
    }

    // Load today's activity from Room and display it
    private fun loadTodayActivity() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dailyActivityViewModel.getDailyActivity(today) { dailyActivity ->
            runOnUiThread {
                todayActivity.text = if (dailyActivity != null) {
                    "Today: ${dailyActivity.steps} steps | " +
                            "${dailyActivity.calories} kcal | " +
                            String.format("%.2f m", dailyActivity.distance)
                } else {
                    "No activity saved for today"
                }
            }
        }
    }

    // Called automatically when the step sensor registers a change
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {

            // Total steps since last device reboot
            totalSteps = event.values[0]

            // Steps taken this session
            val steps = totalSteps.toInt() - previousSteps.toInt()

            // Update step display
            stepsText.text = "Steps: $steps"

            // Calculate and display calories burned
            val calories = steps * 0.04
            caloriesText.text = "Calories Burned: ${calories.toInt()}"

            // Calculate and display distance
            val strideLength = 0.75
            val distance = steps * strideLength
            distanceText.text = "Distance: %.2f m".format(distance)

            // Recalculate and display maintenance calories (Mifflin-St Jeor)
            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel
            maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"

            // Update XP and level
            previousSteps = totalSteps
            xp += (steps / 10).toInt()
            if (xp > level * 100) level++

            // Update XP text display
            xpText.text = "XP: $xp | Level: $level"

            // Update XP progress bar (progress within current level as percentage)
            val xpForCurrentLevel = xp % (level * 100)
            val progressPercent = ((xpForCurrentLevel.toFloat() / (level * 100)) * 100).toInt()
            xpProgressBar.progress = progressPercent  // NEW

            // Save today's activity to Room
            saveTodayActivity(
                steps = steps,
                calories = calories.toInt(),
                distance = distance
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}