package com.example.brighthabit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityNewHabitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NewHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewHabitBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.llDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.tvDate.text = date
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.btnCreate.setOnClickListener {
            val habitName = binding.etHabitName.text.toString()
            val goal = binding.etGoal.text.toString()
            val times = binding.etTimes.text.toString()
            val targetDate = binding.tvDate.text.toString()

            if (habitName.isEmpty() || goal.isEmpty() || times.isEmpty() ||
                targetDate == "Select a target or end date") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Habit initialization when creating new habit
            val habit = Habit(
                namaHabit = habitName,
                goal = goal,
                times = times.toIntOrNull() ?: 0,
                date = targetDate,
                isDone = false,
                userId = userId,
                doneDates = mutableListOf() // Initialize with empty list
            )


            // Save to Firestore
            db.collection("habits")
                .add(habit)
                .addOnSuccessListener {
                    Toast.makeText(this, "Habit created successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to Homepage
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error creating habit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
