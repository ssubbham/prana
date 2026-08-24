package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.PpgWaveformCanvas
import com.example.ui.components.VitalCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTealLight
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VitalAmber
import com.example.ui.theme.VitalBlue
import com.example.ui.theme.VitalGreen
import com.example.ui.theme.VitalRed

@Composable
fun AudioRespiratoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioState by viewModel.audioAnalyzer.state.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.audioAnalyzer.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.audioAnalyzer.stopListening()
                    onBack()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardBorder, CircleShape)
                    .testTag("btn_back_audio")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimaryDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (language == "hi") "ध्वनि श्वसन एवं खांसी विश्लेषण" else "Acoustic Respiratory Analysis",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Microphone 16kHz PCM • Coswara / COUGHVID ML",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MedicalTealLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (audioState.isListening) VitalBlue.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (audioState.isListening) VitalBlue else TextMutedDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (audioState.isListening) "Real-Time Acoustic Inference Running" else "Microphone Idle",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = audioState.feedback,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Intensity Meter (Decibels)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ambient Sound Intensity",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "${audioState.decibelLevel.toInt()} dB SPL",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VitalBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val dbFraction = (audioState.decibelLevel / 100f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { dbFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (audioState.decibelLevel > 80f) VitalRed else VitalBlue,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Audio Envelope Waveform
        Text(
            text = "Real-Time Acoustic Energy Waveform",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        PpgWaveformCanvas(
            waveform = audioState.audioWaveform,
            lineColor = VitalBlue,
            showGrid = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cough & Wheeze Detections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cough counter card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Coughs Detected",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${audioState.coughCount}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (audioState.coughCount > 0) VitalAmber else VitalGreen
                        )
                    )
                    Text(
                        text = if (audioState.coughCount > 0) "Acoustic burst matched" else "No paroxysms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Audio-Derived Cadence
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Audio Breath Cadence",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${audioState.estimatedBreathCadenceBpm}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MedicalTealLight
                        )
                    )
                    Text(
                        text = "Breaths/min (Acoustic)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wheeze status card
        VitalCard(
            title = "Acoustic Wheeze / Stridor Detector",
            value = if (audioState.isWheezingDetected) "FLAGGED" else "CLEAR",
            unit = "",
            icon = Icons.Default.Air,
            accentColor = if (audioState.isWheezingDetected) VitalRed else VitalGreen,
            statusText = if (audioState.isWheezingDetected) "Continuous High-Frequency Sound" else "Normal Breath Sounds",
            isNormal = !audioState.isWheezingDetected,
            subtitle = "Continuous 2D Mel spectrogram pattern classifier"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Control buttons
        if (!audioState.isListening) {
            Button(
                onClick = { viewModel.audioAnalyzer.startListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("btn_start_audio"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Acoustic Listening",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.audioAnalyzer.stopListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("btn_stop_audio"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VitalRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(VitalRed, VitalRed))
                )
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stop Acoustic Listening",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
