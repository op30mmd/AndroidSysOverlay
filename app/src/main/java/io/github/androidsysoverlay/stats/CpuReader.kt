package io.github.androidsysoverlay.stats

data class CpuTimes(val idle: Long, val total: Long)

object CpuReader {
    fun parseCpuLine(line: String): CpuTimes? {
        if (!line.startsWith("cpu")) return null
        val v = line.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
        if (v.size < 7) return null
        val idle = v[3] + v[4] // idle + iowait
        val total = v.sum()
        return CpuTimes(idle, total)
    }

    fun cpuUsagePercent(prev: CpuTimes, now: CpuTimes): Float {
        val dTotal = (now.total - prev.total).toFloat()
        val dIdle = (now.idle - prev.idle).toFloat()
        if (dTotal <= 0f) return 0f
        return ((dTotal - dIdle) / dTotal * 100f).coerceIn(0f, 100f)
    }
}
