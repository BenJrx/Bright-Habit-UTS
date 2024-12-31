package com.example.brighthabit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var editNama: EditText
    private lateinit var editUmur: EditText
    private lateinit var editEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnEdit: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnLogout: Button
    private lateinit var authProfile: FirebaseAuth
    private var user = FirebaseAuth.getInstance().currentUser
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profile_image)
        editNama = findViewById(R.id.show_nama)
        editEmail = findViewById(R.id.show_email)
        editUmur = findViewById(R.id.show_age)
        btnSave = findViewById(R.id.btnSave)
        btnEdit = findViewById(R.id.btnEdit)
        btnBack = findViewById(R.id.btnBack)
        btnLogout = findViewById(R.id.btnLogout)

        authProfile = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserProfile()
        disableEditing()
        btnSave.visibility = View.GONE

        btnEdit.setOnClickListener {
            enableEditing()
        }

        btnSave.setOnClickListener {
            saveUserProfile()
            disableEditing()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile() {
        user?.let {
            val email = it.email
            editEmail.setText(email ?: "Email tidak tersedia")
            editEmail.isEnabled = false

            val userId = it.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nama = document.getString("username")
                        val umur = document.getString("age")
                        val photoUrl = document.getString("photoUrl")

                        editNama.setText(nama ?: "")
                        editUmur.setText(umur ?: "")

                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(profileImage)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfile() {
        val nama = editNama.text.toString()
        val umur = editUmur.text.toString()

        if (user != null) {
            val userId = user!!.uid
            val userMap = mapOf(
                "username" to nama,
                "age" to umur
            )
            firestore.collection("users").document(userId).update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun disableEditing() {
        editNama.isEnabled = false
        editUmur.isEnabled = false
        btnSave.isEnabled = false
        btnSave.visibility = View.GONE
    }

    private fun enableEditing() {
        editNama.isEnabled = true
        editUmur.isEnabled = true
        btnSave.isEnabled = true
        btnSave.visibility = View.VISIBLE
    }
}
