package com.example.brighthabit

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.brighthabit.databinding.ActivityMessageDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val messageId = intent.getStringExtra("messageId")

        if (messageId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Message ID is missing!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadMessageDetails(messageId)
        setupRepliesRecyclerView(messageId)

        binding.sendReplyButton.setOnClickListener {
            val replyText = binding.replyInput.text.toString()
            if (replyText.isNotEmpty()) {
                sendReply(messageId, replyText)
            }
        }
    }

    private fun loadMessageDetails(messageId: String) {
        val messageRef = db.collection("messages").document(messageId)

        messageRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    val date = document.getString("date") ?: "Unknown date"
                    val text = document.getString("text") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""

                    binding.username.text = username
                    binding.textViewDate.text = date
                    binding.textViewMessage.text = text

                    if (photoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(binding.profileImage)
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_default_profile)
                            .circleCrop()
                            .into(binding.profileImage)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendReply(messageId: String, replyText: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        if (currentUserId == "Unknown") {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val replyData = hashMapOf(
            "text" to replyText,
            "parentId" to messageId,
            "date" to System.currentTimeMillis().toString(),
            "userId" to currentUserId
        )

        db.collection("replies")
            .add(replyData)
            .addOnSuccessListener {
                binding.replyInput.text.clear()
                Toast.makeText(this, "Reply sent!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send reply: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRepliesRecyclerView(messageId: String) {
        binding.repliesRecyclerView.layoutManager = LinearLayoutManager(this)
        val repliesAdapter = ReplyAdapter()

        db.collection("replies")
            .whereEqualTo("parentId", messageId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val repliesList = mutableListOf<Reply>()

                val tasksCount = querySnapshot.size()
                var completedTasks = 0

                if (tasksCount == 0) {
                    Log.d("MessageDetailActivity", "No replies found for messageId: $messageId")
                    repliesAdapter.submitList(repliesList)
                    return@addOnSuccessListener
                }

                for (document in querySnapshot.documents) {
                    val reply = Reply(
                        id = document.id,
                        text = document.getString("text") ?: "",
                        date = document.getString("date") ?: "",
                        parentId = document.getString("parentId") ?: "",
                        userId = document.getString("userId") ?: ""
                    )

                    val userId = reply.userId
                    if (userId.isNotEmpty()) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val username = userDoc.getString("username") ?: "Unknown"
                                val photoUrl = userDoc.getString("photoUrl") ?: ""

                                Log.d("MessageDetailActivity", "User data fetched: $username, $photoUrl for userId: $userId")

                                // Update reply dengan data pengguna
                                reply.username = username
                                reply.photoUrl = photoUrl

                                repliesList.add(reply)
                                completedTasks++

                                if (completedTasks == tasksCount) {
                                    Log.d("MessageDetailActivity", "All replies processed successfully")
                                    repliesAdapter.submitList(repliesList)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("MessageDetailActivity", "Failed to fetch user data for userId: $userId", e)
                                reply.username = "Anonymous"
                                reply.photoUrl = ""
                                repliesList.add(reply)
                                completedTasks++
                                if (completedTasks == tasksCount) {
                                    repliesAdapter.submitList(repliesList)
                                }
                            }
                    } else {
                        Log.d("MessageDetailActivity", "No userId found for replyId: ${reply.id}")
                        repliesList.add(reply)
                        completedTasks++

                        if (completedTasks == tasksCount) {
                            repliesAdapter.submitList(repliesList)
                        }
                    }
                }

                binding.repliesRecyclerView.adapter = repliesAdapter
            }
            .addOnFailureListener { e ->
                Log.e("MessageDetailActivity", "Failed to load replies for messageId: $messageId", e)
                Toast.makeText(this, "Failed to load replies: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
