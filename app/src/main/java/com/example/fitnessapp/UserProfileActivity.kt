package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlin.math.abs

class UserProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel

    private val activityLabels = listOf(
        "Sedentary (little or no exercise)",
        "Lightly active (1-3 days a week)",
        "Moderately active (3-5 days a week)",
        "Very active (6-7 days a week)",
        "Extra active (hard training or physical job)"
    )
    private val activityValues = listOf(1.2, 1.375, 1.55, 1.725, 1.9)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // SINGLETON ROOM DATABASE + VIEWMODEL
        val db = AppDatabaseSingleton.getDatabase(this)
        val repository = UserRepository(db.userDao())
        val factory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        // FORM INPUTS
        val ageInput = findViewById<EditText>(R.id.inputAge)
        val weightInput = findViewById<EditText>(R.id.inputWeight)
        val heightInput = findViewById<EditText>(R.id.inputHeight)
        val genderInput = findViewById<EditText>(R.id.inputGender)
        val activitySpinner = findViewById<Spinner>(R.id.inputActivity)

        val adapter = ArrayAdapter(this, R.layout.spinner_item, activityLabels)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        activitySpinner.adapter = adapter

        val saveBtn = findViewById<Button>(R.id.saveButton)
        val backBtn = findViewById<Button>(R.id.backButton)

        // LOAD USER DATA INTO FORM
        viewModel.getUser { user ->
            if (user != null) {
                runOnUiThread {
                    ageInput.setText(user.age.toString())
                    weightInput.setText(user.weight.toString())
                    heightInput.setText(user.height.toString())
                    genderInput.setText(user.gender)

                    // pick the option closest to the saved value
                    val closest = activityValues.indices.minByOrNull {
                        abs(activityValues[it] - user.activityLevel)
                    } ?: 0
                    activitySpinner.setSelection(closest)
                }
            }
        }

        // BACK BUTTON
        backBtn.setOnClickListener { finish() }

        // SAVE BUTTON
        saveBtn.setOnClickListener {

            val age = ageInput.text.toString().toIntOrNull()
            val weight = weightInput.text.toString().toDoubleOrNull()
            val height = heightInput.text.toString().toDoubleOrNull()
            val gender = genderInput.text.toString()
            val activityLevel = activityValues[activitySpinner.selectedItemPosition]

            if (age != null && weight != null && height != null) {

                val user = User(
                    id = 1,
                    age = age,
                    weight = weight,
                    height = height,
                    gender = gender,
                    activityLevel = activityLevel
                )

                viewModel.saveUser(user)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please fill in age, weight and height", Toast.LENGTH_SHORT).show()
            }
        }
    }
}