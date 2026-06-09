package io.github.androidsysoverlay.data

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

        val FONT_WEIGHT = stringPreferencesKey("font_weight")
        val TEXT_OUTLINE = booleanPreferencesKey("text_outline")
        val OUTLINE_COLOR = longPreferencesKey("outline_color")
        val OUTLINE_WIDTH_DP = floatPreferencesKey("outline_width_dp")
        val TEXT_SHADOW = booleanPreferencesKey("text_shadow")
        val BACKGROUND_SCRIM = booleanPreferencesKey("background_scrim")
        val MIN_CONTRAST_AUTO = booleanPreferencesKey("min_contrast_auto")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val ACCENT_OVERRIDE = longPreferencesKey("accent_override")
        val LOCK_POSITION = booleanPreferencesKey("lock_position")
        val PASS_THROUGH_TOUCHES = booleanPreferencesKey("pass_through_touches")
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
            startOnBoot = preferences[PreferencesKeys.START_ON_BOOT] ?: true,

            fontWeight = FontWeightOption.valueOf(preferences[PreferencesKeys.FONT_WEIGHT] ?: FontWeightOption.MEDIUM.name),
            textOutline = preferences[PreferencesKeys.TEXT_OUTLINE] ?: true,
            outlineColor = preferences[PreferencesKeys.OUTLINE_COLOR] ?: 0xFF000000,
            outlineWidthDp = preferences[PreferencesKeys.OUTLINE_WIDTH_DP] ?: 1.5f,
            textShadow = preferences[PreferencesKeys.TEXT_SHADOW] ?: false,
            backgroundScrim = preferences[PreferencesKeys.BACKGROUND_SCRIM] ?: true,
            minContrastAuto = preferences[PreferencesKeys.MIN_CONTRAST_AUTO] ?: false,
            themeMode = ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.FOLLOW_SYSTEM.name),
            useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true,
            accentOverride = preferences[PreferencesKeys.ACCENT_OVERRIDE],
            lockPosition = preferences[PreferencesKeys.LOCK_POSITION] ?: false,
            passThroughTouches = preferences[PreferencesKeys.PASS_THROUGH_TOUCHES] ?: false
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

            preferences[PreferencesKeys.FONT_WEIGHT] = settings.fontWeight.name
            preferences[PreferencesKeys.TEXT_OUTLINE] = settings.textOutline
            preferences[PreferencesKeys.OUTLINE_COLOR] = settings.outlineColor
            preferences[PreferencesKeys.OUTLINE_WIDTH_DP] = settings.outlineWidthDp
            preferences[PreferencesKeys.TEXT_SHADOW] = settings.textShadow
            preferences[PreferencesKeys.BACKGROUND_SCRIM] = settings.backgroundScrim
            preferences[PreferencesKeys.MIN_CONTRAST_AUTO] = settings.minContrastAuto
            preferences[PreferencesKeys.THEME_MODE] = settings.themeMode.name
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
            if (settings.accentOverride != null) {
                preferences[PreferencesKeys.ACCENT_OVERRIDE] = settings.accentOverride
            } else {
                preferences.remove(PreferencesKeys.ACCENT_OVERRIDE)
            }
            preferences[PreferencesKeys.LOCK_POSITION] = settings.lockPosition
            preferences[PreferencesKeys.PASS_THROUGH_TOUCHES] = settings.passThroughTouches
        }
    }

    suspend fun setPosition(x: Int, y: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.POS_X] = x
            preferences[PreferencesKeys.POS_Y] = y
        }
    }
}
