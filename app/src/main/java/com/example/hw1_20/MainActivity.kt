package com.example.hw1_20

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton


class MainActivity : AppCompatActivity() {

    // Grid references
    private lateinit var gridLayout: GridLayout
    private lateinit var playerCells: List<AppCompatImageView>
    private lateinit var obstacleCells: Array<Array<AppCompatImageView>>
    private var maxCol = 5
    private var maxRow = 9

    // Game state
    private var currentPlayerPosition = 2 // Player starts in the middle cell of the bottom row
    private var score = 0
    private var lives = 3
    private var gameRunning = false

    // Game loop handler
    private val handler = Handler(Looper.getMainLooper())
    private var refreshRate = 1000L // default Refresh rate

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize grid and UI
        initializeGrid()
        resetGame()

        // Button listeners for player movement
        findViewById<MaterialButton>(R.id.main_BTN_left).setOnClickListener {
            movePlayer("LEFT")
        }
        findViewById<MaterialButton>(R.id.main_BTN_right).setOnClickListener {
            movePlayer("RIGHT")
        }

        // Retrieve the refresh rate from the bundle
        refreshRate = intent.extras?.getLong("REFRESH_RATE", 1000L) ?: 1000L

        // Start the game loop
        startGameLoop()
    }

    private fun initializeGrid() {
        try {
            // Find the grid layout
            gridLayout = findViewById(R.id.grid_layout)



            // Initialize player cells in the bottom row
            playerCells = listOf(
                findViewById<AppCompatImageView>(R.id.player_cell_8_0)
                    ?: throw RuntimeException("Player cell R.id.player_cell_5_0 not found"),
                findViewById<AppCompatImageView>(R.id.player_cell_8_1)
                    ?: throw RuntimeException("Player cell R.id.player_cell_5_1 not found"),
                findViewById<AppCompatImageView>(R.id.player_cell_8_2)
                    ?: throw RuntimeException("Player cell R.id.player_cell_5_2 not found"),
                findViewById<AppCompatImageView>(R.id.player_cell_8_3)
                    ?: throw RuntimeException("Player cell R.id.player_cell_5_3 not found"),
                findViewById<AppCompatImageView>(R.id.player_cell_8_4)
                    ?: throw RuntimeException("Player cell R.id.player_cell_5_4 not found")
            )

            // Initialize obstacle cells
            obstacleCells = Array(maxRow) { row ->
                Array(maxCol) { column ->
                    val cellId = resources.getIdentifier("cell_${row}_${column}", "id", packageName)
                    if (cellId == 0) {
                        throw RuntimeException("Cell ID for cell_${row}_${column} not found")
                    }
                    findViewById<AppCompatImageView>(cellId)
                        ?: throw RuntimeException("View with ID cell_${row}_${column} not found")
                }
            }
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Error initializing grid: ${e.message}", Toast.LENGTH_LONG).show()
            throw e // Re-throw to crash the app if necessary
        }
    }



    private fun resetGame() {
        // Reset player position
        currentPlayerPosition = 2
        playerCells.forEachIndexed { index, AppCompatImageView ->
            AppCompatImageView.visibility = if (index == currentPlayerPosition) View.VISIBLE else View.INVISIBLE
        }

        // Reset obstacles
        obstacleCells.flatten().forEach { it.visibility = View.INVISIBLE }

        // Reset score and lives
        score = 0
        lives = 3
        updateLives()
    }

    private fun movePlayer(direction: String) {
        if (direction == "LEFT" && currentPlayerPosition > 0) {
            playerCells[currentPlayerPosition].visibility = View.INVISIBLE
            currentPlayerPosition--
            playerCells[currentPlayerPosition].visibility = View.VISIBLE
        } else if (direction == "RIGHT" && currentPlayerPosition < maxCol-1) {
            playerCells[currentPlayerPosition].visibility = View.INVISIBLE
            currentPlayerPosition++
            playerCells[currentPlayerPosition].visibility = View.VISIBLE
        }
    }
    private var obstacleRefreshCount = 0 // Class-level variable to track refresh count

    private fun generateObstacles() {
        val topRow = 0
        if (obstacleRefreshCount < 1) {
            obstacleRefreshCount+=1
        }else {
            val randomColumn = (0..maxCol-1).random()
            obstacleCells[topRow][randomColumn].visibility = View.VISIBLE
            obstacleRefreshCount=0
        }
    }

    private fun moveObstacles() {
        for (row in maxRow-2 downTo 0) { // Start from the second-to-last row
            for (column in 0..maxCol-1) {
                val currentCell = obstacleCells[row][column]
                val nextCell = obstacleCells[row + 1][column]

                if (currentCell.visibility == View.VISIBLE) {
                    currentCell.visibility = View.INVISIBLE
                    nextCell.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun clearBottom(){
        for (column in 0..maxCol-1) {
            if (obstacleCells[maxRow-1][column].visibility == View.VISIBLE) {
                obstacleCells[maxRow-1][column].visibility = View.INVISIBLE
            }
        }
    }

    private fun checkCollision() {
        // Check if an obstacle is in the same column as the player
        if (obstacleCells[maxRow-1][currentPlayerPosition].visibility == View.VISIBLE) {
            // Collision detected
            lives--
            updateLives()
            playCrashEffect()

            if (lives <= 0) {
                endGame()
            }
        }
    }

    private fun updateLives() {
        // Update UI for lives (e.g., hiding heart icons)
        findViewById<AppCompatImageView>(R.id.main_IMG_heart1).visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        findViewById<AppCompatImageView>(R.id.main_IMG_heart2).visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        findViewById<AppCompatImageView>(R.id.main_IMG_heart3).visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    private fun updateScore() {
        score++
        // Update score display (optional)
    }

    private fun playCrashEffect() {

        Toast.makeText(this, "Crash!", Toast.LENGTH_SHORT).show()
        // Get the Vibrator instance
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Trigger vibration
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500) // For older devices
        }
    }

    private fun startGameLoop() {
        gameRunning = true
        var count = 0
        handler.post(object : Runnable {
            override fun run() {
                if (gameRunning) {
                    if(count < 2) {
                        generateObstacles()
                    }
                    else {
                        count+=1
                    }
                    moveObstacles()
                    checkCollision()
                    clearBottom()
                    updateScore()
                    refreshUI()

                    handler.postDelayed(this, refreshRate)
                }
            }
        })
    }

    private fun pauseGame() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun endGame() {
        pauseGame()
        val handler = Handler(Looper.getMainLooper())
        // Show the Toast after 2 seconds
        handler.postDelayed({
            Toast.makeText(this, "Game Over! Score: $score", Toast.LENGTH_LONG).show()
        }, 2000)
        // Reset the game and start the loop after 5 seconds (2s for Toast + 3s for reset)
        handler.postDelayed({
            resetGame()
            startGameLoop()
        }, 5000)
    }


    private fun refreshUI() {
        // Refresh obstacles
        for (row in 0 until obstacleCells.size) {
            for (column in 0 until obstacleCells[row].size) {
                val cell = obstacleCells[row][column]
                if (cell.visibility == View.VISIBLE) {
                    cell.visibility = View.VISIBLE // Keep visible if it's already visible
                } else {
                    cell.visibility = View.INVISIBLE // Ensure it's invisible if not in use
                }
            }
        }

        // Refresh player
        playerCells.forEachIndexed { index, AppCompatImageView ->
            if (index == currentPlayerPosition) {
                AppCompatImageView.visibility = View.VISIBLE // Show player in the current position
            } else {
                AppCompatImageView.visibility = View.INVISIBLE // Hide player in other cells
            }
        }
    }

}
