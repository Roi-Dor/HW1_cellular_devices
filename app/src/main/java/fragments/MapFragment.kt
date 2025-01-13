package fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hw1_20.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Retrieve top scores' locations from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("GameScores", Context.MODE_PRIVATE)
        val locations = sharedPreferences.getString("locations", "") ?: ""
        val scores = sharedPreferences.getString("scores", "") ?: ""

        if (locations.isNotEmpty() && scores.isNotEmpty()) {
            // Parse the locations and scores
            val locationList = locations.split(";").map { loc ->
                val latLng = loc.split(",")
                LatLng(latLng[0].toDouble(), latLng[1].toDouble())
            }

            val scoreList = scores.split(",").map { it.toInt() }

            // Add markers to the map for each location
            for (i in locationList.indices) {
                val position = locationList[i]
                val score = scoreList[i]
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Score: $score")
                )
            }

            // Center the map on the first location, if available
            if (locationList.isNotEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationList[0], 10f))
            }
        }

        // Optional: Customize map settings
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
    }
}
