package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.brighthabit.databinding.ActivityForumBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class ForumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForumBinding
    private lateinit var forumAdapter: ForumAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewForum.layoutManager = LinearLayoutManager(this)
        forumAdapter = ForumAdapter()
        binding.recyclerViewForum.adapter = forumAdapter

        binding.fabAddMessage.setOnClickListener {
            val intent = Intent(this, EditMessageActivity::class.java)
            startActivity(intent)
        }

        // Load messages from Firestore
        loadMessagesFromFirestoreRealtime()
    }

    // Method untuk mengambil data secara realtime dari Firestore
    private fun loadMessagesFromFirestoreRealtime() {
        db.collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Jika ada error, tampilkan log atau toast
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
