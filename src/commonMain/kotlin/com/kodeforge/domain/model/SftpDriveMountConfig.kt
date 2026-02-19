package com.kodeforge.domain.model

/**
 * Configuración para montar una unidad virtual (letra) apuntando a una ruta SFTP.
 *
 * Nota: En Windows el montaje real requiere WinFsp + SSHFS-Win.
 */
data class SftpDriveMountConfig(
    val driveName: String = "",
    /** Letra sin ':' (ej. "S"). */
    val driveLetter: String = "S",
    val enabled: Boolean = true,

    val remoteHost: String = "",
    val remotePort: Int = 22,
    val username: String = "",
    val password: String = "",
    /** Ruta a clave privada (opcional). */
    val privateKeyPath: String = "",
    /** Fingerprint de host (opcional, solo validación). */
    val hostKeyFingerprint: String = "",

    /** "server_root", "user_home" o "specified". */
    val rootMode: String = "specified",
    /** Ruta remota (si rootMode == "specified"). */
    val remoteFolder: String = "/",

    val readOnly: Boolean = false,
    val openFolderOnConnect: Boolean = true,
    /** Subruta a abrir al conectar (p.ej. "/"). */
    val openFolderPath: String = "/"
)
