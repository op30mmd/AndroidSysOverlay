package com.example.rootoverlay.stats

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.topjohnwu.superuser.Shell

class StatsCollector(private val context: Context) {
    private var prevCpu: CpuTimes? = null
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private var gpuNodePath: String? = null

    init {
        probeGpuNode()
    }

    private fun probeGpuNode() {
        val paths = GpuReader.GPU_BUSY_NODES
        val cmd = paths.joinToString(" ; echo '<<>>' ; ") { "[ -f $it ] && echo $it" }
        val out = Shell.cmd(cmd).exec().out.joinToString("\n")
        val chunks = out.split("<<>>")
        gpuNodePath = chunks.map { it.trim() }.firstOrNull { it.isNotBlank() }
    }

    fun sample(): StatsSnapshot {
        val paths = mutableListOf("/proc/stat", "/proc/meminfo", "/sys/class/thermal/thermal_zone0/temp")
        gpuNodePath?.let { paths.add(it) }

        val files = RootShell.readFiles(paths)

        // CPU
        val cpuLine = files["/proc/stat"]?.lineSequence()?.firstOrNull() ?: ""
        val currentCpu = CpuReader.parseCpuLine(cpuLine)
        val cpuUsage = if (prevCpu != null && currentCpu != null) {
            CpuReader.cpuUsagePercent(prevCpu!!, currentCpu)
        } else 0f
        prevCpu = currentCpu

        // Memory
        val meminfo = MemReader.parseMeminfo(files["/proc/meminfo"] ?: "")
        val memTotal = meminfo["MemTotal"] ?: 0L
        val memAvailable = meminfo["MemAvailable"] ?: meminfo["MemFree"] ?: 0L
        val ramUsedMb = (memTotal - memAvailable) / 1024
        val ramTotalMb = memTotal / 1024

        val swapTotal = meminfo["SwapTotal"] ?: 0L
        val swapFree = meminfo["SwapFree"] ?: 0L
        val swapUsedMb = (swapTotal - swapFree) / 1024
        val swapTotalMb = swapTotal / 1024

        // GPU
        val gpuUsage = gpuNodePath?.let { path ->
            files[path]?.let { GpuReader.parseGpuUsage(it, path) }
        }

        // Temp
        val cpuTemp = files["/sys/class/thermal/thermal_zone0/temp"]?.toLongOrNull()?.let {
            ThermalReader.normalizeTemp(it)
        }

        // Battery
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val batteryTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.let { it / 10f }
        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val currentUa = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = currentUa / 1000.0
        val watts = (currentMa / 1000.0) * (voltageMv / 1000.0)

        return StatsSnapshot(
            cpuPercent = cpuUsage,
            ramUsedMb = ramUsedMb,
            ramTotalMb = ramTotalMb,
            swapUsedMb = swapUsedMb,
            swapTotalMb = swapTotalMb,
            gpuPercent = gpuUsage,
            cpuTempC = cpuTemp,
            batteryTempC = batteryTemp,
            dischargeMa = currentMa,
            watts = watts
        )
    }
}
