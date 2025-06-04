package com.example.chickeninvaders_liortoledano.utilities

import android.content.Context
import android.content.SharedPreferences
import com.example.chickeninvaders_liortoledano.Score
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreManager(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveScore(score: Score) {
        val currentScores = getTopScores().toMutableList()
        currentScores.add(score)

        // Sort by score descending and keep only top 10
        val topScores = currentScores.sortedByDescending { it.score }.take(10)

        val json = gson.toJson(topScores)
        sharedPrefs.edit().putString("scores", json).apply()
    }

    fun getTopScores(): List<Score> {
        val json = sharedPrefs.getString("scores", null) ?: return emptyList()
        val type = object : TypeToken<List<Score>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}