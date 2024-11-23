package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brighthabit.databinding.ActivityForumBinding
import com.google.firebase.firestore.FirebaseFirestore

class ForumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForumBinding
    private lateinit var forumAdapter: ForumAdapter
    private val db = FirebaseFirestore.getInstance() // Referensi ke Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView untuk menampilkan daftar pesan
        binding.recyclerViewForum.layoutManager = LinearLayoutManager(this)
        forumAdapter = ForumAdapter()
        binding.recyclerViewForum.adapter = forumAdapter

        // Aksi saat tombol tambah pesan ditekan
        binding.fabAddMessage.setOnClickListener {
            val intent = Intent(this, EditMessageActivity::class.java)
            startActivity(intent)
        }

        // Panggil fungsi untuk memuat pesan dari Firestore secara real-time
        loadMessagesWithUserData { messages ->
            forumAdapter.submitList(messages)
        }
    }

    // Fungsi untuk mendapatkan data pesan dan data pengguna dengan benar dari Firestore
    private fun loadMessagesWithUserData(onComplete: (List<Message>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val messagesRef = firestore.collection("messages")
        val usersRef = firestore.collection("users")

        messagesRef.get().addOnSuccessListener { messagesSnapshot ->
            val messages = mutableListOf<Message>()
            val userIds = mutableSetOf<String>()

            // Ambil userId dari setiap pesan
            for (doc in messagesSnapshot.documents) {
                val userId = doc.getString("userId") ?: ""
                userIds.add(userId)
            }

            // Ambil data pengguna berdasarkan userId
            if (userIds.isNotEmpty()) {
                usersRef.whereIn("userId", userIds.toList()).get()
                    .addOnSuccessListener { usersSnapshot ->
                        val usersMap = usersSnapshot.documents.associateBy(
                            { it.getString("userId") ?: "" },
                            { it }
                        )

                        // Gabungkan data pesan dengan data pengguna
                        for (doc in messagesSnapshot.documents) {
                            val userId = doc.getString("userId") ?: ""
                            val userDoc = usersMap[userId]

                            val message = Message(
                                username = userDoc?.getString("username") ?: "Unknown",  // Pastikan username terisi
                                date = doc.getString("date") ?: "",
                                text = doc.getString("text") ?: "",
                                photoUrl = userDoc?.getString("photoUrl") ?: "",  // Pastikan photoUrl terisi
                                userId = userId
                            )

                            messages.add(message)
                        }

                        onComplete(messages)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            } else {
                onComplete(emptyList())
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

}
