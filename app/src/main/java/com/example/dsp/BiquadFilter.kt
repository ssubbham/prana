package com.example.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Direct-Form-II-Transposed Biquad (Second-Order Section) filter.
 * Stable, low memory, zero-phase (filtfilt) capable for biomedical signals.
 */
data class BiquadSection(
    val b0: Double,
    val b1: Double,
    val b2: Double,
    val a0: Double = 1.0,
    val a1: Double,
    val a2: Double
) {
    // Normalized coefficients (where a0 = 1.0)
    private val nb0 = b0 / a0
    private val nb1 = b1 / a0
    private val nb2 = b2 / a0
    private val na1 = a1 / a0
    private val na2 = a2 / a0

    private var z1: Double = 0.0
    private var z2: Double = 0.0

    fun reset() {
        z1 = 0.0
        z2 = 0.0
    }

    fun filterSample(input: Double): Double {
        val output = nb0 * input + z1
        z1 = nb1 * input - na1 * output + z2
        z2 = nb2 * input - na2 * output
        return output
    }
}

/**
 * 4th-Order Butterworth Bandpass filter implemented as a 2-section Biquad SOS cascade.
 * Supports zero-phase bidirectional (filtfilt) filtering to eliminate phase distortion.
 */
class ButterworthFilter(
    val lowCutHz: Double = 0.7,
    val highCutHz: Double = 4.0,
    val samplingRateHz: Double = 30.0
) {
    private var sections: List<BiquadSection> = designBandpassSOS(lowCutHz, highCutHz, samplingRateHz)

    fun reset() {
        sections.forEach { it.reset() }
    }

    /**
     * Causal single-pass filtering (suitable for live streaming preview).
     */
    fun filterSample(sample: Double): Double {
        var out = sample
        for (sec in sections) {
            out = sec.filterSample(out)
        }
        return out
    }

    /**
     * Zero-phase bidirectional filtering (filtfilt).
     * Filters forward then backward to preserve exact peak locations and timing (vital for accurate HRV).
     */
    fun filtfilt(signal: DoubleArray): DoubleArray {
        if (signal.size < 8) return signal.copyOf()
        val temp = DoubleArray(signal.size)
        val result = DoubleArray(signal.size)

        // Forward pass
        val fwdSections = designBandpassSOS(lowCutHz, highCutHz, samplingRateHz)
        for (i in signal.indices) {
            var v = signal[i]
            for (sec in fwdSections) {
                v = sec.filterSample(v)
            }
            temp[i] = v
        }

        // Backward pass
        val bwdSections = designBandpassSOS(lowCutHz, highCutHz, samplingRateHz)
        for (i in signal.indices.reversed()) {
            var v = temp[i]
            for (sec in bwdSections) {
                v = sec.filterSample(v)
            }
            result[i] = v
        }

        return result
    }

    companion object {
        /**
         * Calculates 2-section SOS (4th order bandpass) Butterworth coefficients dynamically.
         */
        fun designBandpassSOS(fLow: Double, fHigh: Double, fs: Double): List<BiquadSection> {
            val safeFs = if (fs < 10.0) 30.0 else fs
            val wLow = 2.0 * PI * (fLow / safeFs)
            val wHigh = 2.0 * PI * (fHigh / safeFs)

            // Pre-warp frequencies for bilinear transform
            val warpedLow = 2.0 * tan(wLow / 2.0)
            val warpedHigh = 2.0 * tan(wHigh / 2.0)
            val bw = warpedHigh - warpedLow
            val w0sq = warpedLow * warpedHigh
            val w0 = sqrt(w0sq)

            // 2 conjugate pole pairs for Butterworth Q
            val qFactors = doubleArrayOf(0.54119610, 1.30656296)
            val list = mutableListOf<BiquadSection>()

            for (q in qFactors) {
                val alpha = bw / (2.0 * q)
                val b0 = bw / 2.0
                val b1 = 0.0
                val b2 = -bw / 2.0
                val a0 = 1.0 + alpha + w0sq / 4.0
                val a1 = -2.0 + w0sq / 2.0
                val a2 = 1.0 - alpha + w0sq / 4.0

                list.add(BiquadSection(b0, b1, b2, a0, a1, a2))
            }
            return list
        }

        /**
         * Low-pass filter for respiration envelope smoothing (0.1 - 0.5 Hz).
         */
        fun designRespirationSOS(fs: Double): List<BiquadSection> {
            return designBandpassSOS(0.1, 0.5, fs)
        }
    }
}
