package com.example.brighthabit

data class Habit(
    val id: String = "",
    val namaHabit: String = "",
    val goal: String = "",
    val times: Int = 0,
    val date: String = "",
    var isDone: Boolean = false,
    val userId: String = "",
    val doneDates: MutableList<String> = mutableListOf(),
    var isNotDone: Int = 0
)
