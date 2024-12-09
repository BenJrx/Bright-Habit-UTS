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


        // Check if user is logged in
        val userId = auth.currentUser?.uid
        if (userId == null) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }


        // Initialize RecyclerView with empty adapter
        habitAdapter = HabitAdapter(habits)
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(this@Homepage)
            adapter = habitAdapter
        }

        // Add swipe to delete functionality
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position in habits.indices) { // Ensure position is within range
                    val habitToDelete = habits[position]


                    // Temporary removal for visual feedback
                    val removedHabit = habits.removeAt(position)
                    habitAdapter.notifyItemRemoved(position)


                    // Delete from Firestore
                    db.collection("habits").document(habitToDelete.id)
                        .delete()
                        .addOnSuccessListener {
                            showToast("Habit deleted successfully")
                        }
                        .addOnFailureListener { e ->
                            showToast("Error deleting habit: ${e.message}")
                            // Revert deletion if Firestore fails
                            habits.add(position, removedHabit)
                            habitAdapter.notifyItemInserted(position)
                        }
                } else {
                    habitAdapter.notifyDataSetChanged() // Refresh entire list if invalid position
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
        } else {
            showToast("User not authenticated.")
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
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
                        habit.isDone = habit.doneDates.contains(currentDate) || habit.isDone
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
        calendar.add(Calendar.DAY_OF_YEAR, -6)


        val weekDates = mutableListOf<String>()


        for (i in 0 until 7) {
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


    private fun updateHabitStatus(habitId: String, currentDate: String) {
        val userId = auth.currentUser?.uid ?: return
        val habitRef = db.collection("habits").document(habitId)


        habitRef.update(
            "doneDates", FieldValue.arrayUnion(currentDate),
            "isDone", true
        ).addOnSuccessListener {
            Log.d("Homepage", "Habit status updated successfully")
        }.addOnFailureListener { e ->
            showToast("Error updating habit: ${e.message}")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
