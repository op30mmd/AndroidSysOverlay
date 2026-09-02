package io.github.androidsysoverlay.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlaySettingsTest {

    @Test
    fun defaultStartOnBoot_isTrue() {
        val settings = OverlaySettings()
        assertTrue(settings.startOnBoot)
    }

    @Test
    fun copyStartOnBoot_updatesValue() {
        val settings = OverlaySettings()
        val updated = settings.copy(startOnBoot = false)
        assertFalse(updated.startOnBoot)
    }
}
