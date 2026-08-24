package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalSurfaceVariant
import com.example.ui.theme.TextEarthPrimary
import com.example.ui.theme.TextEarthSecondary

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NaturalSurfaceVariant)
            .border(1.dp, NaturalBorder, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLanguage.Language.values().forEach { lang ->
            val isSelected = currentLanguage == lang.code
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) BotanicalGreen else Color.Transparent)
                    .clickable { onLanguageSelected(lang.code) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("lang_btn_${lang.code}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) Color.White else TextEarthSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

