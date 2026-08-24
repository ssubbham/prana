package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.sensors.MotionHazardDetector
import com.example.ui.MainViewModel
import com.example.ui.components.VitalCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MedicalTealLight
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VitalAmber
import com.example.ui.theme.VitalGreen
import com.example.ui.theme.VitalRed

@Composable
fun MotionFallScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionState by viewModel.motionDetector.state.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val isFallAlert = motionState.fallState == MotionHazardDetector.FallState.CRITICAL_FALL_CONFIRMED

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
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardBorder, CircleShape)
                    .testTag("btn_back_motion")
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
                    text = if (language == "hi") "गति एवं गिरावट पहचान (Fall Sentinel)" else "Motion & Fall Sentinel",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Tri-Axial Accel & Gyro • 3-Phase Fall Machine",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MedicalTealLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Critical Fall Alert Banner (if triggered)
        AnimatedVisibility(visible = isFallAlert) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VitalRed.copy(alpha = 0.2f))
                    .border(1.5.dp, VitalRed, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = VitalRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CRITICAL: Fall & Immobility Detected!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = VitalRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = motionState.fallAlertMessage ?: "High impact followed by stillness registered. Emergency SOS prepared.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerSos() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = VitalRed)
                        ) {
                            Icon(imageVector = Icons.Default.Emergency, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send SOS SMS")
                        }
                        OutlinedButton(
                            onClick = { viewModel.motionDetector.resetFallState() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("I'm Okay (Dismiss)")
                        }
                    }
                }
            }
        }

        if (isFallAlert) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live G-Force & Variance Monitor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inertial Acceleration Magnitude",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f m/s²", motionState.accelMagnitude),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                    }

                    // G-Force Indicator
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MedicalTealLight.copy(alpha = 0.15f))
                            .border(2.dp, MedicalTealLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.1fg", motionState.accelMagnitude / 9.81f),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MedicalTealLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Motion Variance (PPG Gating Metric)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Motion Artifact Variance",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark)
                    )
                    Text(
                        text = String.format("%.3f (Gating: %s)", motionState.motionVariance, if (motionState.motionVariance < 0.25f) "OK" else "High"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (motionState.motionVariance < 0.25f) VitalGreen else VitalAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Context Card
        val (activityIcon, activityColor, activityDesc) = when (motionState.activityContext) {
            MotionHazardDetector.ActivityContext.RESTING -> Triple(Icons.Default.Hotel, VitalGreen, "Resting / Sedentary (Optimal for PPG)")
            MotionHazardDetector.ActivityContext.MILD_MOVEMENT -> Triple(Icons.Default.DirectionsWalk, VitalAmber, "Mild Movement (Walking/Shifting)")
            MotionHazardDetector.ActivityContext.ACTIVE_EXERTION -> Triple(Icons.Default.DirectionsRun, VitalRed, "Active Exertion / Vibration")
        }

        VitalCard(
            title = "Current Activity Context",
            value = motionState.activityContext.name.replace('_', ' '),
            unit = "",
            icon = activityIcon,
            accentColor = activityColor,
            statusText = "Automated Gating",
            isNormal = motionState.activityContext == MotionHazardDetector.ActivityContext.RESTING,
            subtitle = activityDesc
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fall Detection Algorithm State Machine Breakdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "3-Phase State Machine Logic",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. Free-Fall: Vector magnitude drops below 0.5g (< 4.9 m/s²)\n2. Impact Shock: Vector magnitude surges above 2.8g (> 27.5 m/s²) within 1.2s\n3. Post-Impact Immobility: Sensor detects stillness (< 0.2 variance) for 2+ seconds",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Simulation & Test Action
        Text(
            text = "Disaster Resilience Validation & Testing",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.motionDetector.triggerSimulatedFall() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("btn_test_fall_trigger"),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalAmber,
                contentColor = Color.Black
            )
        ) {
            Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simulate Hard Fall & Immobility Test",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
