package com.example.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.HazardAlert
import com.example.data.VitalMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Disaster Resilience Layer & Emergency SOS Module.
 *
 * Implements:
 * - Local offline hazard cache for Indian meteorological & disaster scenarios (Heat Waves, Cyclones, Floods, Severe AQI)
 * - Dynamic monitoring frequency recommendations
 * - One-Tap Offline SOS SMS generator embedding exact GPS coordinates (Google Maps link) + last recorded vitals
 */
class DisasterManager(private val context: Context) {

    data class DisasterState(
        val activeHazards: List<HazardAlert> = emptyList(),
        val currentLatitude: Double? = 20.5937,
        val currentLongitude: Double? = 78.9629,
        val locationName: String = "India Disaster Grid",
        val emergencyContactNumber: String = "112",
        val emergencyContactName: String = "National Emergency Support / Family",
        val isOfflineMode: Boolean = true
    )

    private val _state = MutableStateFlow(DisasterState())
    val state: StateFlow<DisasterState> = _state.asStateFlow()

    init {
        loadDefaultCachedHazards()
    }

    private fun loadDefaultCachedHazards() {
        val initialAlerts = listOf(
            HazardAlert(
                id = 1,
                title = "IMD Severe Heatwave Alert",
                category = "HEAT_WAVE",
                severity = "SEVERE",
                location = "Northern & Central Plains",
                description = "Day temperatures exceeding 43°C with intense solar radiation and dry winds.",
                precautions = "Check HR & hydration index every 30 mins. Consume ORS/lemon water. Avoid direct sun between 11 AM - 4 PM.",
                recommendedIntervalMinutes = 30,
                isCachedOffline = true
            ),
            HazardAlert(
                id = 2,
                title = "CPCB Air Quality Index: Severe (AQI 380)",
                category = "SEVERE_AQI",
                severity = "WARNING",
                location = "Urban Metro Zones",
                description = "High PM2.5 concentrations causing respiratory irritation.",
                precautions = "Perform acoustic cough checks. Use N95 mask outdoors. Monitor SpO2 and respiration rate regularly.",
                recommendedIntervalMinutes = 60,
                isCachedOffline = true
            ),
            HazardAlert(
                id = 3,
                title = "NDMA Monsoon Flood Advisory",
                category = "FLOOD_ALERT",
                severity = "ADVISORY",
                location = "Coastal & River Basin Areas",
                description = "Waterlogging and disrupted hospital access. Maintain emergency power backup.",
                precautions = "Keep phone charged, save emergency contact numbers, keep offline vitals log ready.",
                recommendedIntervalMinutes = 120,
                isCachedOffline = true
            )
        )
        _state.value = _state.value.copy(activeHazards = initialAlerts)
    }

    fun updateLocation(lat: Double, lng: Double, locName: String = "Current Device GPS") {
        _state.value = _state.value.copy(
            currentLatitude = lat,
            currentLongitude = lng,
            locationName = locName
        )
    }

    fun updateEmergencyContact(name: String, phone: String) {
        _state.value = _state.value.copy(
            emergencyContactName = name,
            emergencyContactNumber = phone
        )
    }

    /**
     * Generates and triggers offline SMS intent with GPS location + latest vitals.
     */
    fun triggerEmergencySms(lastVitals: VitalMeasurement?, fallOccurred: Boolean = false): Intent {
        val lat = _state.value.currentLatitude ?: 20.5937
        val lng = _state.value.currentLongitude ?: 78.9629
        val mapsLink = "https://maps.google.com/?q=$lat,$lng"

        val hrStr = lastVitals?.let { "${it.heartRate} BPM" } ?: "N/A"
        val spo2Str = lastVitals?.let { "${it.spo2}%" } ?: "N/A"
        val rrStr = lastVitals?.let { "${it.respirationRate} Br/m" } ?: "N/A"
        val stressStr = lastVitals?.heatStressLevel ?: "Normal"

        val fallStatus = if (fallOccurred) "⚠️ CRITICAL: Hard Fall & Immobility Detected!" else "Manual SOS Alert"

        val messageBody = """
            🚨 EMERGENCY MEDICAL & RESILIENCE ALERT (PranaHealth)
            Status: $fallStatus
            Location: $mapsLink (Lat: $lat, Lng: $lng)
            Latest Vitals:
            - Heart Rate: $hrStr
            - SpO2: $spo2Str
            - Respiration: $rrStr
            - Heat Strain: $stressStr
            Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
            Please send immediate assistance!
        """.trimIndent()

        val recipient = _state.value.emergencyContactNumber
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$recipient")
            putExtra("sms_body", messageBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }
}
