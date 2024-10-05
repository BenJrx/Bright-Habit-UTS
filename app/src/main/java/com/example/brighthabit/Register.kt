package com.example.brighthabit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Register : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Listener untuk input gambar
        binding.btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(intent)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val age = binding.etAge.text.toString()
            val username = binding.etUsername.text.toString()

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is Required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Invalid Email"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Password is Required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 10) {
                binding.etPassword.error = "Password Minimal 10 Karakter"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            if (age.isEmpty()) {
                binding.etAge.error = "Age is Required"
                binding.etAge.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                binding.etUsername.error = "Username is Required"
                binding.etUsername.requestFocus()
                return@setOnClickListener
            }

            RegisterFirebase(email, password, age, username)
        }
    }

    private fun RegisterFirebase(email: String, password: String, age: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val uid = it.result?.user?.uid ?: ""
                    uploadImageToFirebase(uid, email, age, username)
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebase(uid: String, email: String, age: String, username: String) {
        if (selectedPhotoUri != null) {
            val filename = UUID.randomUUID().toString()
            val ref = storage.getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        saveUserToFirebaseDatabase(uid, email, age, username, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveUserToFirebaseDatabase(uid, email, age, username, null)
        }
    }

    private fun saveUserToFirebaseDatabase(uid: String, email: String, age: String, username: String, photoUrl: String?) {
        val user = hashMapOf(
            "uid" to uid,
            "email" to email,
            "age" to age,
            "username" to username,
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Register Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedPhotoUri = result.data?.data
            binding.btnAddPhoto.setImageURI(selectedPhotoUri)
        }
    }
}
