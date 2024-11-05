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
            if (messageText.isNotBlank()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val message = Message("Username", currentDate, messageText)

                db.collection("messages").add(message)
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener {
                        // Menampilkan error (bisa menggunakan log atau toast)
                    }
            }
        }
    }
}
