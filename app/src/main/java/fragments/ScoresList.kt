package fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import callbacks.ScoreClickListener
import com.example.hw1_20.R

class ScoresList : Fragment() {

    private var scoreClickListener: ScoreClickListener? = null
    private val scores = mutableListOf<Int>() // Top scores
    private val locations = mutableListOf<Pair<Double, Double>>() // Corresponding locations

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ScoreClickListener) {
            scoreClickListener = context
        } else {
            throw RuntimeException("$context must implement ScoreClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.top_scores, container, false)

        // Retrieve scores and locations from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("GameScores", Context.MODE_PRIVATE)
        val savedScores = sharedPreferences.getString("scores", "") ?: ""
        val savedLocations = sharedPreferences.getString("locations", "") ?: ""

        if (savedScores.isNotEmpty() && savedLocations.isNotEmpty()) {
            scores.addAll(savedScores.split(",").map { it.toInt() })
            locations.addAll(savedLocations.split(";").map {
                val latLng = it.split(",")
                Pair(latLng[0].toDouble(), latLng[1].toDouble())
            })
        }

        // Set up ListView to display scores
        val listView = view.findViewById<ListView>(R.id.fragment_LST_scores)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scores.map { "Score: $it" })
        listView.adapter = adapter

        // Handle item clicks to pass location to MapFragment
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedLocation = locations[position]
            scoreClickListener?.onScoreSelected(selectedLocation)
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        scoreClickListener = null
    }
}
