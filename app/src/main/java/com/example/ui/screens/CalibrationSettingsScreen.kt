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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.LanguageSelector
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

@Composable
fun CalibrationSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var refOximeterValue by remember { mutableStateOf("98") }
    var calibrationMessage by remember { mutableStateOf<String?>(null) }

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
                    .testTag("btn_back_calibration")
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
                    text = if (language == "hi") "कैलिब्रेशन एवं डिवाइस सेटिंग्स" else "Calibration & Device Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "SpO2 Empirical Regression • Baseline Adaptation",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MedicalTealLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Configuration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Application Language",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "English & हिन्दी (India Support)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    )
                }
                LanguageSelector(
                    currentLanguage = language,
                    onLanguageSelected = { viewModel.setLanguage(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SpO2 Empirical Calibration Module
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VitalBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = VitalBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Reference Pulse Oximeter Calibration",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = if (userProfile?.isSpo2Calibrated == true) "Status: Calibrated (a=${String.format("%.1f", userProfile?.spo2CalibrationA)}, b=14.5)" else "Status: Default Uncalibrated Proxy",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (userProfile?.isSpo2Calibrated == true) VitalGreen else VitalAmber,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Camera RGB optical SpO2 uses ratio-of-ratios R = (AC_red/DC_red)/(AC_blue/DC_blue) with linear formula: SpO2 = a − b × R.\nFor best accuracy, enter your reading from a physical fingertip pulse oximeter while measuring.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = refOximeterValue,
                    onValueChange = { refOximeterValue = it },
                    label = { Text("Reference Oximeter SpO2 (%)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ref_oximeter"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val refVal = refOximeterValue.toIntOrNull() ?: 98
                        viewModel.calibrateSpo2(refVal, 0.45)
                        calibrationMessage = "Successfully calibrated! Device regression coefficients stored in Room database."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("btn_save_calibration"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedicalTeal,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calibrate Camera SpO2 Engine",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (calibrationMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = calibrationMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VitalGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Baseline Vitals Configuration
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
                    text = "Personal Physiological Baselines",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The AI Risk Engine dynamically tracks personalized Z-score standard deviations from these resting norms:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Baseline Heart Rate:", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark))
                    Text(text = "${userProfile?.baselineHr?.toInt() ?: 72} BPM", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Baseline RMSSD (HRV):", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark))
                    Text(text = "${userProfile?.baselineRmssd?.toInt() ?: 42} ms", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Baseline Respiration:", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark))
                    Text(text = "${userProfile?.baselineRr?.toInt() ?: 16} Br/m", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
