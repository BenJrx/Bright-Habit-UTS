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
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewForum.layoutManager = LinearLayoutManager(this)
        forumAdapter = ForumAdapter(
            onReplyClick = { message ->
                val intent = Intent(this, MessageDetailActivity::class.java).apply {
                    putExtra("messageId", message.id)
                }
                startActivity(intent)
            },
            onItemClick = { message ->
                val intent = Intent(this, MessageDetailActivity::class.java).apply {
                    putExtra("messageId", message.id)
                }
                startActivity(intent)
            }
        )
        binding.recyclerViewForum.adapter = forumAdapter

        loadMessages { messages ->
            forumAdapter.submitList(messages)
        }
    }

    private fun loadMessages(onComplete: (List<Message>) -> Unit) {
        val messagesRef = db.collection("messages")

        messagesRef.get()
            .addOnSuccessListener { messagesSnapshot ->
                val messages = mutableListOf<Message>()

                for (doc in messagesSnapshot.documents) {
                    val message = Message(
                        id = doc.id,
                        username = doc.getString("username") ?: "Unknown",
                        date = doc.getString("date") ?: "",
                        text = doc.getString("text") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        userId = doc.getString("userId") ?: ""
                    )
                    messages.add(message)
                }

                onComplete(messages)
            }
            .addOnFailureListener { e ->
                onComplete(emptyList())
            }
    }
}
