package io.github.androidsysoverlay.stats

object ThermalReader {
    fun normalizeTemp(raw: Long): Float = when {
        raw > 10000 -> raw / 1000f   // milli-°C (41500 -> 41.5)
        raw > 1000  -> raw / 1000f   // also milli-°C
        raw > 100   -> raw / 10f     // deci-°C (415 -> 41.5)
        else        -> raw.toFloat()
    }
}
