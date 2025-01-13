package com.example.hw1_20

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import callbacks.ScoreClickListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import fragments.ScoresList
import kotlin.properties.Delegates

class GameOverActivity : AppCompatActivity(), ScoreClickListener {

    // Map-related variables
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isMapReady = false
    private var currentMarker: Marker? = null

    // Game settings and score
    private var currentScore by Delegates.notNull<Int>()
    private var refreshRate: Long = 1000L
    private var controlType: String = "BUTTONS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Retrieve data and set up UI
        initializeData()
        setupUI()
        setupMapFragment()
    }

    // Retrieves data passed from the previous activity (score, settings) and saves the top score
    private fun initializeData() {
        val bundle = intent.extras
        currentScore = bundle?.getInt("SCORE") ?: 0
        refreshRate = bundle?.getLong("REFRESH_RATE", 1000L) ?: 1000L
        controlType = bundle?.getString("CONTROL_TYPE", "BUTTONS") ?: "BUTTONS"

        // Save the score with the current location if it's in the top 10
        getCurrentLocation { location ->
            if (isTop10Score(currentScore)) {
                saveScore(currentScore, location)
            }
        }
    }

    // Sets up buttons and adds the ScoresList fragment
    private fun setupUI() {
        // Display game over message with the score
        findViewById<TextView>(R.id.post_LBL_gameOver).text = "Game Over\nYour Score: $currentScore"

        // Play again button: restart the game with the same settings
        findViewById<MaterialButton>(R.id.post_BTN_play_again).setOnClickListener {
            startNewGame()
        }

        // Back to menu button: return to the main menu
        findViewById<MaterialButton>(R.id.post_BTN_back_to_menu).setOnClickListener {
            val intent = Intent(this, PreGameActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Add the ScoresList fragment to display top scores
        supportFragmentManager.beginTransaction()
            .replace(R.id.game_over_fragment_container, ScoresList())
            .commit()
    }

    // Initializes the map fragment and sets up the Google Map
    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment_container) as? SupportMapFragment
        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment_container, newMapFragment)
                .commit()
            newMapFragment.getMapAsync { setupGoogleMap(it) }
        } else {
            mapFragment.getMapAsync { setupGoogleMap(it) }
        }
    }

    // Configures the Google Map when ready
    private fun setupGoogleMap(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        isMapReady = true
    }

    // Starts a new game with the same difficulty and control settings
    private fun startNewGame() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("REFRESH_RATE", refreshRate)
            putExtra("CONTROL_TYPE", controlType)
        }
        startActivity(intent)
        finish()
    }

    // Called when a score is selected from the list; zooms the map to the corresponding location
    override fun onScoreSelected(location: Pair<Double, Double>) {
        if (!isMapReady) {
            Toast.makeText(this, "Map is not ready yet. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        val latLng = com.google.android.gms.maps.model.LatLng(location.first, location.second)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        currentMarker?.remove()
        currentMarker = googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
    }

    // Saves the top 10 scores with their corresponding locations
    private fun saveScore(score: Int, location: Pair<Double, Double>) {
        val sharedPreferences = getSharedPreferences("GameScores", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val scores = sharedPreferences.getString("scores", "") ?: ""
        val locations = sharedPreferences.getString("locations", "") ?: ""

        val scoreList = scores.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
        val locationList = locations.split(";").filter { it.isNotEmpty() }.map {
            val latLng = it.split(",")
            Pair(latLng[0].toDouble(), latLng[1].toDouble())
        }

        // Merge the current score and location, keep only the top 10 scores
        val updatedList = (scoreList.zip(locationList) + (score to location))
            .sortedByDescending { it.first }
            .take(10)

        // Save updated scores and locations
        editor.putString("scores", updatedList.joinToString(",") { it.first.toString() })
        editor.putString("locations", updatedList.joinToString(";") { "${it.second.first},${it.second.second}" })
        editor.apply()
    }

    // Checks if the current score qualifies for the top 10
    private fun isTop10Score(score: Int): Boolean {
        val scores = getSharedPreferences("GameScores", MODE_PRIVATE).getString("scores", "") ?: ""
        val scoreList = scores.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
        return scoreList.size < 10 || score > scoreList.minOrNull() ?: Int.MIN_VALUE
    }

    // Retrieves the current location, with a default fallback
    private fun getCurrentLocation(onLocationReceived: (Pair<Double, Double>) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val currentLocation = location?.let { Pair(it.latitude, it.longitude) } ?: Pair(32.0853, 34.7818) // Default: Tel Aviv
            onLocationReceived(currentLocation)
        }
    }

    // Requests location permission from the user
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation { location ->
                if (isTop10Score(currentScore)) saveScore(currentScore, location)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1 // Permission request code for location
    }
}
