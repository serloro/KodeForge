package com.kodeforge.platform

/**
 * Utilidades de rutas dependientes de plataforma.
 */
expect object PlatformPaths {
    /** Carpeta de descargas por defecto (cuando exista). */
    fun downloadsDir(): String
}
