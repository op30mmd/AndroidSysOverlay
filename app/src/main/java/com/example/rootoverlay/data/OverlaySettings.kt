package com.example.rootoverlay.data

enum class Metric { CPU, RAM, SWAP, GPU, CPU_TEMP, BATTERY_TEMP, DISCHARGE }

enum class Layout { VERTICAL, HORIZONTAL, GRID }

enum class TempUnit { CELSIUS, FAHRENHEIT }

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
    val startOnBoot: Boolean = true
)
