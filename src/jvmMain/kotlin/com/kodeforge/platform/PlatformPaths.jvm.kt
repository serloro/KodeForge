package com.kodeforge.platform

import java.io.File

actual object PlatformPaths {
    actual fun downloadsDir(): String {
        val home = System.getProperty("user.home") ?: "."
        val candidates = listOf(
            File(home, "Downloads"),
            File(home, "Descargas")
        )
        return candidates.firstOrNull { it.exists() }?.absolutePath ?: File(home).absolutePath
    }
}
