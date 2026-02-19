package com.kodeforge.platform

actual fun isWindows(): Boolean {
    val os = System.getProperty("os.name") ?: return false
    return os.lowercase().contains("windows")
}
