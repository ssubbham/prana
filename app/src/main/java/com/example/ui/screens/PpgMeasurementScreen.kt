package com.example.ui.screens

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.AppLanguage
import com.example.sensors.PpgCameraManager
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.PpgWaveformCanvas
import com.example.ui.components.VitalCard
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.BotanicalGreenContainer
import com.example.ui.theme.BotanicalGreenLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.EarthAmberContainer
import com.example.ui.theme.EmergencyContainer
import com.example.ui.theme.EmergencyCoral
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTealLight
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalSageContainer
import com.example.ui.theme.NaturalSageLight
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalSurfaceVariant
import com.example.ui.theme.OnEmergencyContainer
import com.example.ui.theme.RiverBlue
import com.example.ui.theme.TextEarthMuted
import com.example.ui.theme.TextEarthPrimary
import com.example.ui.theme.TextEarthSecondary
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VitalAmber
import com.example.ui.theme.VitalBlue
import com.example.ui.theme.VitalGreen
import com.example.ui.theme.VitalRed

@Composable
fun PpgMeasurementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val ppgState by viewModel.ppgManager.state.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPpgMeasurement()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
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
                    viewModel.stopPpgMeasurement()
                    onBack()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NaturalSurface)
                    .border(1.dp, NaturalBorder, CircleShape)
                    .testTag("btn_back_ppg")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextEarthPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (language == "hi") "कैमरा PPG पल्स मॉनिटरिंग" else "Camera PPG Pulse Oximetry",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextEarthPrimary
                    )
                )
                Text(
                    text = "Photoplethysmography (CameraX Real-Time Preview • 30 Hz)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BotanicalGreen,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Small Window on Screen: Live Optical Lens & Finger Coverage Viewfinder with CameraX PreviewView
        OpticalCoverageWindow(
            ppgState = ppgState,
            previewView = previewView,
            language = language,
            modifier = Modifier.testTag("optical_coverage_window")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center Live Pulse & Progress Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(NaturalSageContainer)
                .border(4.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live PPG Signal Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BotanicalGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE PPG SIGNAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextEarthSecondary,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Progress with Live BPM inside
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressFraction = (ppgState.progressSeconds / ppgState.targetDurationSeconds).coerceIn(0f, 1f)

                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = BotanicalGreen,
                        trackColor = BotanicalGreen.copy(alpha = 0.15f),
                        strokeWidth = 10.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (ppgState.isRecording && ppgState.isFingerCovered) EmergencyCoral else TextEarthMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ppgState.liveBpm?.toString() ?: "--",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Light,
                                color = TextEarthPrimary,
                                fontSize = 44.sp,
                                letterSpacing = (-1).sp
                            )
                        )
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextEarthSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (ppgState.isRecording) {
                        if (ppgState.isFingerCovered) "${(ppgState.targetDurationSeconds - ppgState.progressSeconds).toInt()}s remaining • Hold still"
                        else "Cover camera completely to continue pulse acquisition"
                    } else "Place index fingertip firmly over rear camera & flash",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextEarthSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (ppgState.progressSeconds / ppgState.targetDurationSeconds).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BotanicalGreen,
                    trackColor = BotanicalGreen.copy(alpha = 0.15f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live PPG Pulsatile Waveform Monitor
        Text(
            text = if (language == "hi") "पल्सेटाइल रक्त प्रवाह तरंग (Live Filtered AC)" else "Pulsatile Blood Volume Waveform (Live Filtered AC)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextEarthPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        PpgWaveformCanvas(
            waveform = ppgState.liveWaveform,
            lineColor = if (ppgState.isFingerCovered) BotanicalGreen else EarthAmber
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start / Stop Control Buttons
        if (!ppgState.isRecording) {
            Button(
                onClick = { viewModel.startPpgMeasurement(lifecycleOwner, previewView.surfaceProvider) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("btn_start_measurement"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotanicalGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "hi") "माप शुरू करें (25 सेकंड)" else "Start 25s Vital Capture",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.stopPpgMeasurement() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("btn_cancel_measurement"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmergencyCoral
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(EmergencyCoral, EmergencyCoral))
                )
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "hi") "माप रोकें" else "Cancel Measurement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Error message card
        if (ppgState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmergencyContainer)
                    .border(1.dp, EmergencyCoral.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = EmergencyCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ppgState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnEmergencyContainer,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Completed Results Panel
        if (ppgState.measurementComplete && ppgState.finalResult != null) {
            val res = ppgState.finalResult!!
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (language == "hi") "माप परिणाम (Verified Parameters)" else "Captured Biometric Results",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextEarthPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Result Metrics Grid
            VitalCard(
                title = "Heart Rate",
                value = "${res.heartRateBpm}",
                unit = "BPM",
                icon = Icons.Default.Favorite,
                accentColor = EmergencyCoral,
                statusText = "Calculated via IBI",
                isNormal = res.heartRateBpm in 55..100
            )
            Spacer(modifier = Modifier.height(8.dp))

            VitalCard(
                title = "HRV (RMSSD / SDNN)",
                value = "${res.hrvRmssdMs.toInt()} / ${res.hrvSdnnMs.toInt()}",
                unit = "ms",
                icon = Icons.Default.CheckCircle,
                accentColor = BotanicalGreen,
                statusText = "pNN50: ${res.hrvPnn50Percent.toInt()}%",
                isNormal = res.hrvRmssdMs >= 25.0
            )
            Spacer(modifier = Modifier.height(8.dp))

            VitalCard(
                title = "Estimated SpO2",
                value = "${res.estimatedSpo2Percent}",
                unit = "%",
                icon = Icons.Default.CheckCircle,
                accentColor = RiverBlue,
                statusText = if (res.isCalibratedSpo2) "Empirically Calibrated" else "Standard Baseline Proxy",
                isNormal = res.estimatedSpo2Percent >= 94
            )
            Spacer(modifier = Modifier.height(8.dp))

            VitalCard(
                title = "Respiration Rate",
                value = "${res.respirationRateBpm}",
                unit = "breaths/min",
                icon = Icons.Default.CheckCircle,
                accentColor = EarthAmber,
                statusText = "RIIV Envelope",
                isNormal = res.respirationRateBpm in 12..22
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("btn_done_results"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotanicalGreen
                )
            ) {
                Text(
                    text = if (language == "hi") "सहेजें और डैशबोर्ड पर लौटें" else "Save & Return to Dashboard",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Live Optical Lens & Finger Coverage Viewfinder Component.
 * Gives immediate visual feedback with CameraX PreviewView real-time hardware stream,
 * center biometric sensing reticle, and dynamic coverage meter.
 */
@Composable
fun OpticalCoverageWindow(
    ppgState: PpgCameraManager.LivePpgState,
    previewView: PreviewView? = null,
    language: String,
    modifier: Modifier = Modifier
) {
    val coverage = ppgState.coveragePercent
    val isGoodCoverage = coverage >= 70
    val isModerateCoverage = coverage in 40..69

    val statusColor = when {
        !ppgState.isRecording -> TextEarthMuted
        isGoodCoverage -> BotanicalGreen
        isModerateCoverage -> EarthAmber
        else -> EmergencyCoral
    }

    val statusBg = when {
        !ppgState.isRecording -> NaturalSurfaceVariant
        isGoodCoverage -> BotanicalGreenContainer
        isModerateCoverage -> EarthAmberContainer
        else -> EmergencyContainer
    }

    val statusText = when {
        !ppgState.isRecording -> if (language == "hi") "कैमरा स्टैंडबाय" else "Camera Inactive"
        coverage >= 85 -> if (language == "hi") "पूर्ण कवरेज (100% संपर्क)" else "Full Optical Coverage"
        isGoodCoverage -> if (language == "hi") "अच्छा संपर्क (स्थिर रखें)" else "Good Contact (Hold Steady)"
        isModerateCoverage -> if (language == "hi") "आंशिक कवरेज (उंगली खिसकाएं)" else "Partial Coverage (Slide Finger)"
        else -> if (language == "hi") "लेंस खुला है (उंगली रखें)" else "Lens Exposed (Place Fingertip)"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "hi") "कैमरा कवरेज विंडो" else "Camera Lens & Finger Viewfinder",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextEarthPrimary
                        )
                    )
                }

                // Coverage Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (ppgState.isRecording) "$coverage% Covered" else "Ready",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The Viewfinder Window with CameraX PreviewView
                Box(
                    modifier = Modifier
                        .size(width = 132.dp, height = 100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141712))
                        .border(
                            width = 2.dp,
                            color = if (ppgState.isRecording) statusColor else NaturalBorder,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (ppgState.isRecording) {
                        if (previewView != null) {
                            // Real-time CameraX hardware preview surface
                            AndroidView(
                                factory = { previewView },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (ppgState.previewBitmap != null) {
                            Image(
                                bitmap = ppgState.previewBitmap!!.asImageBitmap(),
                                contentDescription = "Live camera lens optical feed",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Center Biometric Target Reticle Overlay (ROI optical sensing zone)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        )

                        // Top-left live CameraX tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isGoodCoverage) BotanicalGreen else EmergencyCoral)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "CAMERAX FEED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        // Inactive / Standby state placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CameraX Preview",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Real-time Optical Analysis & Telemetry
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (ppgState.isRecording) {
                            if (isGoodCoverage) "Transmitted red wavelength confirmed. Keep gentle pressure."
                            else "Slide finger so no ambient light leaks past the camera aperture."
                        } else {
                            "Real-time CameraX preview verifies fingertip completely covers the lens."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextEarthSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coverage Progress Bar
                    LinearProgressIndicator(
                        progress = { (coverage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = statusColor,
                        trackColor = NaturalSurfaceVariant
                    )

                    if (ppgState.isRecording) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "R: ${ppgState.redMean.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = EmergencyCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "G: ${ppgState.greenMean.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = BotanicalGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "B: ${ppgState.blueMean.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = RiverBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

