package com.example.chickeninvaders_liortoledano

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.example.chickeninvaders_liortoledano.utilities.ScoreManager

class GameOverActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var lblFinalScore: TextView
    private lateinit var lblCoins: TextView
    private lateinit var lblDistance: TextView
    private lateinit var etPlayerName: EditText
    private lateinit var btnSaveScore: MaterialButton
    private lateinit var btnNewGame: MaterialButton
    private lateinit var btnMainMenu: MaterialButton
    private lateinit var scoreManager: ScoreManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var finalScore = 0
    private var finalCoins = 0
    private var finalDistance = 0
    private var currentLatitude = 31.8969 // Default to Or Yehuda
    private var currentLongitude = 34.8186

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        finalCoins = intent.getIntExtra("FINAL_COINS", 0)
        finalDistance = intent.getIntExtra("FINAL_DISTANCE", 0)

        findViews()
        initViews()

        scoreManager = ScoreManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermission()
    }

    private fun findViews() {
        lblFinalScore = findViewById(R.id.gameOver_LBL_final_score)
        lblCoins = findViewById(R.id.gameOver_LBL_coins)
        lblDistance = findViewById(R.id.gameOver_LBL_distance)
        etPlayerName = findViewById(R.id.gameOver_ET_player_name)
        btnSaveScore = findViewById(R.id.gameOver_BTN_save_score)
        btnNewGame = findViewById(R.id.gameOver_BTN_new_game)
        btnMainMenu = findViewById(R.id.gameOver_BTN_main_menu)
    }

    private fun initViews() {
        lblFinalScore.text = "Final Score: $finalScore"
        lblCoins.text = "Coins Collected: $finalCoins"
        lblDistance.text = "Distance Traveled: $finalDistance"

        btnSaveScore.setOnClickListener {
            saveScore()
        }

        btnNewGame.setOnClickListener {
            startNewGame()
        }

        btnMainMenu.setOnClickListener {
            backToMenu()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
            }
        }
    }

    private fun saveScore() {
        val playerName = etPlayerName.text.toString().trim()
        if (playerName.isNotEmpty()) {
            val score = Score(
                playerName = playerName,
                score = finalScore,
                coins = finalCoins,
                distance = finalDistance,
                latitude = currentLatitude,
                longitude = currentLongitude
            )

            scoreManager.saveScore(score)

            btnSaveScore.isEnabled = false
            btnSaveScore.text = "Score Saved!"
        }
    }

    private fun startNewGame() {
        finish()
    }

    private fun backToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}