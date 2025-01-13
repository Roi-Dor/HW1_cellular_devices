package com.example.hw1_20

import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import callbacks.GameUpdateCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import logic.GameManager
import callbacks.TiltCallback

class MainActivity : AppCompatActivity(), GameUpdateCallback, TiltCallback {
    private lateinit var gameManager: GameManager

    // Views
    private lateinit var main_LBL_score_text: MaterialTextView
    private lateinit var gridLayout: GridLayout
    private lateinit var playerCells: List<AppCompatImageView>
    private lateinit var obstacleCells: Array<Array<AppCompatImageView>>
    private lateinit var prizesCells: Array<Array<AppCompatImageView>>
    private lateinit var btnLeft: MaterialButton
    private lateinit var btnRight: MaterialButton


    // Game configuration
    private val maxRow = 9
    private val maxCol = 5
    private var isTiltControlEnabled = false
    private var tiltDetector: TiltDetector? = null
    private var refreshRate = 1000L
    private var controlType = "BUTTONS"

    //sound
    private lateinit var hitSoundPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the settings from the intent
        refreshRate = intent.getLongExtra("REFRESH_RATE", 1000L)
        controlType = intent.getStringExtra("CONTROL_TYPE") ?: "BUTTONS"

        findViews()
        initViews()



        gameManager = GameManager(
            maxRow = maxRow,
            maxCol = maxCol,
            obstacleCells = obstacleCells,
            prizeCells = prizesCells,
            playerCells = playerCells,
            onGameUpdate = this,
            hitSoundPlayer = hitSoundPlayer
        )


        isTiltControlEnabled = intent.getStringExtra("CONTROL_TYPE") == "TILT"

        if (controlType == "TILT") {
            setupTiltControl()
        } else {
            setupButtonControl()
        }

        gameManager.resetGame()
        gameManager.startGameLoop(refreshRate)
    }

    // Find all views
    private fun findViews() {
        main_LBL_score_text = findViewById(R.id.main_LBL_score_text)
        gridLayout = findViewById(R.id.grid_layout)
        btnLeft = findViewById(R.id.main_BTN_left)
        btnRight = findViewById(R.id.main_BTN_right)

        // Player cells
        playerCells = (0 until maxCol).map { col ->
            val id = resources.getIdentifier("player_cell_8_$col", "id", packageName)
            findViewById<AppCompatImageView>(id)
        }

        // Obstacle cells
        obstacleCells = Array(maxRow) { row ->
            Array(maxCol) { col ->
                val idOBS = resources.getIdentifier("cell_OBS_${row}_${col}", "id", packageName)
                findViewById(idOBS) ?: throw RuntimeException("OBS Cell ID for cell_${row}_${col} not found")
            }
        }

        // Prize cells
        prizesCells = Array(maxRow) { row ->
            Array(maxCol) { col ->
                val idPRZ = resources.getIdentifier("cell_PRZ_${row}_${col}", "id", packageName)
                findViewById(idPRZ) ?: throw RuntimeException("PRZ Cell ID for cell_${row}_${col} not found")
            }
        }
    }

    // Initialize views and set up event listeners
    private fun initViews() {
        btnLeft.setOnClickListener { gameManager.movePlayer("LEFT") }
        btnRight.setOnClickListener { gameManager.movePlayer("RIGHT") }
        // Initialize MediaPlayer
        hitSoundPlayer = MediaPlayer.create(this, R.raw.getting_hit)
    }

    private fun setupTiltControl() {
        btnLeft.visibility = View.GONE
        btnRight.visibility = View.GONE
        val tiltDetector = TiltDetector(this, this)
        tiltDetector.start()
    }

    override fun tiltX(direction: String) {
        gameManager.movePlayer(direction)
    }

    override fun onScoreUpdate(score: Int) {
        main_LBL_score_text.text = score.toString()
    }

    override fun onLivesUpdate(lives: Int) {
        updateLivesUI(lives)
    }

    override fun onGameOver(score: Int) {
        val intent = Intent(this, GameOverActivity::class.java)

        // Create a Bundle to pass the data
        val bundle = Bundle().apply {
            putInt("SCORE", score)
            putLong("REFRESH_RATE", refreshRate) // Pass refreshRate
            putString("CONTROL_TYPE", controlType) // Pass controlType
        }

        // Attach the Bundle to the Intent
        intent.putExtras(bundle)

        startActivity(intent)
        finish()
    }

    private fun setupButtonControl() {
        // Ensure buttons are properly initialized
        btnLeft.setOnClickListener {
            gameManager.movePlayer("LEFT") // Move the player to the left
        }

        btnRight.setOnClickListener {
            gameManager.movePlayer("RIGHT") // Move the player to the right
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop and release TiltDetector
        if (isTiltControlEnabled) {
            tiltDetector?.stop()
            tiltDetector = null
        }

        if (::hitSoundPlayer.isInitialized) {
            hitSoundPlayer.release()
        }
    }


    // Update lives UI based on remaining lives
    private fun updateLivesUI(lives: Int) {
        listOf(
            R.id.main_IMG_heart1 to 1,
            R.id.main_IMG_heart2 to 2,
            R.id.main_IMG_heart3 to 3
        ).forEach { (id, threshold) ->
            findViewById<AppCompatImageView>(id).visibility =
                if (lives >= threshold) View.VISIBLE else View.INVISIBLE
        }
    }
}

