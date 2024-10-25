package com.example.brighthabit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityEditMessageBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMessageBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            saveMessageToFirestore(messageText)
        }
    }

    private fun saveMessageToFirestore(messageText: String) {
        val message = Message(messageText)
        db.collection("messages")
            .add(message)
            .addOnSuccessListener {
                finish() // Go back to ForumActivity
            }
    }
}
