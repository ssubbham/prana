package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HazardAlert
import com.example.data.HealthRepository
import com.example.data.UserProfile
import com.example.data.VitalMeasurement
import com.example.dsp.VitalsCalculator
import com.example.health.DisasterManager
import com.example.health.HealthRiskEngine
import com.example.sensors.AudioVitalsAnalyzer
import com.example.sensors.MotionHazardDetector
import com.example.sensors.PpgCameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    PPG_MEASURE,
    AUDIO_RESPIRATORY,
    MOTION_FALL,
    DISASTER_RESILIENCE,
    HISTORY_TRENDS,
    CALIBRATION_SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HealthRepository(db.vitalsDao(), db.hazardAlertDao(), db.userProfileDao())

    val ppgManager = PpgCameraManager(application)
    val audioAnalyzer = AudioVitalsAnalyzer(application)
    val motionDetector = MotionHazardDetector(application)
    val disasterManager = DisasterManager(application)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _ambientTemperatureC = MutableStateFlow(34f)
    val ambientTemperatureC: StateFlow<Float> = _ambientTemperatureC.asStateFlow()

    private val _latestRiskEvaluation = MutableStateFlow<HealthRiskEngine.RiskEvaluation?>(null)
    val latestRiskEvaluation: StateFlow<HealthRiskEngine.RiskEvaluation?> = _latestRiskEvaluation.asStateFlow()

    val vitalsHistory: StateFlow<List<VitalMeasurement>> = repository.allVitals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestVital: StateFlow<VitalMeasurement?> = repository.latestVital
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hazardAlerts: StateFlow<List<HazardAlert>> = repository.allHazards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        motionDetector.start()

        // Link motion variance to PPG manager
        ppgManager.motionVarianceProvider = { motionDetector.getLatestMotionVariance() }

        // Initialize user profile in DB if not present
        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    name = "User",
                    age = 30,
                    baselineHr = 72.0,
                    baselineRmssd = 42.0,
                    baselineSpo2 = 98.0,
                    baselineRr = 16.0,
                    spo2CalibrationA = 102.0,
                    spo2CalibrationB = 14.5,
                    isSpo2Calibrated = false,
                    emergencyContactName = "Disaster Help / Family",
                    emergencyContactPhone = "112"
                )
            )
        }

        // Listen for PPG measurement completion
        viewModelScope.launch {
            ppgManager.state.collect { state ->
                if (state.measurementComplete && state.finalResult != null) {
                    processCompletedPpg(state.finalResult)
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun setAmbientTemperature(temp: Float) {
        _ambientTemperatureC.value = temp
    }

    fun startPpgMeasurement(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null,
        surfaceProvider: androidx.camera.core.Preview.SurfaceProvider? = null
    ) {
        val prof = userProfile.value
        if (prof != null) {
            ppgManager.spo2CalibrationA = prof.spo2CalibrationA
            ppgManager.spo2CalibrationB = prof.spo2CalibrationB
            ppgManager.isCustomCalibrated = prof.isSpo2Calibrated
        }
        ppgManager.startMeasurement(
            lifecycleOwner = lifecycleOwner,
            surfaceProvider = surfaceProvider,
            durationSeconds = 25f
        )
        vibrate(80)
    }

    fun startPpgMeasurement() {
        startPpgMeasurement(lifecycleOwner = null, surfaceProvider = null)
    }

    fun stopPpgMeasurement() {
        ppgManager.stopMeasurement()
    }

    private fun processCompletedPpg(vitals: VitalsCalculator.VitalsResult) {
        vibrate(200)
        viewModelScope.launch {
            val history = vitalsHistory.value
            val prof = userProfile.value
            val activity = motionDetector.state.value.activityContext.name

            val evaluation = HealthRiskEngine.evaluateVitals(
                vitals = vitals,
                ambientTempC = _ambientTemperatureC.value,
                activityContext = activity,
                profile = prof,
                recentHistory = history
            )
            _latestRiskEvaluation.value = evaluation

            val entity = VitalMeasurement(
                heartRate = vitals.heartRateBpm,
                hrvSdnn = vitals.hrvSdnnMs,
                hrvRmssd = vitals.hrvRmssdMs,
                hrvPnn50 = vitals.hrvPnn50Percent,
                spo2 = vitals.estimatedSpo2Percent,
                respirationRate = vitals.respirationRateBpm,
                signalConfidence = vitals.signalConfidence,
                riskScore = evaluation.overallVitalityScore,
                heatStressLevel = evaluation.heatStressLevel.name,
                cardioRiskFlag = evaluation.cardioRiskFlag,
                respiratoryRiskFlag = evaluation.respiratoryRiskFlag,
                activityContext = activity,
                ambientTempCelsius = _ambientTemperatureC.value,
                notes = evaluation.summaryInsight
            )

            repository.saveVital(entity)
        }
    }

    fun calibrateSpo2(refOximeterSpo2: Int, currentR: Double) {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfile()
            // Linear recalibration solving ref = a - b * R
            val newA = (refOximeterSpo2 + (14.5 * currentR)).coerceIn(98.0, 115.0)
            val updated = prof.copy(
                spo2CalibrationA = newA,
                spo2CalibrationB = 14.5,
                isSpo2Calibrated = true
            )
            repository.saveProfile(updated)
            ppgManager.spo2CalibrationA = newA
            ppgManager.isCustomCalibrated = true
        }
    }

    fun updateEmergencyContact(name: String, phone: String) {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfile()
            val updated = prof.copy(
                emergencyContactName = name,
                emergencyContactPhone = phone
            )
            repository.saveProfile(updated)
            disasterManager.updateEmergencyContact(name, phone)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllVitals()
        }
    }

    fun generateSosSms(): String {
        val lat = disasterManager.state.value.currentLatitude ?: 20.5937
        val lng = disasterManager.state.value.currentLongitude ?: 78.9629
        val mapsLink = "https://maps.google.com/?q=$lat,$lng"
        val lastVitals = latestVital.value
        val hrStr = lastVitals?.let { "${it.heartRate} BPM" } ?: "N/A"
        val spo2Str = lastVitals?.let { "${it.spo2}%" } ?: "N/A"
        val rrStr = lastVitals?.let { "${it.respirationRate} Br/m" } ?: "N/A"
        val stressStr = lastVitals?.heatStressLevel ?: "Normal"
        val isFall = motionDetector.state.value.fallState == MotionHazardDetector.FallState.CRITICAL_FALL_CONFIRMED
        val fallStatus = if (isFall) "⚠️ CRITICAL: Hard Fall & Immobility Detected!" else "Manual SOS Alert"

        return """
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
    }

    fun generateSosIntent(context: Context): Intent {
        val isFall = motionDetector.state.value.fallState == MotionHazardDetector.FallState.CRITICAL_FALL_CONFIRMED
        return disasterManager.triggerEmergencySms(latestVital.value, isFall)
    }

    private fun vibrate(ms: Long) {
        val app = getApplication<Application>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        ppgManager.stopMeasurement()
        audioAnalyzer.stopListening()
        motionDetector.stop()
    }
}
