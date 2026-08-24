package com.example

import com.example.data.UserProfile
import com.example.dsp.ButterworthFilter
import com.example.dsp.PeakDetector
import com.example.dsp.VitalsCalculator
import com.example.health.HealthRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class ExampleUnitTest {

    @Test
    fun testButterworthBandpassFiltering() {
        val fs = 30.0
        val lowCut = 0.7
        val highCut = 3.5
        val filter = ButterworthFilter(lowCut, highCut, fs)

        // Generate synthetic sine waves: 1.2 Hz (within passband, ~72 BPM) and 10 Hz (high frequency noise)
        val n = 300
        val rawInput = DoubleArray(n)
        for (i in 0 until n) {
            val t = i / fs
            val cleanSignal = sin(2.0 * PI * 1.2 * t)
            val noiseSignal = 0.5 * sin(2.0 * PI * 10.0 * t)
            rawInput[i] = cleanSignal + noiseSignal
        }

        val filteredOutput = filter.filtfilt(rawInput)

        // Verify filter produces valid, finite, bounded outputs
        assertTrue("Filter output should not contain NaN", filteredOutput.none { it.isNaN() })
        assertTrue("Filter output should attenuate high frequency noise", filteredOutput.maxOrNull()!! < 1.6)
    }

    @Test
    fun testPeakDetectorAndVitalsCalculation() {
        val fs = 30.0
        val hrHz = 1.2 // 72 BPM (~833ms IBI)
        val durationSec = 10.0
        val totalSamples = (durationSec * fs).toInt()

        val rawGreen = DoubleArray(totalSamples)
        val rawRed = DoubleArray(totalSamples)
        val filteredGreen = DoubleArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i / fs
            val ppgWave = sin(2.0 * PI * hrHz * t)
            rawGreen[i] = 120.0 + 8.0 * ppgWave
            rawRed[i] = 180.0 + 10.0 * ppgWave
            filteredGreen[i] = ppgWave
        }

        val peaks = PeakDetector.findPeaks(
            signal = filteredGreen,
            fs = fs
        )

        // For 10 seconds at 1.2 Hz, expect ~11-13 peaks
        assertTrue("Detected peaks count should match heart rate cadence (got ${peaks.size})", peaks.size in 9..14)

        val vitals = VitalsCalculator.calculate(
            filteredGreen = filteredGreen,
            rawRed = rawRed,
            rawGreen = rawGreen,
            peaks = peaks,
            fs = fs,
            spo2CalibrationA = 102.0,
            spo2CalibrationB = 14.5,
            isCustomCalibrated = false
        )

        assertNotNull("Vitals should compute successfully", vitals)
        assertTrue("Heart rate should be near 72 BPM (got ${vitals?.heartRateBpm})", vitals!!.heartRateBpm in 65..80)
        assertTrue("SpO2 percentage should be within physiological bounds", vitals.estimatedSpo2Percent in 88..100)
        assertTrue("Signal confidence should be high", vitals.signalConfidence > 0.5)
    }

    @Test
    fun testHealthRiskEngineHeatStressEvaluation() {
        val mockVitals = VitalsCalculator.VitalsResult(
            heartRateBpm = 115,
            hrvSdnnMs = 20.0,
            hrvRmssdMs = 18.0,
            hrvPnn50Percent = 4.0,
            estimatedSpo2Percent = 95,
            respirationRateBpm = 24,
            systolicUpstrokeMs = 110.0,
            signalConfidence = 0.90,
            isCalibratedSpo2 = true
        )

        val profile = UserProfile(
            baselineHr = 70.0,
            baselineRmssd = 45.0,
            baselineSpo2 = 98.0,
            baselineRr = 15.0
        )

        // Evaluate under severe Indian summer heatwave condition (44°C)
        val evaluation = HealthRiskEngine.evaluateVitals(
            vitals = mockVitals,
            ambientTempC = 44f,
            activityContext = "RESTING",
            profile = profile,
            recentHistory = emptyList()
        )

        assertEquals("Heat stress level should be CRITICAL under 44C with tachycardia and suppressed RMSSD", HealthRiskEngine.HeatStressLevel.CRITICAL, evaluation.heatStressLevel)
        assertTrue("Cardio risk flag should be raised for resting tachycardia at 44C", evaluation.cardioRiskFlag)
    }
}
