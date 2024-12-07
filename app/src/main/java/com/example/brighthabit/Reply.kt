package com.example.brighthabit

data class Reply(
    val id: String = "",
    val text: String = "",
    val date: String = "",
    val parentId: String = "",
    val userId: String = "",
    var username: String = "",
    var photoUrl: String = ""
)
