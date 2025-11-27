package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val ageInput = findViewById<EditText>(R.id.inputAge)
        val weightInput = findViewById<EditText>(R.id.inputWeight)
        val heightInput = findViewById<EditText>(R.id.inputHeight)
        val genderInput = findViewById<EditText>(R.id.inputGender)
        val activityInput = findViewById<EditText>(R.id.inputActivity)

        val saveBtn = findViewById<Button>(R.id.saveButton)
        val backBtn = findViewById<Button>(R.id.backButton)

        // 🔙 BACK BUTTON (go back without saving)
        backBtn.setOnClickListener {
            finish() // closes this screen and returns to MainActivity
        }

        // 💾 SAVE BUTTON
        saveBtn.setOnClickListener {

            val age = ageInput.text.toString().toIntOrNull()
            val weight = weightInput.text.toString().toDoubleOrNull()
            val height = heightInput.text.toString().toDoubleOrNull()
            val gender = genderInput.text.toString()
            val activityLevel = activityInput.text.toString().toDoubleOrNull()

            if (age != null && weight != null && height != null && activityLevel != null) {

                // Later we save to Room — but for now just return to main screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
