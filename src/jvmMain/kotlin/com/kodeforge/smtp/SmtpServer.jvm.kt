package com.kodeforge.smtp

import org.subethamail.smtp.server.SMTPServer
import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.MessageContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.mail.Session
import javax.mail.internet.MimeMessage
import java.util.Properties

/**
 * Implementación JVM del servidor SMTP usando SubEthaSMTP.
 */
actual class SmtpServer {
    
    private var server: SMTPServer? = null
    private var currentAllowedRecipients: List<String> = emptyList()
    private var currentOnEmailReceived: ((EmailReceived) -> Unit)? = null
    
    actual fun start(
        host: String,
        port: Int,
        allowedRecipients: List<String>,
        onEmailReceived: (EmailReceived) -> Unit
    ) {
        // Detener servidor existente si hay uno
        stop()
        
        currentAllowedRecipients = allowedRecipients
        currentOnEmailReceived = onEmailReceived
        
        // Crear factory de handlers
        val handlerFactory = object : MessageHandlerFactory {
            override fun create(ctx: MessageContext): MessageHandler {
                return KodeForgeMessageHandler(
                    allowedRecipients = currentAllowedRecipients,
                    onEmailReceived = currentOnEmailReceived!!
                )
            }
        }
        
        // Crear y configurar servidor
        server = SMTPServer(handlerFactory)
        server!!.port = port
        server!!.hostName = host
        
        // Iniciar servidor
        server?.start()
        
        println("[SMTP Server] Started on $host:$port")
        if (allowedRecipients.isNotEmpty()) {
            println("[SMTP Server] Allowed recipients: ${allowedRecipients.joinToString(", ")}")
        } else {
            println("[SMTP Server] Accepting all recipients")
        }
    }
    
    actual fun stop() {
        server?.let {
            if (it.isRunning) {
                it.stop()
                println("[SMTP Server] Stopped")
            }
        }
        server = null
        currentOnEmailReceived = null
    }
    
    actual fun isRunning(): Boolean {
        return server?.isRunning ?: false
    }
}

/**
 * Handler de mensajes SMTP.
 */
private class KodeForgeMessageHandler(
    private val allowedRecipients: List<String>,
    private val onEmailReceived: (EmailReceived) -> Unit
) : MessageHandler {
    
    private var from: String = ""
    private val recipients = mutableListOf<String>()
    
    override fun from(from: String) {
        this.from = from
        println("[SMTP] FROM: $from")
    }
    
    override fun recipient(recipient: String) {
        // Validar si el destinatario está permitido
        if (allowedRecipients.isNotEmpty()) {
            val isAllowed = allowedRecipients.any { allowed ->
                recipient.contains(allowed, ignoreCase = true)
            }
            
            if (!isAllowed) {
                println("[SMTP] REJECTED recipient: $recipient (not in allowed list)")
                // SubEthaSMTP no tiene forma directa de rechazar, pero podemos no añadirlo
                return
            }
        }
        
        recipients.add(recipient)
        println("[SMTP] TO: $recipient")
    }
    
    override fun data(data: InputStream) {
        try {
            // Si no hay destinatarios válidos, ignorar el email
            if (recipients.isEmpty()) {
                println("[SMTP] Email ignored: no valid recipients")
                return
            }
            
            // Parsear el mensaje MIME
            val session = Session.getDefaultInstance(Properties())
            val message = MimeMessage(session, data)
            
            // Extraer información
            val subject = message.subject ?: "(Sin asunto)"
            val bodyText = extractBodyText(message)
            val headers = extractHeaders(message)
            
            println("[SMTP] Subject: $subject")
            println("[SMTP] Body length: ${bodyText.length} chars")
            
            // Crear objeto EmailReceived
            val emailReceived = EmailReceived(
                from = from,
                to = recipients.toList(),
                subject = subject,
                bodyText = bodyText,
                headers = headers
            )
            
            // Notificar
            onEmailReceived(emailReceived)
            
            println("[SMTP] Email captured successfully")
            
        } catch (e: Exception) {
            println("[SMTP] Error processing email: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override fun done() {
        // Limpiar estado
        from = ""
        recipients.clear()
    }
    
    /**
     * Extrae el texto del body del mensaje.
     */
    private fun extractBodyText(message: MimeMessage): String {
        return try {
            val content = message.content
            when (content) {
                is String -> content
                is InputStream -> {
                    val reader = BufferedReader(InputStreamReader(content))
                    reader.readText()
                }
                else -> content.toString()
            }
        } catch (e: Exception) {
            "(Error al leer body: ${e.message})"
        }
    }
    
    /**
     * Extrae los headers del mensaje.
     */
    private fun extractHeaders(message: MimeMessage): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        
        try {
            val allHeadersEnum = message.allHeaders
            while (allHeadersEnum.hasMoreElements()) {
                val header = allHeadersEnum.nextElement() as javax.mail.Header
                headers[header.name] = header.value
            }
        } catch (e: Exception) {
            println("[SMTP] Error extracting headers: ${e.message}")
        }
        
        return headers
    }
}

