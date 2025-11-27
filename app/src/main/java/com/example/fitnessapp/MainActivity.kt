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

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var stepsText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var distanceText: TextView
    private lateinit var maintenanceText: TextView
    private lateinit var xpText: TextView

    private var totalSteps = 0f
    private var previousSteps = 0f

    private var strideLength = 0.75 // metres per step

    // Maintenance calorie inputs
    private var userAge = 20
    private var userWeight = 70.0
    private var userHeight = 175.0
    private var userActivityLevel = 1.4 // default

    // XP + Level
    private var xp = 0
    private var level = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stepsText = findViewById(R.id.stepsText)
        caloriesText = findViewById(R.id.caloriesText)
        distanceText = findViewById(R.id.distanceText)
        maintenanceText = findViewById(R.id.maintenanceText)
        xpText = findViewById(R.id.xpText)


        // Button
        val profileBtn = findViewById<Button>(R.id.openProfileButton)
        profileBtn.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Request activity recognition permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1
            )
        }

        // Sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {

            totalSteps = event.values[0]
            val steps = totalSteps.toInt() - previousSteps.toInt()

            // Step count
            stepsText.text = "Steps: $steps"

            // Calories burned estimate
            val calories = steps * 0.04
            caloriesText.text = "Calories Burned: ${calories.toInt()}"

            // Distance walked
            val distance = steps * strideLength
            distanceText.text = "Distance: ${"%.2f".format(distance)} m"

            // Maintenance calories calculation
            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel
            maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"

            // XP + Level System
            xp += (steps / 10).toInt()
            if (xp > level * 100) {
                level++
            }
            xpText.text = "XP: $xp | Level: $level"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
