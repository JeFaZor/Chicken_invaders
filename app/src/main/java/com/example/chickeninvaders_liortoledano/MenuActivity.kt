package com.example.chickeninvaders_liortoledano

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MenuActivity : AppCompatActivity() {

    private lateinit var btnButtonModeSlow: MaterialButton
    private lateinit var btnButtonModeFast: MaterialButton
    private lateinit var btnSensorMode: MaterialButton
    private lateinit var btnHighScores: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViews()
        initViews()
    }

    private fun findViews() {
        btnButtonModeSlow = findViewById(R.id.menu_BTN_button_slow)
        btnButtonModeFast = findViewById(R.id.menu_BTN_button_fast)
        btnSensorMode = findViewById(R.id.menu_BTN_sensor_mode)
        btnHighScores = findViewById(R.id.menu_BTN_high_scores)
    }

    private fun initViews() {
        btnButtonModeSlow.setOnClickListener {
            startGame("BUTTON_SLOW")
        }

        btnButtonModeFast.setOnClickListener {
            startGame("BUTTON_FAST")
        }

        btnSensorMode.setOnClickListener {
            startGame("SENSOR")
        }

        btnHighScores.setOnClickListener {
            val intent = Intent(this, ScoreboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startGame(controlMode: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("CONTROL_MODE", controlMode)
        startActivity(intent)
    }
}