package logic

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import callbacks.GameUpdateCallback

private var obstacleRefreshCount = 0


class GameManager(
    private val maxRow: Int,
    private val maxCol: Int,
    private val obstacleCells: Array<Array<AppCompatImageView>>,
    private val prizeCells: Array<Array<AppCompatImageView>>,
    private val playerCells: List<AppCompatImageView>,
    private val onGameUpdate: GameUpdateCallback,
    private val hitSoundPlayer: MediaPlayer?
){
    private var currentPlayerPosition = 2
    private var score = 0
    private var lives = 3
    private var refreshRate = 1000L
    private val handler = Handler(Looper.getMainLooper())
    private var gameRunning = false


    // Starts the game loop
    fun startGameLoop(refreshRate: Long) {
        this.refreshRate = refreshRate
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
                    clearBottom()
                    onGameUpdate.onScoreUpdate(score)
                    handler.postDelayed(this, refreshRate)
                }
            }
        })
    }

    fun stopGameLoop() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    fun resetGame() {
        score = 0
        lives = 3
        currentPlayerPosition = 2
        playerCells.forEachIndexed { index, cell ->
            cell.visibility = if (index == currentPlayerPosition) View.VISIBLE else View.INVISIBLE
        }
        obstacleCells.flatten().forEach { it.visibility = View.INVISIBLE }
        prizeCells.flatten().forEach { it.visibility = View.INVISIBLE }
        onGameUpdate.onScoreUpdate(score)
        onGameUpdate.onLivesUpdate(lives)
    }

    fun movePlayer(direction: String) {
        playerCells[currentPlayerPosition].visibility = View.INVISIBLE
        when (direction) {
            "LEFT" -> if (currentPlayerPosition > 0) currentPlayerPosition--
            "RIGHT" -> if (currentPlayerPosition < maxCol - 1) currentPlayerPosition++
        }
        playerCells[currentPlayerPosition].visibility = View.VISIBLE
    }

    private fun generateObstacles() {
        val randomColumn = (0 until maxCol).random()
        obstacleCells[0][randomColumn].visibility = View.VISIBLE
    }

    private fun generatePrizes() {
        val randomColumn = (0 until maxCol).random()
        if (obstacleCells[0][randomColumn].visibility == View.INVISIBLE) {
            prizeCells[0][randomColumn].visibility = View.VISIBLE
        }
    }

    private fun moveObstacles() {
        for (row in maxRow - 2 downTo 0) {
            for (col in 0 until maxCol) {
                if (obstacleCells[row][col].visibility == View.VISIBLE) {
                    obstacleCells[row][col].visibility = View.INVISIBLE
                    obstacleCells[row + 1][col].visibility = View.VISIBLE
                }
            }
        }
    }

    private fun movePrizes() {
        for (row in maxRow - 2 downTo 0) {
            for (col in 0 until maxCol) {
                if (prizeCells[row][col].visibility == View.VISIBLE) {
                    prizeCells[row][col].visibility = View.INVISIBLE
                    prizeCells[row + 1][col].visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkCollision() {
        val playerCell = obstacleCells[maxRow - 1][currentPlayerPosition]
        val prizeCell = prizeCells[maxRow - 1][currentPlayerPosition]

        if (playerCell.visibility == View.VISIBLE) {
            lives--

            if (hitSoundPlayer != null && !hitSoundPlayer.isPlaying) {
                hitSoundPlayer.start()
            }

            onGameUpdate.onLivesUpdate(lives)
            if (lives <= 0) {
                onGameUpdate.onGameOver(score)
                stopGameLoop()
            }
        }

        if (prizeCell.visibility == View.VISIBLE) {
            score++
            prizeCell.visibility = View.INVISIBLE
            onGameUpdate.onScoreUpdate(score)
        }
    }

    private fun clearBottom() {
        obstacleCells[maxRow - 1].forEach { it.visibility = View.INVISIBLE }
        prizeCells[maxRow - 1].forEach { it.visibility = View.INVISIBLE }
    }
}