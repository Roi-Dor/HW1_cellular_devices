package com.example.hw1_20

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PreGameActivity : AppCompatActivity() {

    private var selectedDifficulty: Long? = null // Selected difficulty
    private var selectedControl: String? = null // Selected control option

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)

        val startGameButton = findViewById<MaterialButton>(R.id.pre_BTN_start)

        // Set difficulty listeners
        setDifficultyListeners(startGameButton)

        // Set control listeners
        setControlListeners(startGameButton)

        // Start game when both options are selected
        startGameButton.setOnClickListener { startGame() }
    }

    private fun setDifficultyListeners(startGameButton: MaterialButton) {
        findViewById<MaterialButton>(R.id.pre_BTN_easy).setOnClickListener {
            selectedDifficulty = 1200L
            checkSelections(startGameButton)
        }
        findViewById<MaterialButton>(R.id.pre_BTN_medium).setOnClickListener {
            selectedDifficulty = 800L
            checkSelections(startGameButton)
        }
        findViewById<MaterialButton>(R.id.pre_BTN_hard).setOnClickListener {
            selectedDifficulty = 400L
            checkSelections(startGameButton)
        }
    }

    private fun setControlListeners(startGameButton: MaterialButton) {
        findViewById<MaterialButton>(R.id.pre_BTN_buttonControl).setOnClickListener {
            selectedControl = "BUTTONS"
            checkSelections(startGameButton)
        }
        findViewById<MaterialButton>(R.id.pre_BTN_tiltControl).setOnClickListener {
            selectedControl = "TILT"
            checkSelections(startGameButton)
        }
    }

    private fun startGame() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("REFRESH_RATE", selectedDifficulty)
            putExtra("CONTROL_TYPE", selectedControl)
        }
        startActivity(intent)
    }

    private fun checkSelections(startGameButton: MaterialButton) {
        startGameButton.isEnabled = selectedDifficulty != null && selectedControl != null
    }
}
