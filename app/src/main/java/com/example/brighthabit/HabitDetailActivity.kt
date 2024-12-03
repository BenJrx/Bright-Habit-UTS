package com.example.brighthabit

import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HabitDetailActivity : AppCompatActivity() {

    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvSuccessDays: TextView
    private lateinit var tvFailedDays: TextView
    private lateinit var tvNote: TextView
    private lateinit var calendarView: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_detail)

        // Inisialisasi View
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
        tvSuccessDays = findViewById(R.id.tvSuccessDays)
        tvFailedDays = findViewById(R.id.tvFailedDays)
        tvNote = findViewById(R.id.tvNote)
        val calendarView: GridLayout = findViewById(R.id.calendarView)


        // Ambil ID Habit dari Intent
        val habitId = intent.getStringExtra("HABIT_ID")
        if (habitId.isNullOrEmpty()) {
            Toast.makeText(this, "ID Habit tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load Detail Habit dari Firestore
        loadHabitDetails(habitId)
    }

    private fun loadHabitDetails(habitId: String) {
        db.collection("habits").document(habitId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Validasi dan ambil data dari Firestore
                val doneDates = try {
                    document.get("doneDates") as? List<String> ?: listOf()
                } catch (e: Exception) {
                    e.printStackTrace()
                    listOf()
                }
                val goal = document.getString("goal") ?: "Tidak ada catatan"

                // Tampilkan data pada UI
                tvNote.text = goal
                updateStreaks(doneDates)
            } else {
                Toast.makeText(this, "Dokumen tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
            Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStreaks(doneDates: List<String>) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = formatter.format(Date())

        // Validasi format tanggal dalam doneDates
        val validDates = doneDates.filter { date ->
            try {
                formatter.parse(date) != null
            } catch (e: Exception) {
                false
            }
        }

        // Hitung statistik
        val successDays = validDates.count()
        val totalDaysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val failedDays = totalDaysInMonth - successDays

        // Tampilkan statistik pada UI
        tvCurrentStreak.text = "$successDays Hari"
        tvSuccessDays.text = "$successDays Hari"
        tvFailedDays.text = "$failedDays Hari"
    }
}
