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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.HazardAlert
import com.example.ui.MainViewModel
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTealLight
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VitalAmber
import com.example.ui.theme.VitalGreen
import com.example.ui.theme.VitalRed

@Composable
fun DisasterResilienceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazardAlerts by viewModel.hazardAlerts.collectAsStateWithLifecycle()
    val disasterState by viewModel.disasterManager.state.collectAsStateWithLifecycle()
    val latestVital by viewModel.latestVital.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var contactName by remember(userProfile) { mutableStateOf(userProfile?.emergencyContactName ?: "Emergency Helpline") }
    var contactPhone by remember(userProfile) { mutableStateOf(userProfile?.emergencyContactPhone ?: "112") }

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
                    .testTag("btn_back_disaster")
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
                    text = if (language == "hi") "आपदा लचीलापन एवं आपातकालीन SOS" else "Disaster Resilience & SOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Offline Early Warning & One-Tap SMS Relay",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MedicalTealLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero SOS Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(VitalRed.copy(alpha = 0.15f))
                .border(1.5.dp, VitalRed, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(VitalRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "SOS",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "One-Tap Offline SOS SMS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Zero internet required • GPS coordinates + Vitals",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GPS & Vitals preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = VitalAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coordinates: ${String.format("%.4f, %.4f", disasterState.currentLatitude, disasterState.currentLongitude)}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimaryDark,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payload: HR ${latestVital?.heartRate ?: 72} BPM • SpO2 ${latestVital?.spo2 ?: 98}% • Respiration ${latestVital?.respirationRate ?: 16} Br/m",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onTriggerSos() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("btn_trigger_emergency_sos"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Emergency, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trigger Emergency SMS Now",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Emergency Contact Configuration
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
                    text = "Emergency Recipient Configuration",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactName,
                    onValueChange = {
                        contactName = it
                        viewModel.updateEmergencyContact(contactName, contactPhone)
                    },
                    label = { Text("Contact Name / Service") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = {
                        contactPhone = it
                        viewModel.updateEmergencyContact(contactName, contactPhone)
                    },
                    label = { Text("Phone Number / SOS Helpline (e.g. 112)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Offline Indian Meteorological & Disaster Hazard Cache
        Text(
            text = "Offline Cached Meteorological Alerts (India Grid)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        disasterState.activeHazards.forEach { alert ->
            HazardAlertItem(alert = alert)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HazardAlertItem(
    alert: HazardAlert,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alert.severity) {
        "EXTREME", "SEVERE" -> VitalRed
        "WARNING" -> VitalAmber
        else -> VitalGreen
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, severityColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
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
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = alert.severity,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = severityColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Precautions & Frequency (${alert.recommendedIntervalMinutes}m interval):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MedicalTealLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alert.precautions,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
