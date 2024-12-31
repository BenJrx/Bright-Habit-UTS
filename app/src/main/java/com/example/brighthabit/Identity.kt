package com.example.brighthabit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brighthabit.databinding.ActivityIdentityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class Identity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityBinding
    private var selectedPhotoUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        binding.btnAddPhoto.setOnClickListener {
            openGallery()
        }

        binding.btnRegister.setOnClickListener {
            val age = binding.etAge.text.toString()
            val username = binding.etUsername.text.toString()

            if (age.isEmpty() || username.isEmpty() || selectedPhotoUri == null) {
                if (age.isEmpty()) binding.etAge.error = "Umur tidak boleh kosong"
                if (username.isEmpty()) binding.etUsername.error = "Username tidak boleh kosong"
                if (selectedPhotoUri == null) showToast("Silakan pilih foto")
                return@setOnClickListener
            }

            registerUser(email, password, username, age, selectedPhotoUri)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            binding.tvPhoto.text = "Foto Dipilih"
        }
    }

    private fun registerUser(email: String?, password: String?, username: String, age: String, photoUri: Uri?) {
        auth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadPhotoToStorage(photoUri, username, age)
                } else {
                    showToast("Pendaftaran gagal: ${task.exception?.message}")
                }
            }
    }

    private fun uploadPhotoToStorage(photoUri: Uri?, username: String, age: String) {
        val photoRef = storage.reference.child("photos/${UUID.randomUUID()}.jpg")

        photoRef.putFile(photoUri!!)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserDataToFirestore(uri.toString(), username, age)
                }
            }.addOnFailureListener { exception ->
                showToast("Upload foto gagal: ${exception.message}")
            }
    }

    private fun saveUserDataToFirestore(photoUrl: String, username: String, age: String) {
        val userId = auth.currentUser?.uid
        val userData = hashMapOf(
            "username" to username,
            "age" to age,
            "photoUrl" to photoUrl
        )

        userId?.let {
            firestore.collection("users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    showToast("Pendaftaran berhasil!")

                    val intent = Intent(this, Homepage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                }.addOnFailureListener { e ->
                    showToast("Gagal menyimpan data pengguna: ${e.message}")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGE = 1
    }
}
