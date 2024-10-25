package com.example.brighthabit

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityNewHabitBinding
import java.util.*

class NewHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewHabitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set a date picker dialog
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

        // Handle create button click
        binding.btnCreate.setOnClickListener {
            val habitName = binding.etHabitName.text.toString()
            val goal = binding.etGoal.text.toString()
            val times = binding.etTimes.text.toString()
            val targetDate = binding.tvDate.text.toString()

            if (habitName.isEmpty() || goal.isEmpty() || times.isEmpty() || targetDate == "Select a target or end date") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Code to save the habit to database or other actions
                Toast.makeText(this, "Habit Created!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
