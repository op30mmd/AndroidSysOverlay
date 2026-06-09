package com.example.rootoverlay.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore

    object PreferencesKeys {
        val ENABLED_METRICS = stringSetPreferencesKey("enabled_metrics")
        val REFRESH_MS = longPreferencesKey("refresh_ms")
        val TEXT_COLOR = longPreferencesKey("text_color")
        val BACKGROUND_COLOR = longPreferencesKey("background_color")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val OPACITY = floatPreferencesKey("opacity")
        val LAYOUT = stringPreferencesKey("layout")
        val POS_X = intPreferencesKey("pos_x")
        val POS_Y = intPreferencesKey("pos_y")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SHOW_UNITS = booleanPreferencesKey("show_units")
        val PER_CORE_CPU = booleanPreferencesKey("per_core_cpu")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
    }

    val settingsFlow: Flow<OverlaySettings> = dataStore.data.map { preferences ->
        OverlaySettings(
            enabledMetrics = preferences[PreferencesKeys.ENABLED_METRICS]?.map { Metric.valueOf(it) }?.toSet() ?: Metric.entries.toSet(),
            refreshMs = preferences[PreferencesKeys.REFRESH_MS] ?: 1000L,
            textColor = preferences[PreferencesKeys.TEXT_COLOR] ?: 0xFFFFFFFF,
            backgroundColor = preferences[PreferencesKeys.BACKGROUND_COLOR] ?: 0x80000000,
            fontSizeSp = preferences[PreferencesKeys.FONT_SIZE_SP] ?: 11f,
            opacity = preferences[PreferencesKeys.OPACITY] ?: 0.85f,
            layout = Layout.valueOf(preferences[PreferencesKeys.LAYOUT] ?: Layout.VERTICAL.name),
            posX = preferences[PreferencesKeys.POS_X] ?: 0,
            posY = preferences[PreferencesKeys.POS_Y] ?: 100,
            showLabels = preferences[PreferencesKeys.SHOW_LABELS] ?: true,
            showUnits = preferences[PreferencesKeys.SHOW_UNITS] ?: true,
            perCoreCpu = preferences[PreferencesKeys.PER_CORE_CPU] ?: false,
            tempUnit = TempUnit.valueOf(preferences[PreferencesKeys.TEMP_UNIT] ?: TempUnit.CELSIUS.name),
            startOnBoot = preferences[PreferencesKeys.START_ON_BOOT] ?: true
        )
    }

    suspend fun updateSettings(settings: OverlaySettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLED_METRICS] = settings.enabledMetrics.map { it.name }.toSet()
            preferences[PreferencesKeys.REFRESH_MS] = settings.refreshMs
            preferences[PreferencesKeys.TEXT_COLOR] = settings.textColor
            preferences[PreferencesKeys.BACKGROUND_COLOR] = settings.backgroundColor
            preferences[PreferencesKeys.FONT_SIZE_SP] = settings.fontSizeSp
            preferences[PreferencesKeys.OPACITY] = settings.opacity
            preferences[PreferencesKeys.LAYOUT] = settings.layout.name
            preferences[PreferencesKeys.POS_X] = settings.posX
            preferences[PreferencesKeys.POS_Y] = settings.posY
            preferences[PreferencesKeys.SHOW_LABELS] = settings.showLabels
            preferences[PreferencesKeys.SHOW_UNITS] = settings.showUnits
            preferences[PreferencesKeys.PER_CORE_CPU] = settings.perCoreCpu
            preferences[PreferencesKeys.TEMP_UNIT] = settings.tempUnit.name
            preferences[PreferencesKeys.START_ON_BOOT] = settings.startOnBoot
        }
    }

    suspend fun setPosition(x: Int, y: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.POS_X] = x
            preferences[PreferencesKeys.POS_Y] = y
        }
    }
}
