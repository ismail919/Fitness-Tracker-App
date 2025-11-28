package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room

class UserProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)


        // SETUP ROOM DATABASE + VIEWMODEL
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "user_database"
        ).build()

        val userDao = db.userDao()
        val repository = UserRepository(userDao)
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


        //  BACK BUTTON
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

                // Save into Room DB
                viewModel.saveUser(user)

                // Go back to main screen
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
