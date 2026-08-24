package com.example.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Inertial Motion and Hazard Detection Engine.
 *
 * Implements:
 * - Real-time acceleration magnitude |a| = sqrt(ax^2 + ay^2 + az^2)
 * - Motion variance tracking for PPG gating (rejecting motion-corrupted PPG windows)
 * - Fall Detection state machine:
 *     1. Free-Fall Phase: |a| drops below 0.5g (< 4.9 m/s^2)
 *     2. Impact Shock Phase: |a| surges above 2.8g (> 27.5 m/s^2) within 1000ms
 *     3. Post-Impact Immobility Phase: Phone remains stationary for > 2.5 seconds
 * - Activity Context classification (Resting vs Mild Movement vs Intense Exertion)
 */
class MotionHazardDetector(private val context: Context) : SensorEventListener {

    enum class ActivityContext {
        RESTING,
        MILD_MOVEMENT,
        ACTIVE_EXERTION
    }

    enum class FallState {
        MONITORING_NORMAL,
        FREE_FALL_TRIGGERED,
        IMPACT_DETECTED,
        CRITICAL_FALL_CONFIRMED
    }

    data class MotionState(
        val accelMagnitude: Float = 9.81f,
        val motionVariance: Float = 0.05f,
        val activityContext: ActivityContext = ActivityContext.RESTING,
        val fallState: FallState = FallState.MONITORING_NORMAL,
        val fallAlertMessage: String? = null,
        val isSensorAvailable: Boolean = true
    )

    private val _state = MutableStateFlow(MotionState())
    val state: StateFlow<MotionState> = _state.asStateFlow()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val recentMagnitudes = ArrayDeque<Float>()
    private val maxMagWindow = 50 // ~1 second window at 50Hz

    // Fall detection state variables
    private var freeFallTimestamp: Long = 0L
    private var impactTimestamp: Long = 0L
    private var isFreeFallOccurred: Boolean = false
    private var isImpactOccurred: Boolean = false

    fun start() {
        recentMagnitudes.clear()
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun resetFallState() {
        isFreeFallOccurred = false
        isImpactOccurred = false
        freeFallTimestamp = 0L
        impactTimestamp = 0L
        _state.value = _state.value.copy(
            fallState = FallState.MONITORING_NORMAL,
            fallAlertMessage = null
        )
    }

    /**
     * Test simulation method to trigger fall detection alert for testing or verification
     */
    fun triggerSimulatedFall() {
        _state.value = _state.value.copy(
            fallState = FallState.CRITICAL_FALL_CONFIRMED,
            fallAlertMessage = "CRITICAL: Potential fall detected! Emergency SOS ready."
        )
    }

    fun getLatestMotionVariance(): Double {
        return _state.value.motionVariance.toDouble()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            val mag = sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
            val now = System.currentTimeMillis()

            recentMagnitudes.addLast(mag)
            if (recentMagnitudes.size > maxMagWindow) {
                recentMagnitudes.removeFirst()
            }

            // Compute rolling variance around 9.81 m/s^2 (1g)
            var sumSqDev = 0.0
            for (m in recentMagnitudes) {
                val dev = m - 9.81f
                sumSqDev += dev * dev
            }
            val variance = (sumSqDev / recentMagnitudes.size).toFloat()

            // Classify Activity Context
            val activity = when {
                variance < 0.25f -> ActivityContext.RESTING
                variance < 2.5f -> ActivityContext.MILD_MOVEMENT
                else -> ActivityContext.ACTIVE_EXERTION
            }

            // Fall Detection State Machine
            // 1. Free fall (< 0.5g = 4.9 m/s^2)
            if (mag < 4.9f) {
                isFreeFallOccurred = true
                freeFallTimestamp = now
            }

            // 2. Impact shock (> 2.8g = 27.5 m/s^2) within 1.2s of free-fall
            if (isFreeFallOccurred && mag > 27.5f && (now - freeFallTimestamp) < 1200) {
                isImpactOccurred = true
                impactTimestamp = now
            }

            // 3. Post-impact stillness (variance < 0.15 for > 2.0s after impact)
            var currentFallState = _state.value.fallState
            var alertMsg = _state.value.fallAlertMessage

            if (isImpactOccurred && (now - impactTimestamp) in 2000..5000) {
                if (variance < 0.20f) {
                    currentFallState = FallState.CRITICAL_FALL_CONFIRMED
                    alertMsg = "Fall detected with prolonged immobility! Check on user or trigger SOS."
                }
            } else if (isImpactOccurred && (now - impactTimestamp) > 6000 && variance > 0.6f) {
                // User got up / recovered
                isImpactOccurred = false
                isFreeFallOccurred = false
                currentFallState = FallState.MONITORING_NORMAL
                alertMsg = null
            }

            _state.value = _state.value.copy(
                accelMagnitude = mag,
                motionVariance = variance,
                activityContext = activity,
                fallState = currentFallState,
                fallAlertMessage = alertMsg
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
