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


private lateinit var fusedLocationClient: FusedLocationProviderClient
private var currentScore by Delegates.notNull<Int>()


class GameOverActivity : AppCompatActivity(), ScoreClickListener {

    private lateinit var googleMap: GoogleMap
    private var isMapReady = false
    private var currentMarker: Marker? = null // Keep track of the current marker
    private var refreshRate: Long = 1000L
    private var controlType: String = "BUTTONS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Retrieve the score from the bundle
        currentScore = intent.extras?.getInt("SCORE") ?: 0

        // Retrieve current location (for demonstration, using default coordinates)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if the score qualifies for the top 10 and save it if it does
        getCurrentLocation { location ->
            if (isTop10Score(currentScore)) {
                saveScore(currentScore, location)
            }
        }

        // Retrieve the score and settings from the intent
        val score = intent.getIntExtra("SCORE", 0)
        refreshRate = intent.getLongExtra("REFRESH_RATE", 1000L)
        controlType = intent.getStringExtra("CONTROL_TYPE") ?: "BUTTONS"

        Toast.makeText(this, "Refresh: $refreshRate/nControls: $controlType" , Toast.LENGTH_SHORT).show()

        // Display the score
        val gameOverMessage = findViewById<TextView>(R.id.post_LBL_gameOver)
        gameOverMessage.text = "Game Over\nYour Score: $currentScore"

        // Add the top_score fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.game_over_fragment_container, ScoresList())
            .commit()

        // adding map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment_container) as? SupportMapFragment
        if (mapFragment == null) {
            val newMapFragment = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment_container, newMapFragment)
                .commit()
            newMapFragment.getMapAsync { map ->
                googleMap = map
                googleMap.uiSettings.isZoomControlsEnabled = true
                isMapReady = true // Set the flag when map is ready
            }
        } else {
            mapFragment.getMapAsync { map ->
                googleMap = map
                googleMap.uiSettings.isZoomControlsEnabled = true
                isMapReady = true
            }
        }


        // Set up the "Play Again" button
        val playAgainButton = findViewById<MaterialButton>(R.id.post_BTN_play_again)
        playAgainButton.setOnClickListener {
            startNewGame()
        }

        // Set up the "Back to Menu" button
        val backToMenuButton = findViewById<MaterialButton>(R.id.post_BTN_back_to_menu)
        backToMenuButton.setOnClickListener {
            val intent = Intent(this, PreGameActivity::class.java)
            startActivity(intent)
            finish() // Close GameOverActivity
        }
    }

    private fun startNewGame() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("REFRESH_RATE", refreshRate)
        intent.putExtra("CONTROL_TYPE", controlType)
        startActivity(intent)
        finish() // Close GameOverActivity
    }

    override fun onScoreSelected(location: Pair<Double, Double>) {
        if (isMapReady) {
            // Perform the map operation only if the map is ready
            val latLng = com.google.android.gms.maps.model.LatLng(location.first, location.second)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            // Add a new marker at the selected location
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
            )
        } else {
            // Handle the case where the map is not ready (optional)
            runOnUiThread {
                Toast.makeText(this, "Map is not ready yet. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveScore(score: Int, location: Pair<Double, Double>) {
        val sharedPreferences = getSharedPreferences("GameScores", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing scores and locations
        val scores = sharedPreferences.getString("scores", "") ?: ""
        val locations = sharedPreferences.getString("locations", "") ?: ""

        val scoreList = if (scores.isNotEmpty()) {
            scores.split(",").map { it.toInt() }
        } else {
            emptyList()
        }

        val locationList = if (locations.isNotEmpty()) {
            locations.split(";").map { loc ->
                val latLng = loc.split(",")
                Pair(latLng[0].toDouble(), latLng[1].toDouble())
            }
        } else {
            emptyList()
        }

        // Add the new score and location, then sort by score descending
        val updatedList = (scoreList.zip(locationList) + (score to location))
            .sortedByDescending { it.first }
            .take(10) // Keep only the top 10

        // Save updated scores and locations
        val updatedScores = updatedList.map { it.first }
        val updatedLocations = updatedList.map { "${it.second.first},${it.second.second}" }

        editor.putString("scores", updatedScores.joinToString(","))
        editor.putString("locations", updatedLocations.joinToString(";"))
        editor.apply()
    }

    private fun isTop10Score(score: Int): Boolean {
        val sharedPreferences = getSharedPreferences("GameScores", MODE_PRIVATE)
        val scores = sharedPreferences.getString("scores", "") ?: ""
        val scoreList = if (scores.isNotEmpty()) {
            scores.split(",").map { it.toInt() }
        } else {
            emptyList()
        }

        // Check if the score qualifies for the top 10
        return if (scoreList.size < 10) {
            true // Always qualifies if fewer than 10 scores
        } else {
            score > scoreList.minOrNull() ?: Int.MIN_VALUE
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Call getCurrentLocation with a lambda to handle the location
                getCurrentLocation { location ->
                    // Handle the retrieved location here
                    if (isTop10Score(currentScore)) { // Assuming `currentScore` is accessible here
                        saveScore(currentScore, location)
                    }
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun getCurrentLocation(onLocationReceived: (Pair<Double, Double>) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = Pair(location.latitude, location.longitude)
                    onLocationReceived(currentLocation)
                } else {
                    // Fallback in case location is null
                    val defaultLocation = Pair(32.0853, 34.7818) // Example: Tel Aviv
                    onLocationReceived(defaultLocation)
                }
            }.addOnFailureListener {
                // Handle failure (e.g., log error or show message)
            }
        } else {
            checkLocationPermission()
        }
    }


}
