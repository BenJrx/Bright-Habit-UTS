package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brighthabit.databinding.ActivityHomepageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class Homepage : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()
    private val weekDates = mutableListOf<String>()

    private lateinit var profileImage: CircleImageView
    private lateinit var greetingText: TextView
    private lateinit var usernameText: TextView
    private lateinit var taglineText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImage = binding.profileImage
        greetingText = binding.greetingText
        usernameText = binding.username
        taglineText = binding.tagline

        resetHabitsIfNewDay()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        habitAdapter = HabitAdapter(habits)
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(this@Homepage)
            adapter = habitAdapter
        }

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position in habits.indices) {
                    val habitToDelete = habits[position]
                    val removedHabit = habits.removeAt(position)
                    habitAdapter.notifyItemRemoved(position)

                    db.collection("habits").document(habitToDelete.id)
                        .delete()
                        .addOnSuccessListener {
                            showToast("Habit deleted successfully")
                        }
                        .addOnFailureListener { e ->
                            showToast("Error deleting habit: ${e.message}")
                            habits.add(position, removedHabit)
                            habitAdapter.notifyItemInserted(position)
                        }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHabits)

        loadUserData()
        loadHabits()
        setWeekDates()

        binding.btnAddHabit.setOnClickListener {
            val intent = Intent(this, NewHabitActivity::class.java)
            startActivity(intent)
        }

        binding.profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.forumIcon.setOnClickListener {
            val intent = Intent(this, ForumActivity::class.java)
            startActivity(intent)
        }
    }

    private fun resetHabitsIfNewDay() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val lastDateChecked = sharedPreferences.getString("lastDateChecked", "")
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        if (lastDateChecked != currentDate) {
            val userId = auth.currentUser?.uid ?: return
            val previousDate = getPreviousDate(lastDateChecked)

            db.collection("habits")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()

                    snapshot.documents.forEach { document ->
                        val doneDates = document.get("doneDates") as? List<String> ?: listOf()

                        val hadStreak = checkForActiveStreak(doneDates, previousDate)

                        if (hadStreak && !doneDates.contains(previousDate)) {
                            val currentNotDone = document.getLong("isNotDone")?.toInt() ?: 0
                            batch.update(
                                db.collection("habits").document(document.id),
                                "isNotDone", currentNotDone + 1
                            )
                        }
                    }

                    batch.commit().addOnSuccessListener {
                        Log.d("Homepage", "Updated isNotDone counts for broken streaks")
                        sharedPreferences.edit().putString("lastDateChecked", currentDate).apply()
                    }.addOnFailureListener { e ->
                        Log.e("Homepage", "Error updating isNotDone counts: ${e.message}")
                    }
                }
        }
    }

    private fun checkForActiveStreak(doneDates: List<String>, previousDate: String): Boolean {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            calendar.time = formatter.parse(previousDate) ?: return false

            var consecutiveDays = 0

            for (i in 1..2) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val checkDate = formatter.format(calendar.time)

                if (doneDates.contains(checkDate)) {
                    consecutiveDays++
                } else {
                    break
                }
            }

            return consecutiveDays >= 2

        } catch (e: Exception) {
            Log.e("Homepage", "Error checking streak: ${e.message}")
            return false
        }
    }

    private fun getPreviousDate(dateStr: String?): String {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            if (dateStr.isNullOrEmpty()) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                calendar.time = formatter.parse(dateStr) ?: Date()
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            formatter.format(calendar.time)
        } catch (e: Exception) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        }
    }

    private fun resetHabitStatusForToday(currentDate: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("habits")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                snapshot?.documents?.forEach { document ->
                    val habitId = document.id
                    batch.update(db.collection("habits").document(habitId), "doneDates", listOf<String>())
                }

                batch.commit().addOnSuccessListener {
                    Log.d("Homepage", "All habits reset for new day")
                }.addOnFailureListener { e ->
                    Log.e("Homepage", "Error resetting habits: ${e.message}")
                }
            }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "User"
                        val profileUrl = document.getString("photoUrl")

                        greetingText.text = "Halo,"
                        usernameText.text = username
                        taglineText.text = "Begin. Believe. Become. Shine."

                        if (!profileUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileUrl).into(profileImage)
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile)
                        }
                    } else {
                        showToast("User data not found.")
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to load data: ${exception.message}")
                }
        }
    }

    private fun loadHabits() {
        val userId = auth.currentUser?.uid ?: return
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        db.collection("habits")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showToast("Error loading habits: ${e.message}")
                    return@addSnapshotListener
                }

                val habitsList = snapshot?.documents?.mapNotNull { doc ->
                    val habit = doc.toObject(Habit::class.java)?.copy(id = doc.id)
                    if (habit != null) {
                        if (!habit.doneDates.contains(currentDate)) {
                            habit.isDone = false
                        }
                    }
                    habit
                } ?: listOf()

                habits.clear()
                habits.addAll(habitsList)
                habitAdapter.notifyDataSetChanged()
            }
    }

    private fun setWeekDates() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -4)

        weekDates.clear()
        for (i in 0 until 8) {
            val day = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            val date = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            weekDates.add("$day $date")
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val dateAdapter = DateAdapter(weekDates)
        binding.recyclerViewDates.apply {
            layoutManager = LinearLayoutManager(this@Homepage, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }

        val today = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        dateAdapter.setSelectedDate(today)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
