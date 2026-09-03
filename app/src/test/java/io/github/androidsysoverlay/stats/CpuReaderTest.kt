package io.github.androidsysoverlay.stats

import org.junit.Assert.*
import org.junit.Test

class CpuReaderTest {

    @Test
    fun parseCpuLine_validLine_returnsCpuTimes() {
        val line = "cpu  100 20 30 400 50 10 5 0 0 0"
        val times = CpuReader.parseCpuLine(line)

        assertNotNull(times)
        // idle = v[3] + v[4] = 400 + 50 = 450
        assertEquals(450L, times!!.idle)
        // total = sum of all 10 values = 615
        assertEquals(615L, times.total)
    }

    @Test
    fun parseCpuLine_invalidLine_returnsNull() {
        assertNull(CpuReader.parseCpuLine("intr 123456"))
        assertNull(CpuReader.parseCpuLine("cpu 10 20 30"))
    }

    @Test
    fun cpuUsagePercent_calculatesPercentageCorrectly() {
        val prev = CpuTimes(idle = 400, total = 1000)
        val now = CpuTimes(idle = 450, total = 1200)

        // dTotal = 200, dIdle = 50, dActive = 150
        // usage = 150 / 200 * 100 = 75%
        val usage = CpuReader.cpuUsagePercent(prev, now)
        assertEquals(75f, usage, 0.01f)
    }

    @Test
    fun cpuUsagePercent_zeroDeltaTotal_returnsZero() {
        val prev = CpuTimes(idle = 400, total = 1000)
        val now = CpuTimes(idle = 400, total = 1000)

        val usage = CpuReader.cpuUsagePercent(prev, now)
        assertEquals(0f, usage, 0.01f)
    }
}
