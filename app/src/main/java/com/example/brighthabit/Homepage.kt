package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.brighthabit.databinding.ActivityHomepageBinding

class Homepage : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var profileImage: CircleImageView
    private lateinit var greetingText: TextView
    private lateinit var dateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImage = binding.profileImage
        greetingText = binding.greetingText
        dateText = binding.todayDate

        binding.recyclerViewHabits.layoutManager = LinearLayoutManager(this)
        val habitAdapter = HabitAdapter(getHabits())
        binding.recyclerViewHabits.adapter = habitAdapter

        loadUserData()
        setCurrentDate()

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
                        binding.username.text = username

                        if (!profileUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileUrl).into(profileImage)
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile) // Set a default profile image
                        }
                    } else {
                        showToast("Data pengguna tidak ditemukan.")
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Gagal memuat data: ${exception.message}")
                }
        } else {
            showToast("Pengguna tidak terautentikasi.")
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("EEEE dd", Locale.getDefault())
        val currentDate = sdf.format(Date())
        dateText.text = currentDate
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getHabits(): MutableList<Habit> {
        return mutableListOf(
            Habit("Belajar", "10/13/2024", false),
            Habit("Mengaji", "10/13/2024", false),
            Habit("Membaca Alkitab", "10/13/2024", false)
        )
    }
}
