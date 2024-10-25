package com.example.brighthabit

data class Message(
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
