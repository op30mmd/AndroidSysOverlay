package io.github.androidsysoverlay.data

enum class Metric { CPU, RAM, SWAP, GPU, CPU_TEMP, BATTERY_TEMP, DISCHARGE }

enum class Layout { VERTICAL, HORIZONTAL, GRID }

enum class TempUnit { CELSIUS, FAHRENHEIT }

enum class FontWeightOption { LIGHT, NORMAL, MEDIUM, BOLD }
enum class ThemeMode { LIGHT, DARK, FOLLOW_SYSTEM }

data class OverlaySettings(
    val enabledMetrics: Set<Metric> = Metric.entries.toSet(),
    val metricOrder: List<Metric> = Metric.entries,
    val refreshMs: Long = 1000,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x80000000,
    val fontSizeSp: Float = 11f,
    val opacity: Float = 0.85f,
    val layout: Layout = Layout.VERTICAL,
    val posX: Int = 0,
    val posY: Int = 100,
    val showLabels: Boolean = true,
    val showUnits: Boolean = true,
    val perCoreCpu: Boolean = false,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val startOnBoot: Boolean = true,

    // 1. Font visibility
    val fontWeight: FontWeightOption = FontWeightOption.MEDIUM,
    val textOutline: Boolean = true,
    val outlineColor: Long = 0xFF000000,
    val outlineWidthDp: Float = 1.5f,
    val textShadow: Boolean = false,
    val backgroundScrim: Boolean = true,
    val minContrastAuto: Boolean = false,

    // 2. Theming
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = true,
    val accentOverride: Long? = null,

    // 3. Lock position
    val lockPosition: Boolean = false,

    // 4. Touch pass-through
    val passThroughTouches: Boolean = false
)
