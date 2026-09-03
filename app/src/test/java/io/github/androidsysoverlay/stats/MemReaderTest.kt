package io.github.androidsysoverlay.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class MemReaderTest {

    @Test
    fun parseMeminfo_validInput_parsesValuesInKb() {
        val raw = """
            MemTotal:       8123456 kB
            MemFree:        1234567 kB
            MemAvailable:   4567890 kB
            SwapTotal:      2097148 kB
            SwapFree:       1048576 kB
        """.trimIndent()

        val map = MemReader.parseMeminfo(raw)

        assertEquals(8123456L, map["MemTotal"])
        assertEquals(1234567L, map["MemFree"])
        assertEquals(4567890L, map["MemAvailable"])
        assertEquals(2097148L, map["SwapTotal"])
        assertEquals(1048576L, map["SwapFree"])
    }
}
