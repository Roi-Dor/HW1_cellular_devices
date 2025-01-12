package fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hw1_20.R

class scoresList : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.top_scores, container, false)

        // Retrieve and display the top 10 scores
        val sharedPreferences = requireContext().getSharedPreferences("GameScores", Context.MODE_PRIVATE)
        val scores = sharedPreferences.getString("scores", "") ?: ""
        val topScores = if (scores.isNotEmpty()) {
            scores.split(",").map { it.toInt() }.sortedDescending().take(10) // Top 10 scores
        } else {
            listOf()
        }

        val messageView = view.findViewById<TextView>(R.id.fragment_LBL_scores)
        messageView.text = if (topScores.isNotEmpty()) {
            "Top Scores:\n${topScores.joinToString("\n")}"
        } else {
            "No scores yet. Play to set a high score!"
        }

        return view
    }
}