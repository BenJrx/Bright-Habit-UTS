package com.example.brighthabit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityEditMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMessageBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()

            // Periksa apakah pengguna sudah login dan pesan tidak kosong
            val currentUser = auth.currentUser
            if (currentUser != null && messageText.isNotBlank()) {
                Log.d("EditMessageActivity", "User logged in: ${currentUser.uid}")
                // Ambil data pengguna dan kirim pesan
                val userId = currentUser.uid
                fetchUserDataAndSendMessage(userId, messageText)
            } else {
                Log.e("EditMessageActivity", "No user logged in or message is empty.")
                // Tampilkan pesan kesalahan jika pengguna tidak login atau pesan kosong
            }
        }
    }

    private fun fetchUserDataAndSendMessage(userId: String, messageText: String) {
        // Ambil data pengguna dari koleksi `users`
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userSnapshot ->
                if (userSnapshot.exists()) {
                    val username = userSnapshot.getString("username") ?: ""
                    val photoUrl = userSnapshot.getString("photoUrl") ?: ""
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    // Buat data pesan dengan informasi pengguna
                    val message = Message(
                        username = username,
                        date = currentDate,
                        text = messageText,
                        photoUrl = photoUrl,
                        userId = userId
                    )

                    // Simpan pesan ke koleksi `messages`
                    db.collection("messages").add(message)
                        .addOnSuccessListener {
                            Log.d("EditMessageActivity", "Message sent successfully: $message")
                            finish() // Kembali ke activity sebelumnya
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditMessageActivity", "Error sending message: ${e.message}")
                        }
                } else {
                    Log.e("EditMessageActivity", "User document not found for userId=$userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditMessageActivity", "Error fetching user data: ${e.message}")
            }
    }
}
