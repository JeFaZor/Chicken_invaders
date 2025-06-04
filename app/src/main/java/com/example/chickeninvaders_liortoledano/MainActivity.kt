package com.example.chickeninvaders_liortoledano

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.button.MaterialButton
import android.os.VibratorManager
import android.widget.TextView
import com.example.chickeninvaders_liortoledano.utilities.SingleSoundPlayer
import com.example.chickeninvaders_liortoledano.utilities.TiltDetector
import com.example.chickeninvaders_liortoledano.interfaces.TiltCallback
import android.content.Intent

class MainActivity : AppCompatActivity() {

    // Constants for game configuration
    companion object {
        const val ROWS = 8
        const val COLS = 5
        const val MAX_LIVES = 3
        const val SLOW_DELAY = 1500L
        const val FAST_DELAY = 800L
        const val SENSOR_DELAY = 1000L

    }

    // Cell types for the game matrix
    enum class CellType {
        EMPTY,
        SPACESHIP,
        CHICKEN,
        COIN
    }

    // Game state variables
    private var lives = MAX_LIVES
    private var coinsCollected = 0
    private var distance = 0
    private var score = 0
    private var isGameActive = false
    private lateinit var controlMode: String
    private var currentSpeed = SENSOR_DELAY

    // Game board matrix
    private lateinit var gameMatrix: Array<Array<CellType>>

    // Initial position of the spaceship
    private var spaceshipRow = ROWS - 1  // Bottom row
    private var spaceshipCol = COLS / 2  // Middle column

    // UI Elements
    private lateinit var hearts: Array<ImageView>
    private lateinit var coinsLabel: TextView
    private lateinit var distanceLabel: TextView
    private lateinit var btnLeft: MaterialButton
    private lateinit var btnRight: MaterialButton
    private lateinit var gridGameBoard: GridLayout
    private lateinit var tiltDetector: TiltDetector

    // Game board cells (ImageViews)
    private lateinit var cellViews: Array<Array<ImageView>>

    // Handler for game updates
    private val handler = Handler(Looper.getMainLooper())
    private val gameRunnable = object : Runnable {
        override fun run() {
            updateGame()
            if (isGameActive) {
                handler.postDelayed(this, getGameDelay())  // שנה את זה גם
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        controlMode = intent.getStringExtra("CONTROL_MODE") ?: "BUTTON_SLOW"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize game data structures
        initGameMatrix()

        // Find and initialize UI components
        findViews()
        initViews()

        // Create game board
        createGameBoard()

        // Start game
        startGame()
        initTiltDetector()
        // Start tilt detector if in sensor mode
        if (controlMode == "SENSOR") {
            tiltDetector.start()
        }

    }

    private fun initTiltDetector() {
        tiltDetector = TiltDetector(
            context = this,
            tiltCallback = object : TiltCallback {
                override fun tiltLeft() {
                    if (controlMode == "SENSOR") {
                        moveSpaceship(0, -1) // Move left
                    }
                }

                override fun tiltRight() {
                    if (controlMode == "SENSOR") {
                        moveSpaceship(0, 1) // Move right
                    }
                }

                override fun tiltForward() {
                    if (controlMode == "SENSOR") {
                        currentSpeed = FAST_DELAY // Fast when tilted forward
                    }
                }

                override fun tiltBackward() {
                    if (controlMode == "SENSOR") {
                        currentSpeed = SLOW_DELAY // Slow when tilted backward
                    }
                }


            }
        )
    }

    private fun initGameMatrix() {
        // Initialize the game matrix with all cells empty
        gameMatrix = Array(ROWS) { Array(COLS) { CellType.EMPTY } }

        // Place the spaceship at its initial position
        gameMatrix[spaceshipRow][spaceshipCol] = CellType.SPACESHIP
    }

    private fun getGameDelay(): Long {
        return when (controlMode) {
            "BUTTON_SLOW" -> SLOW_DELAY
            "BUTTON_FAST" -> FAST_DELAY
            "SENSOR" -> currentSpeed
            else -> SLOW_DELAY
        }
    }

    private fun findViews() {
        // Find heart ImageViews
        hearts = arrayOf(
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2),
            findViewById(R.id.main_IMG_heart3)
        )

        coinsLabel = findViewById(R.id.main_LBL_coins)
        distanceLabel = findViewById(R.id.main_LBL_distance)


        // Find buttons
        btnLeft = findViewById(R.id.main_BTN_left)
        btnRight = findViewById(R.id.main_BTN_right)

        // Find grid layout
        gridGameBoard = findViewById(R.id.main_GRID_gameBoard)
    }

    private fun initViews() {
        // Set button click listeners only for button modes
        if (controlMode != "SENSOR") {
            btnLeft.setOnClickListener { moveSpaceship(0, -1) }
            btnRight.setOnClickListener { moveSpaceship(0, 1) }
        } else {
            // Hide buttons in sensor mode
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
        }
    }

    private fun createGameBoard() {
        // Remove any existing views from the grid
        gridGameBoard.removeAllViews()

        // Initialize the array for cell views
        cellViews = Array(ROWS) { Array(COLS) { ImageView(this) } }

        // Create ImageViews for each cell and add them to the grid
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = ImageView(this)
                cell.layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.getDimensionPixelSize(R.dimen.cell_size)
                    height = resources.getDimensionPixelSize(R.dimen.cell_size)
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                }

                // Set padding/margin if needed
                cell.setPadding(4, 4, 4, 4)

                // Add to grid
                gridGameBoard.addView(cell)

                // Store reference
                cellViews[row][col] = cell
            }
        }

        // Update UI to match the initial game matrix
        updateUI()
    }

    private fun updateUI() {
        // Update the game board UI based on the matrix
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = cellViews[row][col]
                when (gameMatrix[row][col]) {
                    CellType.EMPTY -> cell.setImageResource(android.R.color.transparent)
                    CellType.SPACESHIP -> cell.setImageResource(R.drawable.spaceship)
                    CellType.CHICKEN -> cell.setImageResource(R.drawable.chicken)
                    CellType.COIN -> cell.setImageResource(R.drawable.coin)
                }
            }
        }

        // Update hearts display
        for (i in hearts.indices) {
            hearts[i].visibility = if (i < lives) View.VISIBLE else View.INVISIBLE
        }
        coinsLabel.text = "Coins: $coinsCollected"
        distanceLabel.text = "Distance: $distance"


    }

    private fun startGame() {
        isGameActive = true
        handler.postDelayed(gameRunnable, getGameDelay())
    }

    private fun stopGame() {
        isGameActive = false
        handler.removeCallbacks(gameRunnable)
    }

    private fun updateGame() {
        // Move existing chickens down
        moveObjectsDown()

        // Generate new chickens and coins at the top row
        generateNewObjects()

        // Update distance counter
        distance++

        // Update score (distance gives points + coins give bonus)
        score = distance + (coinsCollected * 10)

        // Update UI
        updateUI()
    }


    private val oldChickensInLastRow = BooleanArray(COLS) { false }

    private fun moveObjectsDown() {
        for (col in 0 until COLS) {
            if (oldChickensInLastRow[col] && gameMatrix[ROWS - 1][col] == CellType.CHICKEN) {
                gameMatrix[ROWS - 1][col] = CellType.EMPTY
            }
            oldChickensInLastRow[col] = false
        }

        for (row in ROWS - 1 downTo 1) {
            for (col in 0 until COLS) {
                if (gameMatrix[row - 1][col] == CellType.CHICKEN) {
                    if (gameMatrix[row][col] == CellType.SPACESHIP) {
                        handleCollision()
                    } else {
                        gameMatrix[row][col] = CellType.CHICKEN
                        if (row == ROWS - 1) {
                            oldChickensInLastRow[col] = true
                        }
                    }
                    gameMatrix[row - 1][col] = CellType.EMPTY
                } else if (gameMatrix[row - 1][col] == CellType.COIN) {  // הוסף את החלק הזה
                    if (gameMatrix[row][col] == CellType.SPACESHIP) {
                        handleCoinCollection()
                    } else {
                        gameMatrix[row][col] = CellType.COIN
                    }
                    gameMatrix[row - 1][col] = CellType.EMPTY
                }
            }
        }

        for (col in 0 until COLS) {
            if (gameMatrix[0][col] == CellType.CHICKEN) {
                gameMatrix[0][col] = CellType.EMPTY
            } else if (gameMatrix[0][col] == CellType.COIN) {  // הוסף את זה
                gameMatrix[0][col] = CellType.EMPTY
            }
        }
    }
    private fun generateNewObjects() {
        var chickenCount = 0

        // Generate chickens
        for (col in 0 until COLS) {
            if (Math.random() < 0.15 && chickenCount < 2) {
                gameMatrix[0][col] = CellType.CHICKEN
                chickenCount++
            }
        }

        // Generate coins (lower probability than chickens)
        for (col in 0 until COLS) {
            if (gameMatrix[0][col] == CellType.EMPTY && Math.random() < 0.1) {
                gameMatrix[0][col] = CellType.COIN
            }
        }
    }

    private fun moveSpaceship(rowDelta: Int, colDelta: Int) {
        // Calculate new position
        val newRow = spaceshipRow + rowDelta
        val newCol = spaceshipCol + colDelta

        // Check if the new position is valid
        if (newCol in 0 until COLS && newRow in 0 until ROWS) {
            // Check if there's a chicken at the new position
            // Check if there's a chicken at the new position
            if (gameMatrix[newRow][newCol] == CellType.CHICKEN) {
                handleCollision()
            } else if (gameMatrix[newRow][newCol] == CellType.COIN) {
                handleCoinCollection()
            }

            // Clear current position
            gameMatrix[spaceshipRow][spaceshipCol] = CellType.EMPTY

            // Update spaceship position
            spaceshipRow = newRow
            spaceshipCol = newCol

            // Set spaceship at new position
            gameMatrix[spaceshipRow][spaceshipCol] = CellType.SPACESHIP

            // Update UI
            updateUI()
        }
    }

    private fun handleCollision() {
        SingleSoundPlayer(this).playSound(R.raw.crash)
        // Vibrate device - with compatibility for newer Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // For Android 12 (API 31) and higher
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For older versions
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        // Show toast message
        Toast.makeText(this, R.string.crash_message, Toast.LENGTH_SHORT).show()

        // Reduce lives
        lives--

        // Check if game over
        if (lives <= 0) {
            gameOver()
        }
    }

    private fun handleCoinCollection() {
        coinsCollected++

    }

    private fun gameOver() {
        // Stop game updates
        stopGame()

        // Calculate final score
        score = distance + (coinsCollected * 10)

        // Navigate to game over screen
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("FINAL_SCORE", score)
        intent.putExtra("FINAL_COINS", coinsCollected)
        intent.putExtra("FINAL_DISTANCE", distance)
        startActivity(intent)
        finish() // Close the game
    }

    private fun resetGame() {
        // Reset lives
        lives = MAX_LIVES

        // Reset counters
        coinsCollected = 0
        distance = 0
        score = 0

        // Reset game matrix
        initGameMatrix()

        // Update UI
        updateUI()

        // Restart game
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to stop the handler when the activity is destroyed
        stopGame()

        // Stop tilt detector
        if (controlMode == "SENSOR") {
            tiltDetector.stop()
        }
    }
    override fun onPause() {
        super.onPause()
        // Stop game when app goes to background
        if (isGameActive) {
            stopGame()
        }

        // Stop tilt detector if in sensor mode
        if (controlMode == "SENSOR") {
            tiltDetector.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isGameActive) {
            startGame()
        }

        // Start tilt detector if in sensor mode
        if (controlMode == "SENSOR") {
            tiltDetector.start()
        }
    }
}