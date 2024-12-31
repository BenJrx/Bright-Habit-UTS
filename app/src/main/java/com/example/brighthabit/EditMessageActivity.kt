package com.example.brighthabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.brighthabit.databinding.ActivityEditMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.location.Geocoder
import android.location.Address
import java.io.IOException

class EditMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMessageBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.buttonSubmit.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()

            val currentUser = auth.currentUser
            if (currentUser != null && messageText.isNotBlank()) {
                Log.d("EditMessageActivity", "User logged in: ${currentUser.uid}")
                getLocationAndSendMessage(currentUser.uid, messageText)
            } else {
                Log.e("EditMessageActivity", "No user logged in or message is empty.")
                Toast.makeText(this, "Message cannot be empty or user not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocationAndSendMessage(userId: String, messageText: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        val locationString = getLocationFromCoordinates(latitude, longitude)

                        fetchUserDataAndSendMessage(userId, messageText, latitude, longitude, locationString)
                    } else {
                        fetchUserDataAndSendMessage(userId, messageText, null, null, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditMessageActivity", "Error getting location: ${e.message}")
                    fetchUserDataAndSendMessage(userId, messageText, null, null, null)
                }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getLocationFromCoordinates(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: ""
                val country = address.countryName ?: ""
                return "$city, $country"
            }
        } catch (e: IOException) {
            Log.e("EditMessageActivity", "Geocoder error: ${e.message}")
        }
        return null
    }

    private fun fetchUserDataAndSendMessage(userId: String, messageText: String, latitude: Double?, longitude: Double?, location: String?) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userSnapshot ->
                if (userSnapshot.exists()) {
                    val username = userSnapshot.getString("username") ?: ""
                    val photoUrl = userSnapshot.getString("photoUrl") ?: ""
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    val message = Message(
                        username = username,
                        date = currentDate,
                        text = messageText,
                        photoUrl = photoUrl,
                        userId = userId,
                        latitude = latitude,
                        longitude = longitude,
                        location = location
                    )

                    db.collection("messages").add(message)
                        .addOnSuccessListener {
                            Log.d("EditMessageActivity", "Message sent successfully")
                            Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditMessageActivity", "Error sending message: ${e.message}")
                            Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("EditMessageActivity", "User document not found for userId=$userId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditMessageActivity", "Error fetching user data: ${e.message}")
                Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        getLocationAndSendMessage(currentUser.uid, binding.editTextMessage.text.toString())
                    }
                } else {
                    Log.e("EditMessageActivity", "Location permission denied")
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
