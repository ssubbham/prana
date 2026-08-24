package com.example.dsp

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Signal Quality Index (SQI) gating module.
 * Evaluates Perfusion Index (PI), Spectral Welch PSD Band Ratio (0.7 - 4.0 Hz),
 * Finger Contact detection, and Accelerometer motion disturbance.
 */
object SignalQualityIndex {

    data class QualityEvaluation(
        val isFingerCovered: Boolean,
        val perfusionIndexPercent: Double,
        val spectralSqi: Double,
        val motionVariance: Double,
        val overallQualityScore: Double, // 0.0 to 1.0
        val isAcceptable: Boolean,
        val feedbackReason: String
    )

    /**
     * Checks if a fingertip is properly placed over the illuminated camera lens.
     * Fingertip transmuted red light dominates strongly: High Red DC mean, Green/Blue significantly lower.
     */
    fun checkFingerContact(redMean: Double, greenMean: Double, blueMean: Double): Boolean {
        // Red channel must be strong (typical > 50-70 on 0-255 scale) and significantly higher than blue
        return redMean > 45.0 && (redMean > (greenMean * 1.15)) && (redMean > (blueMean * 1.5))
    }

    /**
     * Computes Perfusion Index (PI): AC_amplitude / DC_amplitude * 100%.
     * Normal finger PPG has PI between 0.2% and 10%. PI < 0.2% indicates weak pulse or poor contact.
     */
    fun computePerfusionIndex(acSignal: DoubleArray, dcMean: Double): Double {
        if (dcMean <= 0.001 || acSignal.isEmpty()) return 0.0
        var maxVal = acSignal[0]
        var minVal = acSignal[0]
        for (v in acSignal) {
            if (v > maxVal) maxVal = v
            if (v < minVal) minVal = v
        }
        val acPeakToPeak = (maxVal - minVal) / 2.0
        return (acPeakToPeak / dcMean) * 100.0
    }

    /**
     * Estimates spectral SQI: Ratio of spectral power in cardiac band (0.7 - 4.0 Hz) vs total band (0.1 - 10.0 Hz).
     * Uses Discrete Fourier Transform power density estimate.
     */
    fun computeSpectralSqi(signal: DoubleArray, fs: Double = 30.0): Double {
        if (signal.size < 32) return 0.0
        val n = signal.size
        val maxFreqIdx = (10.0 * n / fs).toInt().coerceAtMost(n / 2)
        val cardiacLowIdx = (0.7 * n / fs).toInt().coerceAtLeast(1)
        val cardiacHighIdx = (4.0 * n / fs).toInt().coerceAtMost(n / 2)

        var totalPower = 0.0
        var cardiacPower = 0.0

        for (k in 1..maxFreqIdx) {
            var re = 0.0
            var im = 0.0
            for (t in 0 until n) {
                val angle = 2.0 * PI * k * t / n
                re += signal[t] * cos(angle)
                im -= signal[t] * sin(angle)
            }
            val power = (re * re + im * im)
            totalPower += power
            if (k in cardiacLowIdx..cardiacHighIdx) {
                cardiacPower += power
            }
        }

        if (totalPower <= 1e-9) return 0.0
        return (cardiacPower / totalPower).coerceIn(0.0, 1.0)
    }

    /**
     * Full evaluation for an 8s to 25s window.
     */
    fun evaluateWindow(
        rawRed: DoubleArray,
        rawGreen: DoubleArray,
        rawBlue: DoubleArray,
        filteredGreen: DoubleArray,
        motionVariance: Double,
        fs: Double = 30.0
    ): QualityEvaluation {
        if (rawRed.isEmpty()) {
            return QualityEvaluation(false, 0.0, 0.0, motionVariance, 0.0, false, "No signal data")
        }

        val rMean = rawRed.average()
        val gMean = rawGreen.average()
        val bMean = rawBlue.average()

        val isCovered = checkFingerContact(rMean, gMean, bMean)
        if (!isCovered) {
            return QualityEvaluation(
                isFingerCovered = false,
                perfusionIndexPercent = 0.0,
                spectralSqi = 0.0,
                motionVariance = motionVariance,
                overallQualityScore = 0.0,
                isAcceptable = false,
                feedbackReason = "Place finger firmly over rear camera lens & flashlight"
            )
        }

        val pi = computePerfusionIndex(filteredGreen, gMean)
        val spectralSqi = computeSpectralSqi(filteredGreen, fs)

        var score = 0.0
        if (pi >= 0.2) score += 0.35
        if (spectralSqi >= 0.55) score += 0.45
        if (motionVariance < 0.4) score += 0.20

        val acceptable = isCovered && (pi >= 0.15) && (spectralSqi >= 0.50) && (motionVariance < 0.8)

        val reason = when {
            motionVariance >= 0.8 -> "Excessive phone movement detected. Hold still."
            pi < 0.15 -> "Weak pulse signal. Adjust finger pressure gently."
            spectralSqi < 0.50 -> "Noisy optical signal. Keep finger steady over flash."
            else -> "Signal locked and clear"
        }

        return QualityEvaluation(
            isFingerCovered = isCovered,
            perfusionIndexPercent = pi,
            spectralSqi = spectralSqi,
            motionVariance = motionVariance,
            overallQualityScore = score.coerceIn(0.0, 1.0),
            isAcceptable = acceptable,
            feedbackReason = reason
        )
    }
}
