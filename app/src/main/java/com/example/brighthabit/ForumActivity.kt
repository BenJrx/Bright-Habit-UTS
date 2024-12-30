package com.example.brighthabit

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // Menambahkan jarak antar item di RecyclerView
        val decoration = SpaceItemDecoration(40) // Mengatur jarak antar item (16dp)
        binding.recyclerViewForum.addItemDecoration(decoration)

        loadMessages { messages ->
            forumAdapter.submitList(messages)
        }

        binding.fabAddMessage.setOnClickListener {
            val intent = Intent(this, EditMessageActivity::class.java)
            startActivity(intent)
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
                        location = doc.getString("location") ?: "",
                        text = doc.getString("text") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        userId = doc.getString("userId") ?: ""
                    )
                    messages.add(message)
                }

                onComplete(messages)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    // Kelas untuk ItemDecoration agar memberi jarak antar item
    class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = space // Memberikan jarak bawah antar item
        }
    }
}
