package com.kodeforge.sftp

import kotlinx.serialization.Serializable

/**
 * Representa un archivo o directorio remoto en SFTP.
 */
@Serializable
data class RemoteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val modifiedAt: String = "",
    val permissions: String = ""
)

/**
 * Resultado de una operación SFTP.
 */
sealed class SftpResult<out T> {
    data class Success<T>(val data: T) : SftpResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : SftpResult<Nothing>()
}

/**
 * Estado de una conexión SFTP.
 */
enum class SftpConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

