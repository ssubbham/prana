package com.example.health

import com.example.data.UserProfile
import com.example.data.VitalMeasurement
import com.example.dsp.VitalsCalculator
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * On-Device AI Health Risk Assessment & Baseline Anomaly Detection Engine.
 *
 * Implements:
 * - Multi-sensor physiological fusion (HR, HRV, SpO2, Respiration Rate, Ambient Heat)
 * - Heat Stress & Dehydration Risk Detector
 * - Cardiovascular strain & arrhythmia irregularity flag
 * - Respiratory Distress Index
 * - Personalized Z-score anomaly tracking against rolling user baseline
 * - Daily Vitality & Resilience Score (0 to 100)
 */
object HealthRiskEngine {

    data class RiskEvaluation(
        val overallVitalityScore: Int, // 0 to 100
        val heatStressLevel: HeatStressLevel,
        val cardioRiskFlag: Boolean,
        val cardioRiskDescription: String?,
        val respiratoryRiskFlag: Boolean,
        val respiratoryRiskDescription: String?,
        val isBaselineAnomaly: Boolean,
        val baselineAnomalyReason: String?,
        val summaryInsight: String,
        val actionableAdvice: List<String>
    )

    enum class HeatStressLevel {
        NORMAL,
        ELEVATED,
        HIGH,
        CRITICAL
    }

    fun evaluateVitals(
        vitals: VitalsCalculator.VitalsResult,
        ambientTempC: Float = 32f,
        activityContext: String = "RESTING",
        profile: UserProfile? = null,
        recentHistory: List<VitalMeasurement> = emptyList()
    ): RiskEvaluation {
        val hr = vitals.heartRateBpm
        val rmssd = vitals.hrvRmssdMs
        val sdnn = vitals.hrvSdnnMs
        val spo2 = vitals.estimatedSpo2Percent
        val rr = vitals.respirationRateBpm

        val baselineHr = profile?.baselineHr ?: 72.0
        val baselineRmssd = profile?.baselineRmssd ?: 40.0
        val isResting = activityContext.equals("RESTING", ignoreCase = true)

        // 1. Heat Stress Evaluation
        // Heat stress manifests as tachycardia + reduced parasympathetic HRV (low RMSSD) + elevated ambient temp
        val heatStress = when {
            ambientTempC >= 40f && hr > (baselineHr + 20) && rmssd < 22.0 -> HeatStressLevel.CRITICAL
            ambientTempC >= 36f && hr > (baselineHr + 14) && rmssd < 28.0 -> HeatStressLevel.HIGH
            ambientTempC >= 32f && hr > (baselineHr + 8) -> HeatStressLevel.ELEVATED
            else -> HeatStressLevel.NORMAL
        }

        // 2. Cardiovascular Stress & Irregularity Flag
        var isCardioRisk = false
        var cardioDesc: String? = null

        if (isResting) {
            if (hr > 105) {
                isCardioRisk = true
                cardioDesc = "Resting tachycardia detected (HR $hr BPM). Ensure hydration and rest in a cool area."
            } else if (hr < 48) {
                isCardioRisk = true
                cardioDesc = "Resting bradycardia detected (HR $hr BPM)."
            } else if (rmssd < 15.0 && sdnn < 20.0) {
                isCardioRisk = true
                cardioDesc = "Significantly reduced autonomic heart rate variability (RMSSD ${rmssd.toInt()}ms)."
            }
        }

        // 3. Respiratory Risk
        var isRespRisk = false
        var respDesc: String? = null

        if (spo2 < 93) {
            isRespRisk = true
            respDesc = "Estimated blood oxygen level is low ($spo2%). Recheck in a calm posture."
        } else if (rr > 24 && isResting) {
            isRespRisk = true
            respDesc = "Tachypnea: Resting breathing rate is elevated ($rr breaths/min)."
        }

        // 4. Personalized Z-score Baseline Anomaly
        var isAnomaly = false
        var anomalyReason: String? = null

        if (recentHistory.size >= 3) {
            val pastHrs = recentHistory.map { it.heartRate.toDouble() }
            val meanHr = pastHrs.average()
            val stdHr = sqrt(pastHrs.map { (it - meanHr) * (it - meanHr) }.average()).coerceAtLeast(4.0)

            val zScoreHr = abs(hr - meanHr) / stdHr
            if (zScoreHr > 2.2) {
                isAnomaly = true
                anomalyReason = "Heart rate deviates significantly from your 7-day personal normal by ${zScoreHr.format(1)} standard deviations."
            }
        }

        // 5. Compute Vitality Score (0-100)
        var score = 100
        if (hr !in 60..90 && isResting) score -= 15
        if (rmssd < 25.0) score -= 15
        if (spo2 < 95) score -= (95 - spo2) * 5
        if (rr !in 12..20) score -= 10
        if (heatStress == HeatStressLevel.HIGH) score -= 15
        if (heatStress == HeatStressLevel.CRITICAL) score -= 30
        score = score.coerceIn(25, 99)

        // Actionable guidance
        val adviceList = mutableListOf<String>()
        when (heatStress) {
            HeatStressLevel.CRITICAL -> {
                adviceList.add("Immediate cool-down required: move to shade, loosen clothing, drink electrolyte water.")
                adviceList.add("Avoid outdoor exposure. Prepare emergency contacts if dizziness persists.")
            }
            HeatStressLevel.HIGH -> {
                adviceList.add("High heat strain: drink 500ml oral rehydration fluid and reduce physical activity.")
            }
            HeatStressLevel.ELEVATED -> {
                adviceList.add("Elevated heat index: stay hydrated throughout the day.")
            }
            HeatStressLevel.NORMAL -> {
                adviceList.add("Autonomic and thermal vitals are balanced and steady.")
            }
        }

        if (isRespRisk) {
            adviceList.add("Airway check: ensure good ventilation, avoid dust/smoke exposure.")
        }
        if (adviceList.size < 2) {
            adviceList.add("Maintain regular hydration and scheduled vital monitoring.")
        }

        val summary = when {
            score >= 85 -> "Excellent physiological resilience. Core biometrics are stable."
            score >= 70 -> "Good overall status with mild environmental or fatigue load."
            score >= 50 -> "Elevated physical strain detected. Follow hydration and resting protocols."
            else -> "High risk alert: Autonomic indicators under significant stress. Rest immediately."
        }

        return RiskEvaluation(
            overallVitalityScore = score,
            heatStressLevel = heatStress,
            cardioRiskFlag = isCardioRisk,
            cardioRiskDescription = cardioDesc,
            respiratoryRiskFlag = isRespRisk,
            respiratoryRiskDescription = respDesc,
            isBaselineAnomaly = isAnomaly,
            baselineAnomalyReason = anomalyReason,
            summaryInsight = summary,
            actionableAdvice = adviceList
        )
    }

    private fun Double.format(decimals: Int): String = String.format("%.${decimals}f", this)
}
