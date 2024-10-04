package com.example.brighthabit

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    lateinit var binding : ActivityRegisterBinding
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val age = binding.etAge.text.toString()
            val username = binding.etUsername.text.toString()

            if (email.isEmpty()){
                binding.etEmail.error = "Email is Required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.etEmail.error = "Invalid Email"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()){
                binding.etPassword.error = "Password is Required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 10){
                binding.etPassword.error = "Password Minimal 10 Karakter"
            }
        }
        }
    }
