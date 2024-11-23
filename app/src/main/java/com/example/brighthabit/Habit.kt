package com.example.brighthabit

data class Habit(
    val id: String = "", // Firestore document ID
    val namaHabit: String = "",
    val goal: String = "",
    val times: Int = 0,
    val date: String = "",
    var isDone: Boolean = false,
    val userId: String = "" // To associate habits with specific users
)


//data class Attendance(
//    val date: String,
//    val checkInTime: String,
//    val checkOutTime: String,
//    val checkInPhotoUrl: String,
//    val checkOutPhotoUrl: String
//)