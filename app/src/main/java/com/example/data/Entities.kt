package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vital_measurements")
data class VitalMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Int,
    val hrvSdnn: Double,
    val hrvRmssd: Double,
    val hrvPnn50: Double,
    val spo2: Int,
    val respirationRate: Int,
    val signalConfidence: Double,
    val riskScore: Int, // 0 - 100 overall vitality score
    val heatStressLevel: String, // NORMAL, ELEVATED, HIGH, CRITICAL
    val cardioRiskFlag: Boolean,
    val respiratoryRiskFlag: Boolean,
    val activityContext: String = "RESTING",
    val ambientTempCelsius: Float = 28f,
    val notes: String = ""
)

@Entity(tableName = "hazard_alerts")
data class HazardAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "HEAT_WAVE", "FLOOD_ALERT", "CYCLONE_WARNING", "SEVERE_AQI"
    val severity: String, // "ADVISORY", "WARNING", "SEVERE", "EXTREME"
    val location: String = "India Region",
    val description: String,
    val precautions: String,
    val recommendedIntervalMinutes: Int = 30,
    val timestamp: Long = System.currentTimeMillis(),
    val isCachedOffline: Boolean = true
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Companion User",
    val age: Int = 32,
    val baselineHr: Double = 72.0,
    val baselineRmssd: Double = 42.0,
    val baselineSpo2: Double = 98.0,
    val baselineRr: Double = 16.0,
    val spo2CalibrationA: Double = 102.0,
    val spo2CalibrationB: Double = 14.5,
    val isSpo2Calibrated: Boolean = false,
    val referenceOximeterName: String = "Standard Pulse Oximeter",
    val emergencyContactName: String = "Emergency Contact",
    val emergencyContactPhone: String = "112",
    val selectedLanguage: String = "en", // "en", "hi"
    val privacyConsentGiven: Boolean = true
)
