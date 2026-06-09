package io.github.androidsysoverlay.stats

data class StatsSnapshot(
    val cpuPercent: Float,
    val ramUsedMb: Long, val ramTotalMb: Long,
    val swapUsedMb: Long, val swapTotalMb: Long,
    val gpuPercent: Float?,
    val cpuTempC: Float?, val batteryTempC: Float?,
    val dischargeMa: Double, val watts: Double
)
