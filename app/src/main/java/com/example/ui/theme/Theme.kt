package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NaturalColorScheme = lightColorScheme(
    primary = BotanicalGreen,
    onPrimary = Color.White,
    primaryContainer = BotanicalGreenContainer,
    onPrimaryContainer = OnBotanicalGreenContainer,
    secondary = RiverBlue,
    onSecondary = Color.White,
    secondaryContainer = RiverBlueContainer,
    onSecondaryContainer = OnRiverBlueContainer,
    tertiary = EarthAmber,
    onTertiary = Color.White,
    tertiaryContainer = EarthAmberContainer,
    onTertiaryContainer = OnEarthAmberContainer,
    error = EmergencyCoral,
    errorContainer = EmergencyContainer,
    onError = Color.White,
    onErrorContainer = OnEmergencyContainer,
    background = NaturalBackground,
    onBackground = TextEarthPrimary,
    surface = NaturalSurface,
    onSurface = TextEarthPrimary,
    surfaceVariant = NaturalSurfaceVariant,
    onSurfaceVariant = TextEarthSecondary,
    outline = NaturalBorder
)

@Composable
fun PranaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = NaturalColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

