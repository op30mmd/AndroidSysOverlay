package io.github.androidsysoverlay.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class ThermalReaderTest {

    @Test
    fun normalizeTemp_milliCelsius_dividesBy1000() {
        assertEquals(41.5f, ThermalReader.normalizeTemp(41500L), 0.01f)
    }

    @Test
    fun normalizeTemp_deciCelsius_dividesBy10() {
        assertEquals(41.5f, ThermalReader.normalizeTemp(415L), 0.01f)
    }

    @Test
    fun normalizeTemp_celsius_returnsSameValue() {
        assertEquals(41.0f, ThermalReader.normalizeTemp(41L), 0.01f)
    }
}
