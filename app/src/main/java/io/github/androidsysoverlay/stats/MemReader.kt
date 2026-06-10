package io.github.androidsysoverlay.stats

object MemReader {
    fun parseMeminfo(raw: String): Map<String, Long> =
        raw.lineSequence().mapNotNull { l ->
            val m = Regex("^(\\w+):\\s+(\\d+)").find(l) ?: return@mapNotNull null
            m.groupValues[1] to m.groupValues[2].toLong() // kB
        }.toMap()
}
