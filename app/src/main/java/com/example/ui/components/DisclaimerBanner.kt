package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VitalAmber

@Composable
fun DisclaimerBanner(
    lang: String = "en",
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VitalAmber.copy(alpha = 0.10f))
            .border(1.dp, VitalAmber.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(12.dp)
            .testTag("disclaimer_banner")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Medical Disclaimer",
                        tint = VitalAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "hi") "चिकित्सा कल्याण सूचना (Disclaimer)" else "Medical Wellness & Early Warning Notice",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Disclaimer Details",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppLanguage.getString("disclaimer", lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            lineHeight = 18.sp,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == "hi") "• 100% ऑन-डिवाइस सिग्नल प्रोसेसिंग\n• कोई कैमरा या ऑडियो क्लाउड पर नहीं भेजा जाता" else "• 100% On-Device Signal Processing (Zero Cloud Upload)\n• Calibrate with reference pulse oximeter for personal accuracy tuning",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMutedDark,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
