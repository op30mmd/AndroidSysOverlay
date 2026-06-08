package com.example.rootoverlay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rootoverlay.data.Metric
import com.example.rootoverlay.data.OverlaySettings
import com.example.rootoverlay.data.SettingsRepository
import com.example.rootoverlay.overlay.OverlayService
import com.example.rootoverlay.stats.RootShell
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(this)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(settingsRepository)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(repository: SettingsRepository) {
    val context = LocalContext.current
    val settings by repository.settingsFlow.collectAsStateWithLifecycle(initialValue = OverlaySettings())
    val scope = rememberCoroutineScope()

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isRootAvailable by remember { mutableStateOf(RootShell.isRootAvailable()) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Root Overlay Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Overlay Permission",
                isGranted = hasOverlayPermission,
                onGrant = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    title = "Notification Permission",
                    isGranted = hasNotificationPermission,
                    onGrant = {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            PermissionCard(
                title = "Root Access",
                isGranted = isRootAvailable,
                onGrant = {
                    isRootAvailable = RootShell.isRootAvailable()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Metrics", style = MaterialTheme.typography.titleLarge)
        }

        items(Metric.entries) { metric ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(metric.name)
                Checkbox(
                    checked = metric in settings.enabledMetrics,
                    onCheckedChange = { checked ->
                        val newMetrics = if (checked) {
                            settings.enabledMetrics + metric
                        } else {
                            settings.enabledMetrics - metric
                        }
                        scope.launch { repository.updateSettings(settings.copy(enabledMetrics = newMetrics)) }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Appearance", style = MaterialTheme.typography.titleLarge)

            SliderSetting(
                label = "Refresh Rate (${settings.refreshMs}ms)",
                value = settings.refreshMs.toFloat(),
                range = 100f..5000f,
                onValueChangeFinished = { newValue ->
                    scope.launch { repository.updateSettings(settings.copy(refreshMs = newValue.toLong())) }
                }
            )

            SliderSetting(
                label = "Font Size (${settings.fontSizeSp}sp)",
                value = settings.fontSizeSp,
                range = 8f..24f,
                onValueChangeFinished = { newValue ->
                    scope.launch { repository.updateSettings(settings.copy(fontSizeSp = newValue)) }
                }
            )

            SliderSetting(
                label = "Opacity (${(settings.opacity * 100).toInt()}%)",
                value = settings.opacity,
                range = 0.1f..1.0f,
                onValueChangeFinished = { newValue ->
                    scope.launch { repository.updateSettings(settings.copy(opacity = newValue)) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(context, OverlayService::class.java)
                    context.startService(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasOverlayPermission
            ) {
                Text("Start Overlay")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(context, OverlayService::class.java)
                    context.stopService(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop Overlay")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Refresh overlay permission status when returning to app
    LaunchedEffect(Unit) {
        // In a real app we'd use a LifecycleEventObserver
    }
}

@Composable
fun PermissionCard(title: String, isGranted: Boolean, onGrant: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (isGranted) "Granted" else "Missing",
                    color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (!isGranted) {
                Button(onClick = onGrant) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
fun SliderSetting(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChangeFinished: (Float) -> Unit) {
    var sliderValue by remember(value) { mutableStateOf(value) }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChangeFinished(sliderValue) },
            valueRange = range
        )
    }
}
