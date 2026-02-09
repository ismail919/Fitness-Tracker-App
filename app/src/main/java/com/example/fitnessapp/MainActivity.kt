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

    private lateinit var stepsText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var distanceText: TextView
    private lateinit var maintenanceText: TextView
    private lateinit var xpText: TextView

    // ViewModel (EXISTING)
    private lateinit var viewModel: UserViewModel

    // ===================== ADD HERE (1/4) =====================
    // Daily Activity ViewModel
    private lateinit var dailyActivityViewModel: DailyActivityViewModel
    // ==========================================================

    // Default values
    private var userAge = 20
    private var userWeight = 70.0
    private var userHeight = 175.0
    private var userActivityLevel = 1.4

    // XP + Steps
    private var totalSteps = 0f
    private var previousSteps = 0f
    private var strideLength = 0.75
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

        // PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1
            )
        }

        // SENSOR
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Reload user data from Room
        viewModel.getUser { user ->
            if (user != null) {
                userAge = user.age
                userWeight = user.weight
                userHeight = user.height
                userActivityLevel = user.activityLevel
            }

            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel

            runOnUiThread {
                maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // Save today's activity into Room
    private fun saveTodayActivity(steps: Int, calories: Int, distance: Double) {

        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())

        val dailyActivity = DailyActivity(
            date = today,
            steps = steps,
            calories = calories,
            distance = distance
        )

        dailyActivityViewModel.saveDailyActivity(dailyActivity)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {

            totalSteps = event.values[0]
            val steps = totalSteps.toInt() - previousSteps.toInt()

            stepsText.text = "Steps: $steps"

            val calories = steps * 0.04
            caloriesText.text = "Calories Burned: ${calories.toInt()}"

            val distance = steps * strideLength
            distanceText.text = "Distance: %.2f m".format(distance)

            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel
            maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"

            xp += (steps / 10).toInt()
            if (xp > level * 100) level++
            xpText.text = "XP: $xp | Level: $level"


            // Persist today's activity
            saveTodayActivity(
                steps = steps,
                calories = calories.toInt(),
                distance = distance
            )

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
