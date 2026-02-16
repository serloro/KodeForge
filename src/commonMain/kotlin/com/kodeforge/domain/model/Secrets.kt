package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

/**
 * Secrets - Referencias a secretos (passwords, tokens, etc.)
 * En MVP se guardan en texto plano con warning.
 * En producción se usaría Keychain/Credential Manager por plataforma.
 */
@Serializable
data class Secrets(
    val storageMode: String = "local", // local (texto plano), keychain (futuro)
    val items: List<SecretItem> = emptyList()
)

@Serializable
data class SecretItem(
    val key: String, // ej: "secret:sftp_001"
    val type: String = "placeholder", // placeholder, password, token
    val note: String? = null,
    val value: String? = null // ⚠️ TEXTO PLANO en MVP - migrar a keychain en producción
)

