package com.example.brighthabit

data class Message(
    val id: String = "",
    val username: String = "",
    val date: String = "",
    val text: String = "",
    val photoUrl: String = "",
    val userId: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: String? = null
)
