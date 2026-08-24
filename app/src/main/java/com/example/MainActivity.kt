package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.screens.AudioRespiratoryScreen
import com.example.ui.screens.CalibrationSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DisasterResilienceScreen
import com.example.ui.screens.HistoryTrendsScreen
import com.example.ui.screens.MotionFallScreen
import com.example.ui.screens.PpgMeasurementScreen
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PranaTheme
import kotlinx.coroutines.launch

sealed class Screen {
    data object Dashboard : Screen()
    data object PpgMeasurement : Screen()
    data object AudioRespiratory : Screen()
    data object MotionFall : Screen()
    data object DisasterResilience : Screen()
    data object HistoryTrends : Screen()
    data object CalibrationSettings : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PranaTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                // Dynamic permission request launcher
                val permissionsToRequest = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val cameraGranted = results[Manifest.permission.CAMERA] ?: false
                    val audioGranted = results[Manifest.permission.RECORD_AUDIO] ?: false
                    if (cameraGranted || audioGranted) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Sensor permissions granted for on-device analysis")
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val missingPermissions = permissionsToRequest.filter {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (missingPermissions.isNotEmpty()) {
                        permissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }

                // SOS Trigger Handler
                val triggerSosHandler: () -> Unit = {
                    val smsText = viewModel.generateSosSms()
                    val recipient = viewModel.userProfile.value?.emergencyContactPhone ?: "112"
                    try {
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$recipient")
                            putExtra("sms_body", smsText)
                        }
                        startActivity(smsIntent)
                        scope.launch {
                            snackbarHostState.showSnackbar("Emergency SMS drafted to $recipient")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Emergency SOS triggered: $smsText", Toast.LENGTH_LONG).show()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepNavy)
                        .statusBarsPadding(),
                    containerColor = DeepNavy,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                is Screen.Dashboard -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigate = { targetAppScreen ->
                                        currentScreen = when (targetAppScreen) {
                                            com.example.ui.AppScreen.DASHBOARD -> Screen.Dashboard
                                            com.example.ui.AppScreen.PPG_MEASURE -> Screen.PpgMeasurement
                                            com.example.ui.AppScreen.AUDIO_RESPIRATORY -> Screen.AudioRespiratory
                                            com.example.ui.AppScreen.MOTION_FALL -> Screen.MotionFall
                                            com.example.ui.AppScreen.DISASTER_RESILIENCE -> Screen.DisasterResilience
                                            com.example.ui.AppScreen.HISTORY_TRENDS -> Screen.HistoryTrends
                                            com.example.ui.AppScreen.CALIBRATION_SETTINGS -> Screen.CalibrationSettings
                                        }
                                    },
                                    onTriggerSos = triggerSosHandler
                                )
                                is Screen.PpgMeasurement -> PpgMeasurementScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                                is Screen.AudioRespiratory -> AudioRespiratoryScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                                is Screen.MotionFall -> MotionFallScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard },
                                    onTriggerSos = triggerSosHandler
                                )
                                is Screen.DisasterResilience -> DisasterResilienceScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard },
                                    onTriggerSos = triggerSosHandler
                                )
                                is Screen.HistoryTrends -> HistoryTrendsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                                is Screen.CalibrationSettings -> CalibrationSettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
