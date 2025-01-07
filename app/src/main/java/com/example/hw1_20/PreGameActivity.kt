package com.example.hw1_20

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class PreGameActivity : AppCompatActivity() {

    private var selectedDifficulty: Long? = null // Store the selected difficulty
    private var selectedControl: String? = null // Store the selected control option

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)

        val startGameButton = findViewById<MaterialButton>(R.id.pre_BTN_start)

        // Difficulty selection
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

        // Control selection
        findViewById<MaterialButton>(R.id.pre_BTN_buttonControl).setOnClickListener {
            selectedControl = "BUTTONS"
            checkSelections(startGameButton)
        }

        findViewById<MaterialButton>(R.id.pre_BTN_tiltControl).setOnClickListener {
            selectedControl = "TILT"
            checkSelections(startGameButton)
        }

        // Start game
        startGameButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val bundle = Bundle()
            selectedDifficulty?.let { bundle.putLong("REFRESH_RATE", it) }
            selectedControl?.let { bundle.putString("CONTROL_TYPE", it) }
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    // Enable "Start Game" button only when both options are selected
    private fun checkSelections(startGameButton: MaterialButton) {
        startGameButton.isEnabled = selectedDifficulty != null && selectedControl != null
    }
}