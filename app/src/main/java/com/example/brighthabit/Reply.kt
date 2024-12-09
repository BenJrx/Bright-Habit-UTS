package com.example.brighthabit

data class Reply(
    val id: String,
    val text: String,
    val date: String,
    val parentId: String,
    val userId: String,
    var username: String = "Anonymous",
    var photoUrl: String = ""
)