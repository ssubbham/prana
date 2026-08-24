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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VitalMeasurement
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
import com.example.ui.theme.VitalBlue
import com.example.ui.theme.VitalGreen
import com.example.ui.theme.VitalRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTrendsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.vitalsHistory.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("btn_back_history")
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
                        text = if (language == "hi") "स्वास्थ्य रुझान एवं डेटा लॉग" else "Vitals History & Trends",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Encrypted Local Room Database • Zero Cloud",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MedicalTealLight
                        )
                    )
                }
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, CardBorder, CircleShape)
                        .testTag("btn_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = VitalRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & DPDP Act Compliance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MedicalTeal.copy(alpha = 0.12f))
                .border(1.dp, MedicalTeal.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MedicalTealLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "India DPDP Act 2023 Compliant",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "Your biometrics never leave this device. Complete local retention and instant purge control.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recorded measurements yet.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete a camera PPG session to start tracking your vital trends.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    VitalHistoryItem(item = item)
                }
            }
        }
    }
}

@Composable
fun VitalHistoryItem(
    item: VitalMeasurement,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(item.timestamp))

    val scoreColor = when {
        item.riskScore >= 80 -> VitalGreen
        item.riskScore >= 60 -> VitalAmber
        else -> VitalRed
    }

    Box(
        modifier = modifier
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
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Score: ${item.riskScore}/100",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = scoreColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Heart Rate", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 11.sp))
                    Text(text = "${item.heartRate} BPM", style = MaterialTheme.typography.titleMedium.copy(color = VitalRed, fontWeight = FontWeight.Bold))
                }
                Column {
                    Text(text = "SpO2", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 11.sp))
                    Text(text = "${item.spo2}%", style = MaterialTheme.typography.titleMedium.copy(color = VitalBlue, fontWeight = FontWeight.Bold))
                }
                Column {
                    Text(text = "RMSSD", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 11.sp))
                    Text(text = "${item.hrvRmssd.toInt()} ms", style = MaterialTheme.typography.titleMedium.copy(color = MedicalTealLight, fontWeight = FontWeight.Bold))
                }
                Column {
                    Text(text = "Respiration", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 11.sp))
                    Text(text = "${item.respirationRate} Br/m", style = MaterialTheme.typography.titleMedium.copy(color = VitalAmber, fontWeight = FontWeight.Bold))
                }
            }

            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
