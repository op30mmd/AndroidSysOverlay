package io.github.androidsysoverlay.stats

object GpuReader {
    val GPU_BUSY_NODES = listOf(
        "/sys/class/kgsl/kgsl-3d0/gpubusy",
        "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load",
        "/sys/class/misc/mali0/device/utilization",
        "/sys/devices/platform/*.mali/utilization",
        "/sys/kernel/gpu/gpu_busy",
    )

    fun parseGpuUsage(raw: String, path: String): Float? {
        return when {
            path.endsWith("gpubusy") -> {
                val parts = raw.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (parts.size >= 2) {
                    val busy = parts[0].toLongOrNull() ?: 0L
                    val total = parts[1].toLongOrNull() ?: 0L
                    if (total > 0) (busy.toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f) else 0f
                } else null
            }
            raw.isNotBlank() -> raw.trim().split(Regex("\\s+"))[0].toFloatOrNull()?.coerceIn(0f, 100f)
            else -> null
        }
    }
}
