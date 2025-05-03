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

class MainActivity : AppCompatActivity() {

    // Constants for game configuration
    companion object {
        const val ROWS = 5
        const val COLS = 3
        const val MAX_LIVES = 3
        const val INITIAL_DELAY = 1000L  // Milliseconds between game updates
    }

    // Cell types for the game matrix
    enum class CellType {
        EMPTY,
        SPACESHIP,
        CHICKEN
    }

    // Game state variables
    private var lives = MAX_LIVES
    private var isGameActive = false

    // Game board matrix
    private lateinit var gameMatrix: Array<Array<CellType>>

    // Initial position of the spaceship
    private var spaceshipRow = ROWS - 1  // Bottom row
    private var spaceshipCol = COLS / 2  // Middle column

    // UI Elements
    private lateinit var hearts: Array<ImageView>
    private lateinit var btnLeft: MaterialButton
    private lateinit var btnRight: MaterialButton
    private lateinit var gridGameBoard: GridLayout

    // Game board cells (ImageViews)
    private lateinit var cellViews: Array<Array<ImageView>>

    // Handler for game updates
    private val handler = Handler(Looper.getMainLooper())
    private val gameRunnable = object : Runnable {
        override fun run() {
            updateGame()
            if (isGameActive) {
                handler.postDelayed(this, INITIAL_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
    }

    private fun initGameMatrix() {
        // Initialize the game matrix with all cells empty
        gameMatrix = Array(ROWS) { Array(COLS) { CellType.EMPTY } }

        // Place the spaceship at its initial position
        gameMatrix[spaceshipRow][spaceshipCol] = CellType.SPACESHIP
    }

    private fun findViews() {
        // Find heart ImageViews
        hearts = arrayOf(
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2),
            findViewById(R.id.main_IMG_heart3)
        )

        // Find buttons
        btnLeft = findViewById(R.id.main_BTN_left)
        btnRight = findViewById(R.id.main_BTN_right)

        // Find grid layout
        gridGameBoard = findViewById(R.id.main_GRID_gameBoard)
    }

    private fun initViews() {
        // Set button click listeners
        btnLeft.setOnClickListener { moveSpaceship(0, -1) }
        btnRight.setOnClickListener { moveSpaceship(0, 1) }
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
                }
            }
        }

        // Update hearts display
        for (i in hearts.indices) {
            hearts[i].visibility = if (i < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun startGame() {
        isGameActive = true
        handler.postDelayed(gameRunnable, INITIAL_DELAY)
    }

    private fun stopGame() {
        isGameActive = false
        handler.removeCallbacks(gameRunnable)
    }

    private fun updateGame() {
        // Move existing chickens down
        moveChickensDown()

        // Generate new chickens at the top row with some probability
        generateNewChickens()

        // Update UI
        updateUI()
    }


    private val oldChickensInLastRow = BooleanArray(COLS) { false }

    private fun moveChickensDown() {
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
                            oldChickensInLastRow[col] = true;
                        }
                    }
                    gameMatrix[row - 1][col] = CellType.EMPTY
                }
            }
        }

        for (col in 0 until COLS) {
            if (gameMatrix[0][col] == CellType.CHICKEN) {
                gameMatrix[0][col] = CellType.EMPTY
            }
        }
    }
    private fun generateNewChickens() {
        val chickensInRow = BooleanArray(COLS) { false }
        var chickenCount = 0


        for (col in 0 until COLS) {
            if (Math.random() < 0.15 && chickenCount < 2) {
                gameMatrix[0][col] = CellType.CHICKEN
                chickenCount++
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
            if (gameMatrix[newRow][newCol] == CellType.CHICKEN) {
                handleCollision()
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

    private fun gameOver() {
        // Stop game updates
        stopGame()

        // Show game over message
        Toast.makeText(this, R.string.game_over, Toast.LENGTH_LONG).show()

        // Reset the game after a delay
        handler.postDelayed({
            resetGame()
        }, 3000)
    }

    private fun resetGame() {
        // Reset lives
        lives = MAX_LIVES

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
    }
}