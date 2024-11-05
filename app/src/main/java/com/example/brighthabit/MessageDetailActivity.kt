package com.example.brighthabit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityMessageDetailBinding

class MessageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("username")
        val date = intent.getStringExtra("date")
        val message = intent.getStringExtra("message")

        binding.username.text = username
        binding.textViewDate.text = date
        binding.textViewMessage.text = message
    }
}
