package com.example.brighthabit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityEditMessageBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMessageBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            val username = "YourUsername" // Gantilah sesuai mekanisme mendapatkan username pengguna
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            saveMessageToFirestore(username, date, messageText)
        }
    }

    private fun saveMessageToFirestore(username: String, date: String, text: String) {
        val message = Message(username, date, text)
        db.collection("messages")
            .add(message)
            .addOnSuccessListener {
                finish() // Kembali ke ForumActivity setelah berhasil
            }
    }
}
