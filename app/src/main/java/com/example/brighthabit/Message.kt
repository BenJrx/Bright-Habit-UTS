package com.example.brighthabit

data class Message(
    val username: String = "",
    val date: String = "",
    val text: String = "",
    val photoUrl: String = "", // URL foto profil
    val userId: String = ""   // ID pengguna
)
