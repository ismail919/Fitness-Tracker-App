package com.example.fitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var xpProgressBar: ProgressBar

    private lateinit var stepGoalText: TextView
    private lateinit var stepGoalProgressBar: ProgressBar
    private lateinit var nextLevelText: TextView
    private lateinit var badgeText: TextView

    private lateinit var strainText: TextView
    private lateinit var strainProgressBar: ProgressBar

    private lateinit var recoveryText: TextView
    private lateinit var recoveryProgressBar: ProgressBar
    private lateinit var recoveryHint: TextView
    private lateinit var sleepInput: EditText
    private lateinit var feelingSpinner: Spinner
    private lateinit var saveCheckInButton: Button

    private val feelingLabels = listOf(
        "1 - Terrible",
        "2 - Poor",
        "3 - Okay",
        "4 - Good",
        "5 - Great"
    )

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

    private val dailyStepGoal = 8000

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
        xpProgressBar = findViewById(R.id.xpProgressBar)

        stepGoalText = findViewById(R.id.stepGoalText)
        stepGoalProgressBar = findViewById(R.id.stepGoalProgressBar)
        nextLevelText = findViewById(R.id.nextLevelText)
        badgeText = findViewById(R.id.badgeText)

        strainText = findViewById(R.id.strainText)
        strainProgressBar = findViewById(R.id.strainProgressBar)

        recoveryText = findViewById(R.id.recoveryText)
        recoveryProgressBar = findViewById(R.id.recoveryProgressBar)
        recoveryHint = findViewById(R.id.recoveryHint)
        sleepInput = findViewById(R.id.sleepInput)
        feelingSpinner = findViewById(R.id.feelingSpinner)
        saveCheckInButton = findViewById(R.id.saveCheckInButton)

        // FEELING DROPDOWN
        val feelingAdapter = ArrayAdapter(this, R.layout.spinner_item, feelingLabels)
        feelingAdapter.setDropDownViewResource(R.layout.spinner_item)
        feelingSpinner.adapter = feelingAdapter
        feelingSpinner.setSelection(2)

        // SAVE CHECK-IN BUTTON
        saveCheckInButton.setOnClickListener {
            val sleep = sleepInput.text.toString().toDoubleOrNull()
            if (sleep == null || sleep < 0 || sleep > 24) {
                Toast.makeText(this, "Enter hours slept, between 0 and 24", Toast.LENGTH_SHORT).show()
            } else {
                val feeling = feelingSpinner.selectedItemPosition + 1
                getSharedPreferences("checkin", MODE_PRIVATE).edit()
                    .putString("date", todayString())
                    .putFloat("sleep", sleep.toFloat())
                    .putInt("feeling", feeling)
                    .apply()
                showRecovery(sleep, feeling)
            }
        }

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

        // Load today's check-in, if there is one
        loadCheckIn()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Shows the strain score for a given number of steps
    private fun showStrain(steps: Int) {
        val strain = Scores.strain(steps, dailyStepGoal)
        strainText.text = "$strain / 100"
        strainProgressBar.progress = strain
    }

    // Shows the recovery score for a sleep + feeling check-in
    private fun showRecovery(sleepHours: Double, feeling: Int) {
        val recovery = Scores.recovery(sleepHours, feeling)
        recoveryText.text = "$recovery / 100"
        recoveryProgressBar.progress = recovery
        recoveryHint.text = "Based on $sleepHours h sleep and feeling ${feelingLabels[feeling - 1].substring(4).lowercase()}"
    }

    // Loads today's check-in from storage, if it was saved today
    private fun loadCheckIn() {
        val prefs = getSharedPreferences("checkin", MODE_PRIVATE)
        if (prefs.getString("date", "") == todayString()) {
            val sleep = prefs.getFloat("sleep", 0f).toDouble()
            val feeling = prefs.getInt("feeling", 3)
            sleepInput.setText(sleep.toString())
            feelingSpinner.setSelection(feeling - 1)
            showRecovery(sleep, feeling)
        } else {
            recoveryText.text = "-- / 100"
            recoveryProgressBar.progress = 0
            recoveryHint.text = "Do your check-in below to get today's score"
        }
    }

    // Save today's activity to Room
    private fun saveTodayActivity(steps: Int, calories: Int, distance: Double) {
        val dailyActivity = DailyActivity(
            date = todayString(),
            steps = steps,
            calories = calories,
            distance = distance
        )
        dailyActivityViewModel.saveDailyActivity(dailyActivity)
    }

    // Load today's activity from Room and display it
    private fun loadTodayActivity() {
        dailyActivityViewModel.getDailyActivity(todayString()) { dailyActivity ->
            runOnUiThread {
                if (dailyActivity != null) {
                    todayActivity.text = "Today: ${dailyActivity.steps} steps | " +
                            "${dailyActivity.calories} kcal | " +
                            String.format("%.2f m", dailyActivity.distance)

                    stepsText.text = "${dailyActivity.steps}"
                    caloriesText.text = "${dailyActivity.calories} kcal"
                    distanceText.text = "%.2f m".format(dailyActivity.distance)

                    val stepGoalProgress =
                        ((dailyActivity.steps.toFloat() / dailyStepGoal) * 100).toInt().coerceAtMost(100)
                    stepGoalText.text = "Goal: ${dailyActivity.steps} / $dailyStepGoal"
                    stepGoalProgressBar.progress = stepGoalProgress

                    showStrain(dailyActivity.steps)
                } else {
                    todayActivity.text = "No activity saved for today"
                    stepsText.text = "0"
                    caloriesText.text = "0 kcal"
                    distanceText.text = "0.00 m"
                    stepGoalText.text = "Goal: 0 / $dailyStepGoal"
                    stepGoalProgressBar.progress = 0

                    showStrain(0)
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
            stepsText.text = "$steps"

            // Update daily goal progress
            val stepGoalProgress = ((steps.toFloat() / dailyStepGoal) * 100).toInt().coerceAtMost(100)
            stepGoalText.text = "Goal: $steps / $dailyStepGoal"
            stepGoalProgressBar.progress = stepGoalProgress

            // Update strain score
            showStrain(steps)

            // Calculate and display calories burned
            val calories = steps * 0.04
            caloriesText.text = "${calories.toInt()} kcal"

            // Calculate and display distance
            val strideLength = 0.75
            val distance = steps * strideLength
            distanceText.text = "%.2f m".format(distance)

            // Recalculate and display maintenance calories (Mifflin-St Jeor)
            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel
            maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"

            // Update XP and level
            previousSteps = totalSteps
            xp += (steps / 10).toInt()
            if (xp >= level * 100) level++

            // Update XP text display
            xpText.text = "XP: $xp | Level: $level"

            // XP progress calculation
            val previousLevelThreshold = (level - 1) * 100
            val currentLevelThreshold = level * 100
            val xpForCurrentLevel = xp - previousLevelThreshold
            val xpNeededForLevel = currentLevelThreshold - previousLevelThreshold
            val progressPercent = ((xpForCurrentLevel.toFloat() / xpNeededForLevel) * 100)
                .toInt().coerceIn(0, 100)

            xpProgressBar.progress = progressPercent

            // Next level text
            val xpNeeded = currentLevelThreshold - xp
            nextLevelText.text = "$xpNeeded XP to next level"

            // Badge system
            badgeText.text = when {
                steps >= 10000 -> "Badge: Goal Crusher"
                steps >= 8000 -> "Badge: Daily Goal Achieved"
                steps >= 5000 -> "Badge: Active Day"
                steps >= 1000 -> "Badge: Getting Started"
                else -> "Badge: No badge yet"
            }

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