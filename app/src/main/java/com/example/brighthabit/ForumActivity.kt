package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
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
        loadMessagesFromFirestoreRealtime()
    }

    // Fungsi untuk mendapatkan data pesan secara real-time dari Firestore
    private fun loadMessagesFromFirestoreRealtime() {
        db.collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messagesList = mutableListOf<Message>()
                    for (document in snapshot.documents) {
                        val message = document.toObject(Message::class.java)
                        message?.let { messagesList.add(it) }
                    }
                    forumAdapter.submitList(messagesList)
                }
            }
    }
}
