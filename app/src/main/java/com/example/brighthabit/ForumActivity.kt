package com.example.brighthabit

import android.content.ContentValues.TAG
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

        // Panggil fungsi untuk memuat pesan dari Firestore
        loadMessages { messages ->
            forumAdapter.submitList(messages)
        }
    }

    // Fungsi untuk mendapatkan data pesan langsung dari Firestore
    private fun loadMessages(onComplete: (List<Message>) -> Unit) {
        val messagesRef = db.collection("messages")

        messagesRef.get()
            .addOnSuccessListener { messagesSnapshot ->
                val messages = mutableListOf<Message>()

                // Ambil data langsung dari dokumen di koleksi messages
                for (doc in messagesSnapshot.documents) {
                    val message = Message(
                        username = doc.getString("username") ?: "Unknown", // Ambil username langsung
                        date = doc.getString("date") ?: "",                // Ambil tanggal
                        text = doc.getString("text") ?: "",                // Ambil isi pesan
                        photoUrl = doc.getString("photoUrl") ?: "",        // Ambil URL foto
                        userId = doc.getString("userId") ?: ""             // Ambil userId
                    )
                    messages.add(message)
                    Log.d(TAG, "Message loaded: $message") // Debug log
                }

                onComplete(messages) // Kirim data ke adapter
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch messages: ${e.message}")
                onComplete(emptyList()) // Kirim daftar kosong jika gagal
            }
    }
}
