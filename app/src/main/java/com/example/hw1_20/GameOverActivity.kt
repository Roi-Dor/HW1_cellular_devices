package com.example.hw1_20

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Retrieve the score from the bundle
        val score = intent.extras?.getInt("SCORE") ?: 0

        // Display the score
        val gameOverMessage = findViewById<TextView>(R.id.post_LBL_gameOver)
        gameOverMessage.text = "Game Over\nYour Score: $score"

        // Set up the "Play Again" button
        val playAgainButton = findViewById<MaterialButton>(R.id.post_BTN_play_again)
        playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            // Retrieve and pass back the original settings
            val refreshRate = intent.extras?.getLong("REFRESH_RATE", 1000L) ?: 1000L
            val controlType = intent.extras?.getString("CONTROL_TYPE") ?: "BUTTONS"

            val bundle = Bundle()
            bundle.putLong("REFRESH_RATE", refreshRate)
            bundle.putString("CONTROL_TYPE", controlType)
            intent.putExtras(bundle)

            startActivity(intent)
            finish() // Close GameOverActivity
        }

        // Set up the "Back to Menu" button
        val backToMenuButton = findViewById<MaterialButton>(R.id.post_BTN_back_to_menu)
        backToMenuButton.setOnClickListener {
            val intent = Intent(this, PreGameActivity::class.java)
            startActivity(intent)
            finish() // Close GameOverActivity
        }
    }
}
