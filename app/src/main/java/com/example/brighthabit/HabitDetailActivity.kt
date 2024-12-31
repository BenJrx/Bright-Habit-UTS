package com.example.brighthabit

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HabitDetailActivity : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var tvSuccessDays: TextView
    private lateinit var tvFailedDays: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var tvNote: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private lateinit var habitId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_detail)

        // Inisialisasi view
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSuccessDays = findViewById(R.id.tvSuccessDays)
        tvFailedDays = findViewById(R.id.tvFailedDays)
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
        rvCalendar = findViewById(R.id.calendarView)
        tvNote = findViewById(R.id.tvNote)
        btnBack = findViewById(R.id.btnBack)

        // Ambil habit ID dari intent
        habitId = intent.getStringExtra("HABIT_ID") ?: return

        setupCalendar()
        loadHabitDetails()

        // Aksi tombol "Kembali"
        btnBack.setOnClickListener {
            val intent = Intent(this, Homepage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupCalendar() {
        updateMonthYear()
        rvCalendar.layoutManager = GridLayoutManager(this, 7)

        // Tambahkan jarak antar item kalender
        rvCalendar.addItemDecoration(CalendarItemDecoration(16)) // 16dp padding

        rvCalendar.adapter = CalendarAdapter(currentMonth, currentYear) { selectedDate ->
            checkHabitStatus(selectedDate)
        }
    }

    private fun updateMonthYear() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)

        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = formatter.format(calendar.time)
    }

    private fun checkHabitStatus(selectedDate: String) {
        db.collection("habits").document(habitId).get().addOnSuccessListener { document ->
            val doneDates = document["doneDates"] as? List<String> ?: listOf()
            val isDone = selectedDate in doneDates

            val statusMessage = if (isDone) "Habit selesai pada hari ini." else "Habit belum selesai pada hari ini."

            AlertDialog.Builder(this)
                .setMessage(statusMessage)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun loadHabitDetails() {
        db.collection("habits").document(habitId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val doneDates = document.get("doneDates") as? List<String> ?: listOf()
                val isNotDone = document.getLong("isNotDone")?.toInt() ?: 0
                val goal = document.getString("goal") ?: "No goal set."

                // Update the streaks
                updateStreaks(doneDates, isNotDone)

                // Update tvNote with the goal
                tvNote.text = goal
            } else {
                tvNote.text = "Goal data not found."
            }
        }.addOnFailureListener { exception ->
            tvNote.text = "Failed to load goal: ${exception.message}"
        }
    }

    private fun updateStreaks(doneDates: List<String>, isNotDone: Int) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)

        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
        val lastDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, totalDaysInMonth) }.time

        // Filter dates for current month
        val validDates = doneDates.filter {
            try {
                val date = formatter.parse(it)
                date != null && date.after(firstDayOfMonth) && date.before(lastDayOfMonth)
            } catch (e: Exception) {
                false
            }
        }

        val successDays = validDates.size
        val failedDays = isNotDone

        // Calculate current streak
        var currentStreak = 0
        if (validDates.isNotEmpty()) {
            // Sort dates in ascending order
            val sortedDates = validDates.map { formatter.parse(it) }.sortedBy { it }

            // Get today's date without time
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            // Start from the most recent date
            for (i in sortedDates.indices.reversed()) {
                val currentDate = sortedDates[i]

                if (i == sortedDates.lastIndex) {
                    // First iteration, check if this is today or yesterday
                    val daysDiff = ((today.time - currentDate.time) / (24 * 60 * 60 * 1000)).toInt()
                    if (daysDiff <= 1) { // Today or yesterday
                        currentStreak = 1
                    } else {
                        break // Streak is broken
                    }
                } else {
                    // Check if the previous date is consecutive
                    val daysDiff = ((sortedDates[i + 1].time - currentDate.time) / (24 * 60 * 60 * 1000)).toInt()
                    if (daysDiff == 1) {
                        currentStreak++
                    } else {
                        break // Streak is broken
                    }
                }
            }

            // If there was any break in streak (isNotDone > 0), reset current streak
            if (failedDays > 0) {
                // Check if we started a new streak after the last break
                val lastCompletedDate = sortedDates.last()
                val daysSinceLastCompletion = ((today.time - lastCompletedDate.time) / (24 * 60 * 60 * 1000)).toInt()

                if (daysSinceLastCompletion <= 1) {
                    currentStreak = 1 // New streak started
                } else {
                    currentStreak = 0 // No active streak
                }
            }
        }

        tvSuccessDays.text = "$successDays Hari"
        tvFailedDays.text = "$failedDays Hari"
        tvCurrentStreak.text = "$currentStreak Hari"
    }

    // Inner class untuk menambahkan jarak antar item RecyclerView
    class CalendarItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.set(spacing, spacing, spacing, spacing)
        }
    }
}