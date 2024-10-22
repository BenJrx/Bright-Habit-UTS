package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Homepage : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var profileImage: CircleImageView
    private lateinit var greetingText: TextView
    private lateinit var dateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImage = findViewById(R.id.profileImage)
        greetingText = findViewById(R.id.greetingText)
        dateText = findViewById(R.id.todayDate)

        loadUserData()

        setCurrentDate()
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

                        findViewById<TextView>(R.id.username).text = username

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
}
