package com.example.hw1_20

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class TiltDetector(context: Context, private val tiltCallback: MainActivity) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val tiltThreshold = 3.0 // Minimum tilt angle to trigger movement
    private val delayThreshold = 500L // Minimum time in ms to detect a tilt
    private var lastTiltTimestampX: Long = 0L

    private lateinit var sensorEventListener: SensorEventListener

    init {
        initEventListener()
    }

    private fun initEventListener() {
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                handleTilt(x)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for this implementation
            }
        }
    }

    private fun handleTilt(x: Float) {
        val currentTime = System.currentTimeMillis()

        if (abs(x) > tiltThreshold && currentTime - lastTiltTimestampX >= delayThreshold) {
            lastTiltTimestampX = currentTime
            if (x > 0) {
                tiltCallback.tiltX("LEFT")
            } else {
                tiltCallback.tiltX("RIGHT")
            }
        }
    }

    fun start() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(sensorEventListener)
    }
}
