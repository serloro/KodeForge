package com.kodeforge.smtp

/**
 * Interfaz para servidor SMTP multiplataforma.
 * 
 * Implementación:
 * - JVM: SubEthaSMTP (servidor real)
 * - Otros targets: No implementado (placeholder)
 */
expect class SmtpServer {
    /**
     * Inicia el servidor SMTP.
     * 
     * @param host Host donde escuchar (ej: "127.0.0.1", "0.0.0.0")
     * @param port Puerto donde escuchar (ej: 2525)
     * @param allowedRecipients Lista de emails permitidos (vacío = todos)
     * @param onEmailReceived Callback cuando se recibe un email
     */
    fun start(
        host: String,
        port: Int,
        allowedRecipients: List<String>,
        onEmailReceived: (EmailReceived) -> Unit
    )
    
    /**
     * Detiene el servidor SMTP.
     */
    fun stop()
    
    /**
     * Indica si el servidor está corriendo.
     */
    fun isRunning(): Boolean
}

/**
 * Datos de un email recibido.
 */
data class EmailReceived(
    val from: String,
    val to: List<String>,
    val subject: String,
    val bodyText: String,
    val headers: Map<String, String>
)

