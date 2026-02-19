package com.kodeforge.sftpdrive

import com.kodeforge.domain.model.SftpDriveMountConfig

/**
 * API multiplataforma: en JVM/Windows se implementa con WinFsp + SSHFS-Win.
 */
expect object SftpDriveMounter {
    fun isSupported(): Boolean

    /**
     * Intenta instalar dependencias necesarias (solo Windows).
     *
     * @return texto simple para mostrar al usuario.
     */
    suspend fun ensureDependenciesInstalled(silent: Boolean = false): String

    /** Monta la unidad. Lanza excepción si falla. */
    suspend fun mount(config: SftpDriveMountConfig): String

    /** Desmonta la unidad (si está montada). */
    suspend fun unmount(driveLetter: String): String
}
