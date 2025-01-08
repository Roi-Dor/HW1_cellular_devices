package com.example.hw1_20

import android.os.*
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity(), TiltCallback {

    // Constants
    private val maxCol = 5
    private val maxRow = 9

    // UI Components
    private lateinit var gridLayout: GridLayout
    private lateinit var playerCells: List<AppCompatImageView>
    private lateinit var obstacleCells: Array<Array<AppCompatImageView>>
    private lateinit var prizesCells: Array<Array<AppCompatImageView>>

    // Game State
    private var currentPlayerPosition = 2
    private var score = 0
    private var lives = 3
    private var gameRunning = false
    private var refreshRate = 1000L

    // Control
    private lateinit var tiltDetector: TiltDetector
    private var isTiltControlEnabled = false

    // Handler
    private val handler = Handler(Looper.getMainLooper())
    private var obstacleRefreshCount = 0
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeGrid()
        resetGame()

        // Retrieve settings from intent
        refreshRate = intent.extras?.getLong("REFRESH_RATE", 1000L) ?: 1000L
        isTiltControlEnabled = intent.extras?.getString("CONTROL_TYPE", "BUTTONS") == "TILT"

        if (isTiltControlEnabled) {
            setupTiltControl()
        } else {
            setupButtonControl()
        }

        startGameLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTiltControlEnabled) {
            tiltDetector.stop()
        }
    }

    private fun initializeGrid() {
        gridLayout = findViewById(R.id.grid_layout)

        // Initialize player cells
        playerCells = (0 until maxCol).map { col ->
            val id = resources.getIdentifier("player_cell_8_$col", "id", packageName)
            findViewById<AppCompatImageView>(id)
        }

        // Initialize obstacle cells
        obstacleCells = Array(maxRow) { row ->
            Array(maxCol) { col ->
                val idOBS = resources.getIdentifier("cell_OBS_${row}_${col}", "id", packageName)
                findViewById(idOBS) ?: throw RuntimeException("OBS Cell ID for cell_${row}_${col} not found")
            }
        }

        // Initialize prizes cells
        prizesCells = Array(maxRow) { row ->
            Array(maxCol) { col ->
                val idPRZ = resources.getIdentifier("cell_PRZ_${row}_${col}", "id", packageName)
                findViewById(idPRZ) ?: throw RuntimeException("OBS Cell ID for cell_${row}_${col} not found")
            }
        }
    }

    private fun resetGame() {
        // Reset player position
        currentPlayerPosition = 2
        playerCells.forEachIndexed { index, cell ->
            cell.visibility = if (index == currentPlayerPosition) View.VISIBLE else View.INVISIBLE
        }

        // Reset obstacles
        obstacleCells.flatten().forEach { it.visibility = View.INVISIBLE }

        // Reset score and lives
        score = 0
        lives = 3
        updateLives()
    }

    private fun setupButtonControl() {
        findViewById<MaterialButton>(R.id.main_BTN_left).setOnClickListener { movePlayer("LEFT") }
        findViewById<MaterialButton>(R.id.main_BTN_right).setOnClickListener { movePlayer("RIGHT") }
    }

    private fun setupTiltControl() {
        findViewById<MaterialButton>(R.id.main_BTN_left).visibility = View.GONE
        findViewById<MaterialButton>(R.id.main_BTN_right).visibility = View.GONE
        tiltDetector = TiltDetector(this, this)
        tiltDetector.start()
    }

    override fun tiltX(direction: String) {
        if (isTiltControlEnabled) {
            movePlayer(direction)
        }
    }

    private fun movePlayer(direction: String) {
        playerCells[currentPlayerPosition].visibility = View.INVISIBLE
        when (direction) {
            "LEFT" -> if (currentPlayerPosition > 0) currentPlayerPosition--
            "RIGHT" -> if (currentPlayerPosition < maxCol - 1) currentPlayerPosition++
        }
        playerCells[currentPlayerPosition].visibility = View.VISIBLE
    }

    private fun startGameLoop() {
        gameRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (gameRunning) {
                    if (obstacleRefreshCount < 1) {
                        obstacleRefreshCount++
                    } else {
                        generateObstacles()
                        generatePrizes()
                        obstacleRefreshCount = 0
                    }
                    moveObstacles()
                    movePrizes()
                    checkCollision()
                    updateScore()
                    clearBottom()
                    handler.postDelayed(this, refreshRate)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        gameRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        if (!isPaused) {
            Toast.makeText(this, "Resuming game...", Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                isPaused = false
                if (lives <= 0) endGame()
                else startGameLoop()
            }, 1000)
        }
    }

    private fun endGame() {
        onPause()
        Toast.makeText(this, "Game Over! Score: $score", Toast.LENGTH_LONG).show()
        handler.postDelayed({
            resetGame()
            startGameLoop()
        }, 5000)
    }

    private fun generateObstacles() {
        val topRow = 0
        val randomColumn = (0 until maxCol).random()
        obstacleCells[topRow][randomColumn].visibility = View.VISIBLE
    }

    private fun generatePrizes() {
        val topRow = 0
        var randomColumn = (0 until maxCol).random()
        // Can not have an obstacle and a prize on the same cell
        while(obstacleCells[topRow][randomColumn].visibility == View.VISIBLE){
            randomColumn = (0 until maxCol).random()
        }
        obstacleCells[topRow][randomColumn].visibility = View.VISIBLE
    }

    private fun moveObstacles() {
        for (row in maxRow - 2 downTo 0) {
            for (col in 0 until maxCol) {
                val currentCell = obstacleCells[row][col]
                val belowCell = obstacleCells[row + 1][col]

                if (currentCell.visibility == View.VISIBLE) {
                    currentCell.visibility = View.INVISIBLE
                    belowCell.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun movePrizes() {
        for (row in maxRow - 2 downTo 0) {
            for (col in 0 until maxCol) {
                val currentCell = prizesCells[row][col]
                val belowCell = prizesCells[row + 1][col]

                if (currentCell.visibility == View.VISIBLE) {
                    currentCell.visibility = View.INVISIBLE
                    belowCell.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkCollision() {
        if (obstacleCells[maxRow - 1][currentPlayerPosition].visibility == View.VISIBLE) {
            lives--
            updateLives()
            playCrashEffect()
            if (lives <= 0) endGame()
        }
    }

    private fun clearBottom() {
        obstacleCells[maxRow - 1].forEach { it.visibility = View.INVISIBLE }
    }

    private fun updateLives() {
        listOf(
            R.id.main_IMG_heart1 to 1,
            R.id.main_IMG_heart2 to 2,
            R.id.main_IMG_heart3 to 3
        ).forEach { (id, threshold) ->
            findViewById<AppCompatImageView>(id).visibility =
                if (lives >= threshold) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateScore() {
        if (obstacleCells[maxRow - 1][currentPlayerPosition].visibility == View.VISIBLE) {
            score++
        }
    }

    private fun playCrashEffect() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
        Toast.makeText(this, "Crash!", Toast.LENGTH_SHORT).show()
    }
}
