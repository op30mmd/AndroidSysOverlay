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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rootoverlay.data.*
import com.example.rootoverlay.ui.theme.RootOverlayTheme
import com.example.rootoverlay.overlay.OverlayService
import com.example.rootoverlay.stats.RootShell
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingsRepository = SettingsRepository(this)
        setContent {
            val settings by settingsRepository.settingsFlow.collectAsStateWithLifecycle(initialValue = OverlaySettings())
            RootOverlayTheme(
                darkTheme = when (settings.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                },
                dynamicColor = settings.useDynamicColor,
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Root Overlay Settings") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
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

                Spacer(modifier = Modifier.height(16.dp))
                Text("Font Visibility", style = MaterialTheme.typography.titleLarge)

                EnumDropdownSetting(
                    label = "Font Weight",
                    selected = settings.fontWeight,
                    options = FontWeightOption.entries,
                    onSelected = { newValue ->
                        scope.launch { repository.updateSettings(settings.copy(fontWeight = newValue)) }
                    }
                )

                ToggleSetting(
                    label = "Text Outline",
                    checked = settings.textOutline,
                    onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(textOutline = it)) } }
                )

                if (settings.textOutline) {
                    SliderSetting(
                        label = "Outline Width (${settings.outlineWidthDp}dp)",
                        value = settings.outlineWidthDp,
                        range = 0.5f..5.0f,
                        onValueChangeFinished = { newValue ->
                            scope.launch { repository.updateSettings(settings.copy(outlineWidthDp = newValue)) }
                        }
                    )
                    ColorSetting(
                        label = "Outline Color",
                        value = settings.outlineColor,
                        onValueChange = { newValue ->
                            scope.launch { repository.updateSettings(settings.copy(outlineColor = newValue)) }
                        }
                    )
                }

                ToggleSetting(
                    label = "Text Shadow",
                    checked = settings.textShadow,
                    onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(textShadow = it)) } }
                )

                ToggleSetting(
                    label = "Background Scrim",
                    checked = settings.backgroundScrim,
                    onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(backgroundScrim = it)) } }
                )

                if (settings.backgroundScrim) {
                    ToggleSetting(
                        label = "Auto Contrast (Black/White text)",
                        checked = settings.minContrastAuto,
                        onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(minContrastAuto = it)) } }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Theming", style = MaterialTheme.typography.titleLarge)

                ListItem(
                    headlineContent = { Text("Theme Mode") },
                    trailingContent = {
                        val options = ThemeMode.entries
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(settings.themeMode.name)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                options.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.name) },
                                        onClick = {
                                            scope.launch { repository.updateSettings(settings.copy(themeMode = mode)) }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )

                val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ListItem(
                    headlineContent = { Text("Use wallpaper colors") },
                    supportingContent = {
                        if (!supportsDynamic) Text("Requires Android 12 or newer")
                    },
                    trailingContent = {
                        Switch(
                            checked = settings.useDynamicColor && supportsDynamic,
                            enabled = supportsDynamic,
                            onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(useDynamicColor = it)) } },
                        )
                    },
                )

                OptionalColorSetting(
                    label = "Accent Override",
                    value = settings.accentOverride,
                    onValueChange = { newValue ->
                        scope.launch { repository.updateSettings(settings.copy(accentOverride = newValue)) }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Behavior", style = MaterialTheme.typography.titleLarge)

                ToggleSetting(
                    label = "Lock Position",
                    checked = settings.lockPosition,
                    onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(lockPosition = it)) } }
                )

                ToggleSetting(
                    label = "Pass Touches Through",
                    checked = settings.passThroughTouches,
                    onCheckedChange = { scope.launch { repository.updateSettings(settings.copy(passThroughTouches = it)) } }
                )
                if (settings.passThroughTouches) {
                    Text(
                        "Warning: You won't be able to drag the overlay while this is enabled. Use the persistent notification to disable if you get stuck.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

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
    }
}

@Composable
fun PermissionCard(title: String, isGranted: Boolean, onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
fun ToggleSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun <T : Enum<T>> EnumDropdownSetting(label: String, selected: T, options: List<T>, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedTextField(
                value = selected.name,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
            // Overlay an invisible box to handle clicks on the whole field
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }
    }
}

@Composable
fun ColorSetting(label: String, value: Long, onValueChange: (Long) -> Unit) {
    var textValue by remember(value) { mutableStateOf(String.format("%08X", value)) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        if (label.isNotEmpty()) {
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                try {
                    val newValue = it.toLong(16)
                    onValueChange(newValue)
                } catch (e: Exception) {}
            },
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("0x") }
        )
    }
}

@Composable
fun OptionalColorSetting(label: String, value: Long?, onValueChange: (Long?) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = value != null, onCheckedChange = {
                if (!it) onValueChange(null)
                else onValueChange(0xFF90CAF9L)
            })
            Text(label)
        }
        if (value != null) {
            ColorSetting(label = "", value = value, onValueChange = onValueChange)
        }
    }
}
