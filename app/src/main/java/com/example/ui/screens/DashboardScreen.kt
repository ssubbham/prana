package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.localization.AppLanguage
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.LanguageSelector
import com.example.ui.components.VitalCard
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.BotanicalGreenLight
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.EarthAmberContainer
import com.example.ui.theme.EmergencyCoral
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTealLight
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalSageContainer
import com.example.ui.theme.NaturalSageLight
import com.example.ui.theme.NaturalSurface
import com.example.ui.theme.NaturalSurfaceVariant
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
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (AppScreen) -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val latestVital by viewModel.latestVital.collectAsStateWithLifecycle()
    val riskEvaluation by viewModel.latestRiskEvaluation.collectAsStateWithLifecycle()
    val ambientTemp by viewModel.ambientTemperatureC.collectAsStateWithLifecycle()
    val hazardAlerts by viewModel.hazardAlerts.collectAsStateWithLifecycle()
    val motionState by viewModel.motionDetector.state.collectAsStateWithLifecycle()

    val vitalityScore = riskEvaluation?.overallVitalityScore ?: latestVital?.riskScore ?: 88
    val scoreColor = when {
        vitalityScore >= 80 -> VitalGreen
        vitalityScore >= 60 -> VitalAmber
        else -> VitalRed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header with Language Switcher and Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = AppLanguage.getString("app_title", language),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = AppLanguage.getString("app_subtitle", language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MedicalTealLight,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageSelector(
                    currentLanguage = language,
                    onLanguageSelected = { viewModel.setLanguage(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, CardBorder, CircleShape)
                        .clickable { onNavigate(AppScreen.CALIBRATION_SETTINGS) }
                        .testTag("btn_settings"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings & Calibration",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Medical disclaimer banner
        DisclaimerBanner(lang = language)

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Vitality & Resilience Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NaturalSurface, NaturalSurfaceVariant)
                    )
                )
                .border(1.dp, NaturalBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppLanguage.getString("vitality_score", language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                vitalityScore >= 80 -> if (language == "hi") "उत्कृष्ट स्वास्थ्य स्थिति" else "Optimal Resilience"
                                vitalityScore >= 60 -> if (language == "hi") "हल्का तनाव / सामान्य" else "Moderate Strain"
                                else -> if (language == "hi") "उच्च तनाव चेतावनी" else "High Risk Warning"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                    }

                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(scoreColor.copy(alpha = 0.15f))
                            .border(2.5.dp, scoreColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$vitalityScore",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = scoreColor,
                                    fontSize = 24.sp
                                )
                            )
                            Text(
                                text = "/100",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMutedDark,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Activity & Thermal Context pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaturalSageLight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Context: ${motionState.activityContext.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BotanicalGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EarthAmberContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Temp: ${ambientTemp.toInt()}°C",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EarthAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                if (riskEvaluation?.summaryInsight != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = riskEvaluation!!.summaryInsight,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero CTA: Start PPG Measurement
        Button(
            onClick = { onNavigate(AppScreen.PPG_MEASURE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("btn_start_ppg_measure"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MedicalTeal,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = AppLanguage.getString("start_ppg", language),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Disaster Resilience / Hazard Card
        val activeHazard = hazardAlerts.firstOrNull()
        if (activeHazard != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (activeHazard.severity == "SEVERE") VitalRed.copy(alpha = 0.12f)
                        else VitalAmber.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (activeHazard.severity == "SEVERE") VitalRed.copy(alpha = 0.4f)
                        else VitalAmber.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onNavigate(AppScreen.DISASTER_RESILIENCE) }
                    .padding(16.dp)
                    .testTag("card_active_hazard")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (activeHazard.severity == "SEVERE") VitalRed else VitalAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeHazard.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            )
                        }
                        Text(
                            text = activeHazard.severity,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (activeHazard.severity == "SEVERE") VitalRed else VitalAmber,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeHazard.precautions,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Physiological Vitals Grid
        Text(
            text = if (language == "hi") "नवीनतम शारीरिक संकेतक (Latest Vitals)" else "Physiological Parameters (Sensor-Derived)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Heart Rate Card
        VitalCard(
            title = AppLanguage.getString("heart_rate", language),
            value = latestVital?.heartRate?.toString() ?: "--",
            unit = "BPM",
            icon = Icons.Default.Favorite,
            accentColor = VitalRed,
            statusText = when {
                latestVital == null -> "No data"
                latestVital!!.heartRate in 60..95 -> "Normal"
                latestVital!!.heartRate > 100 -> "Elevated"
                else -> "Low"
            },
            isNormal = latestVital?.heartRate in 55..100,
            subtitle = "Resting camera PPG photoplethysmography"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // HRV Card (SDNN & RMSSD)
        val rmssdVal = latestVital?.hrvRmssd?.toInt()?.toString() ?: "--"
        val sdnnVal = latestVital?.hrvSdnn?.toInt()?.toString() ?: "--"
        VitalCard(
            title = AppLanguage.getString("hrv", language),
            value = "$rmssdVal / $sdnnVal",
            unit = "ms (RMSSD/SDNN)",
            icon = Icons.Default.Timeline,
            accentColor = MedicalTealLight,
            statusText = if (latestVital?.hrvRmssd ?: 40.0 >= 25.0) "Good Parasympathetic" else "Stress Strain",
            isNormal = (latestVital?.hrvRmssd ?: 40.0) >= 25.0,
            subtitle = "Autonomic nervous system recovery index"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SpO2 Card
        VitalCard(
            title = AppLanguage.getString("spo2", language),
            value = latestVital?.spo2?.toString() ?: "--",
            unit = "%",
            icon = Icons.Default.WaterDrop,
            accentColor = VitalBlue,
            statusText = if ((latestVital?.spo2 ?: 98) >= 95) "Normal Saturation" else "Attention",
            isNormal = (latestVital?.spo2 ?: 98) >= 94,
            subtitle = "Estimated optical ratio of ratios (RGB camera)"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Respiration Rate Card
        VitalCard(
            title = AppLanguage.getString("respiration", language),
            value = latestVital?.respirationRate?.toString() ?: "--",
            unit = "breaths/min",
            icon = Icons.Default.Air,
            accentColor = VitalAmber,
            statusText = if ((latestVital?.respirationRate ?: 16) in 12..22) "Steady Eupnea" else "Elevated",
            isNormal = (latestVital?.respirationRate ?: 16) in 12..22,
            subtitle = "Respiratory-Induced Intensity Variation (RIIV)"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Heat Stress Card
        VitalCard(
            title = AppLanguage.getString("heat_stress", language),
            value = latestVital?.heatStressLevel ?: "NORMAL",
            unit = "",
            icon = Icons.Default.Thermostat,
            accentColor = if (latestVital?.heatStressLevel == "NORMAL" || latestVital == null) VitalGreen else VitalRed,
            statusText = "${ambientTemp.toInt()}°C Ambient",
            isNormal = latestVital?.heatStressLevel == "NORMAL" || latestVital == null,
            subtitle = "Fused thermal-cardiac dehydration predictor"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Environmental Heat Slider Simulator
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ambient Heat Simulation",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "${ambientTemp.toInt()}°C",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VitalAmber
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adjust ambient temperature to test the real-time heat stress early warning engine.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                )
                Slider(
                    value = ambientTemp,
                    onValueChange = { viewModel.setAmbientTemperature(it) },
                    valueRange = 20f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = VitalAmber,
                        activeTrackColor = VitalAmber
                    ),
                    modifier = Modifier.testTag("slider_ambient_temp")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Quick Navigation Matrix
        Text(
            text = if (language == "hi") "अतिरिक्त सेंसर मॉड्यूल" else "On-Device Sensor Modules",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Audio check button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(AppScreen.AUDIO_RESPIRATORY) }
                    .padding(16.dp)
                    .testTag("nav_audio_module"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = VitalBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Acoustic Check",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Cough & Breaths",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Motion & Fall Sentinel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(AppScreen.MOTION_FALL) }
                    .padding(16.dp)
                    .testTag("nav_motion_module"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = VitalGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fall Sentinel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Accel & Gyro",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disaster & SOS button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(AppScreen.DISASTER_RESILIENCE) }
                    .padding(16.dp)
                    .testTag("nav_disaster_module"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = null,
                        tint = VitalRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Disaster & SOS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Offline Hazards",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // History & Trends
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .clickable { onNavigate(AppScreen.HISTORY_TRENDS) }
                    .padding(16.dp)
                    .testTag("nav_history_module"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = VitalAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trends & Logs",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Room DB Export",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Emergency SOS Action
        Button(
            onClick = { onTriggerSos() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("btn_quick_sos"),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalRed,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Emergency,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppLanguage.getString("emergency_sos", language),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
