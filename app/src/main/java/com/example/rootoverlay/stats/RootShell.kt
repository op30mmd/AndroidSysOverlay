package com.example.rootoverlay.stats

import com.topjohnwu.superuser.Shell

object RootShell {
    fun readFiles(paths: List<String>): Map<String, String> {
        val cmd = paths.joinToString(" ; echo '<<>>' ; ") { "cat $it 2>/dev/null" }
        val out = Shell.cmd(cmd).exec().out.joinToString("\n")
        val chunks = out.split("<<>>")
        return paths.mapIndexed { i, p -> p to (chunks.getOrNull(i)?.trim() ?: "") }.toMap()
    }

    fun isRootAvailable(): Boolean = Shell.getShell().isRoot
}
