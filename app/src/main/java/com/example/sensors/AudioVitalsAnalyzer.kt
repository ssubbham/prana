package com.example.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.dsp.TfLiteModelBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Microphone-based Audio Respiratory & Cough Analyzer.
 *
 * Implements:
 * - 16 kHz Mono PCM audio capture
 * - Cough and wheeze event detector (using audio energy bursts and spectral feature signatures)
 * - Audio-derived breathing cadence estimation
 * - Live decibel and waveform audio meter
 */
class AudioVitalsAnalyzer(private val context: Context) {

    data class AudioState(
        val isListening: Boolean = false,
        val decibelLevel: Float = 0f,
        val coughCount: Int = 0,
        val lastCoughConfidence: Float = 0f,
        val isWheezingDetected: Boolean = false,
        val estimatedBreathCadenceBpm: Int = 16,
        val audioWaveform: List<Float> = emptyList(),
        val feedback: String = "Tap start to analyze breathing and cough acoustic patterns."
    )

    private val _state = MutableStateFlow(AudioState())
    val state: StateFlow<AudioState> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val tfLiteBridge = TfLiteModelBridge(context)

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_state.value.isListening) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _state.value = _state.value.copy(
                    isListening = false,
                    feedback = "Audio hardware initialization failed. Check microphone permission."
                )
                return
            }

            audioRecord?.startRecording()
            _state.value = _state.value.copy(
                isListening = true,
                coughCount = 0,
                feedback = "Listening to respiratory sounds... Breathe naturally near microphone."
            )

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val shortBuffer = ShortArray(bufferSize / 2)
                val waveformHistory = ArrayDeque<Float>()
                var coughCounter = 0
                val breathEnergyWindows = ArrayDeque<Double>()

                while (isActive && _state.value.isListening) {
                    val read = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: -1
                    if (read > 0) {
                        // Compute RMS and Decibels
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            sumSq += shortBuffer[i] * shortBuffer[i]
                        }
                        val rms = sqrt(sumSq / read)
                        val db = (20.0 * log10((rms.coerceAtLeast(1.0) / 32767.0))).toFloat() + 90f

                        // Audio waveform normalization
                        val waveSample = (rms / 4000.0).toFloat().coerceIn(0f, 1f)
                        waveformHistory.addLast(waveSample)
                        if (waveformHistory.size > 50) {
                            waveformHistory.removeFirst()
                        }

                        // Classify cough/wheeze
                        val coughResult = tfLiteBridge.classifyRespiratoryAudio(shortBuffer, sampleRate)
                        if (coughResult.isCoughDetected) {
                            coughCounter++
                        }

                        // Track slow breathing periodicity (energy modulation)
                        breathEnergyWindows.addLast(rms)
                        if (breathEnergyWindows.size > 150) {
                            breathEnergyWindows.removeFirst()
                        }

                        var estCadence = 16
                        if (breathEnergyWindows.size >= 80) {
                            // Find peaks in energy envelope
                            var envelopePeaks = 0
                            val avgEnergy = breathEnergyWindows.average()
                            for (k in 1 until breathEnergyWindows.size - 1) {
                                if (breathEnergyWindows[k] > avgEnergy * 1.3 &&
                                    breathEnergyWindows[k] > breathEnergyWindows[k - 1] &&
                                    breathEnergyWindows[k] >= breathEnergyWindows[k + 1]
                                ) {
                                    envelopePeaks++
                                }
                            }
                            if (envelopePeaks >= 2) {
                                estCadence = (envelopePeaks * 12).coerceIn(10, 30)
                            }
                        }

                        _state.value = _state.value.copy(
                            decibelLevel = db.coerceIn(20f, 100f),
                            coughCount = coughCounter,
                            lastCoughConfidence = coughResult.confidence,
                            isWheezingDetected = coughResult.isWheezeDetected,
                            estimatedBreathCadenceBpm = estCadence,
                            audioWaveform = waveformHistory.toList(),
                            feedback = if (coughCounter > 0) {
                                "$coughCounter cough event(s) identified. Evaluating acoustic signatures."
                            } else {
                                "Normal steady respiratory background audio detected."
                            }
                        )
                    }
                    delay(80)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioVitalsAnalyzer", "Error recording audio: ${e.message}")
            _state.value = _state.value.copy(
                isListening = false,
                feedback = "Audio recording error: ${e.message}"
            )
        }
    }

    fun stopListening() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e("AudioVitalsAnalyzer", "Error releasing audio: ${e.message}")
        }
        _state.value = _state.value.copy(
            isListening = false,
            feedback = "Audio analysis paused."
        )
    }
}
