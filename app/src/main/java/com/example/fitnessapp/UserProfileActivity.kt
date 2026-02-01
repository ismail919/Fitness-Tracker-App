package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider

class UserProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel

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
        val activityInput = findViewById<EditText>(R.id.inputActivity)

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
                    activityInput.setText(user.activityLevel.toString())
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
            val activityLevel = activityInput.text.toString().toDoubleOrNull()

            if (age != null && weight != null && height != null && activityLevel != null) {

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
            }
        }
    }
}
