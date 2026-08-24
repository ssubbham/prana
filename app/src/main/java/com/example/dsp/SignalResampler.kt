package com.example.dsp

import kotlin.math.max
import kotlin.math.min

/**
 * Resamples non-uniform timestamped camera PPG frames onto a uniform time grid (fs = 30 Hz)
 * using Catmull-Rom cubic spline interpolation to preserve waveform curvature and derivatives.
 * Fixes hardware timing jitter (±5-15ms) across Android devices without step artifacts.
 */
class SignalResampler(val targetFs: Double = 30.0) {

    data class TimestampedSample(
        val timestampNanos: Long,
        val red: Double,
        val green: Double,
        val blue: Double
    )

    data class ResampledGrid(
        val timestampsMs: DoubleArray,
        val red: DoubleArray,
        val green: DoubleArray,
        val blue: DoubleArray,
        val actualDurationSeconds: Double
    )

    /**
     * Converts a collection of raw timestamped camera frames into uniformly sampled arrays using cubic spline.
     */
    fun resample(rawSamples: List<TimestampedSample>): ResampledGrid {
        if (rawSamples.size < 4) {
            return ResampledGrid(DoubleArray(0), DoubleArray(0), DoubleArray(0), DoubleArray(0), 0.0)
        }

        val t0 = rawSamples.first().timestampNanos
        val timesSec = DoubleArray(rawSamples.size) { (rawSamples[it].timestampNanos - t0) / 1_000_000_000.0 }
        val rVals = DoubleArray(rawSamples.size) { rawSamples[it].red }
        val gVals = DoubleArray(rawSamples.size) { rawSamples[it].green }
        val bVals = DoubleArray(rawSamples.size) { rawSamples[it].blue }

        val totalDuration = timesSec.last()
        val numUniformPoints = (totalDuration * targetFs).toInt()
        if (numUniformPoints <= 2) {
            return ResampledGrid(DoubleArray(0), DoubleArray(0), DoubleArray(0), DoubleArray(0), 0.0)
        }

        val stepSec = 1.0 / targetFs
        val outTimesMs = DoubleArray(numUniformPoints)
        val outRed = DoubleArray(numUniformPoints)
        val outGreen = DoubleArray(numUniformPoints)
        val outBlue = DoubleArray(numUniformPoints)

        var rawIdx = 0
        for (i in 0 until numUniformPoints) {
            val targetTime = i * stepSec
            outTimesMs[i] = targetTime * 1000.0

            while (rawIdx < timesSec.size - 2 && timesSec[rawIdx + 1] < targetTime) {
                rawIdx++
            }

            val p0 = (rawIdx - 1).coerceAtLeast(0)
            val p1 = rawIdx
            val p2 = (rawIdx + 1).coerceAtMost(timesSec.size - 1)
            val p3 = (rawIdx + 2).coerceAtMost(timesSec.size - 1)

            val tA = timesSec[p1]
            val tB = timesSec[p2]
            val t = if (tB > tA) ((targetTime - tA) / (tB - tA)).coerceIn(0.0, 1.0) else 0.0

            outRed[i] = catmullRom(rVals[p0], rVals[p1], rVals[p2], rVals[p3], t)
            outGreen[i] = catmullRom(gVals[p0], gVals[p1], gVals[p2], gVals[p3], t)
            outBlue[i] = catmullRom(bVals[p0], bVals[p1], bVals[p2], bVals[p3], t)
        }

        return ResampledGrid(outTimesMs, outRed, outGreen, outBlue, totalDuration)
    }

    /**
     * Catmull-Rom cubic interpolation between y1 and y2 given preceding y0 and succeeding y3.
     */
    private fun catmullRom(y0: Double, y1: Double, y2: Double, y3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            (2.0 * y1) +
            (-y0 + y2) * t +
            (2.0 * y0 - 5.0 * y1 + 4.0 * y2 - y3) * t2 +
            (-y0 + 3.0 * y1 - 3.0 * y2 + y3) * t3
        )
    }

    /**
     * Detrends signal by subtracting a rolling mean (1.5 - 2.0 second moving average window)
     * to eliminate low-frequency baseline wander without distorting AC pulsatile beats.
     */
    fun detrendRolling(signal: DoubleArray, windowSamples: Int = (targetFs * 1.8).toInt()): DoubleArray {
        if (signal.size < windowSamples) return DoubleArray(signal.size)
        val out = DoubleArray(signal.size)
        val halfWin = windowSamples / 2

        for (i in signal.indices) {
            val start = (i - halfWin).coerceAtLeast(0)
            val end = (i + halfWin).coerceAtMost(signal.size - 1)
            var sum = 0.0
            val count = end - start + 1
            for (k in start..end) {
                sum += signal[k]
            }
            out[i] = signal[i] - (sum / count)
        }
        return out
    }
}

