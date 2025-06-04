package com.example.chickeninvaders_liortoledano

import java.text.SimpleDateFormat
import java.util.*

data class Score(
    val playerName: String,
    val score: Int,
    val coins: Int,
    val distance: Int,
    val date: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)