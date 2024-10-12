package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityOnboarding2Binding

class Onboarding2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetStarted.setOnClickListener {
            // Arahkan ke Login Activity
            startActivity(Intent(this, Login::class.java))
            finish() // Optional: tutup activity ini agar tidak bisa kembali ke onboarding
        }
    }
}