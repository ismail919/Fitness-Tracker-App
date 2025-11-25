package com.example.fitnessapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    // Step Tracking
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private lateinit var stepsText: TextView
    private lateinit var caloriesText: TextView

    // Step system
    private var totalSteps = 0f
    private var previousSteps = 0f

    // Distance
    private lateinit var distanceText: TextView
    private var strideLength = 0.75 // Metres per step ( can be replaced with user input)

    // Maintenance calories
    private lateinit var maintenanceText: TextView
    private var userAge = 20
    private var userWeight = 70.0
    private var userHeight = 175.0
    private var userActivityLevel = 1.4 // default

    // Level + XP System
    private lateinit var xpText: TextView
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


        // Request permission for physical activity
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

        // Sensor Access
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

            // Step Count
            stepsText.text = "Steps: $steps"

            // Calories (simple estimate currently)
            val calories = steps * 0.04
            caloriesText.text = "Calories Burned: ${calories.toInt()}"

            // Distance Calculation
            val distance = steps * strideLength
            distanceText.text = "Distance: ${"%.2f".format(distance)} m"

            // Maintenance Calories formula (Mifflin-St Jeor)
            val bmr = (10 * userWeight) + (6.25 * userHeight) - (5 * userAge) + 5
            val maintenance = bmr * userActivityLevel
            maintenanceText.text = "Maintenance: ${maintenance.toInt()} kcal"

            //Level + XP system
            xp += (steps / 10).toInt()
            if (xp > level * 100) {
                level++
            }
            xpText.text = "XP: $xp | Level: $level"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Required method for SensorEventListener, even if empty
    }
}
