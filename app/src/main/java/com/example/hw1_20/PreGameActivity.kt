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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_game)

        val startGameButton = findViewById<MaterialButton>(R.id.start_game_button)

        // Set click listeners for the difficulty buttons
        findViewById<MaterialButton>(R.id.easy_button).setOnClickListener {
            selectedDifficulty = 1200L
            startGameButton.isEnabled = true // Enable start game button
        }

        findViewById<MaterialButton>(R.id.medium_button).setOnClickListener {
            selectedDifficulty = 800L
            startGameButton.isEnabled = true // Enable start game button
        }

        findViewById<MaterialButton>(R.id.hard_button).setOnClickListener {
            selectedDifficulty = 400L
            startGameButton.isEnabled = true // Enable start game button
        }

        // Set click listener for the start game button
        startGameButton.setOnClickListener {
            selectedDifficulty?.let { difficulty ->
                val intent = Intent(this, MainActivity::class.java)
                val bundle = Bundle()
                bundle.putLong("REFRESH_RATE", difficulty) // Pass the selected difficulty
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }
    }
}