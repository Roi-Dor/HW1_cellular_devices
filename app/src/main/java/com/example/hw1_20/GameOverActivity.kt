package com.example.hw1_20

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import fragments.MapFragment

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Retrieve the score from the bundle
        val score = intent.extras?.getInt("SCORE") ?: 0

        saveScore(score)

        // Display the score
        val gameOverMessage = findViewById<TextView>(R.id.post_LBL_gameOver)
        gameOverMessage.text = "Game Over\nYour Score: $score"

        // Add the top_score fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.game_over_fragment_container, fragments.scoresList())
            .commit()

        // Add the MapFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_fragment_container, MapFragment())
            .commit()

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

    private fun saveScore(score: Int) {
        val sharedPreferences = getSharedPreferences("GameScores", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing scores and parse them into a list
        val scores = sharedPreferences.getString("scores", "") ?: ""
        val scoreList = if (scores.isNotEmpty()) {
            scores.split(",").map { it.toInt() }
        } else {
            emptyList()
        }

        // Add the new score and sort the list in descending order
        val updatedScores = (scoreList + score).sortedDescending().take(10) // Keep only top 10 scores

        // Save the updated scores back to SharedPreferences
        editor.putString("scores", updatedScores.joinToString(","))
        editor.apply()
    }


    private fun getScores(): List<Int> {
        val sharedPreferences = getSharedPreferences("GameScores", MODE_PRIVATE)
        val scores = sharedPreferences.getString("scores", "") ?: ""
        return if (scores.isNotEmpty()) {
            scores.split(",").map { it.toInt() }.sortedDescending().take(10) // Limit to top 10
        } else {
            emptyList()
        }
    }


}
