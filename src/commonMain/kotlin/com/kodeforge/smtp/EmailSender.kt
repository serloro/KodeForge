package com.kodeforge.smtp

import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.SmtpFakeUseCases
import kotlinx.datetime.Clock

/**
 * Servicio para enviar emails.
 * 
 * MVP: Simula el envío inyectando el email directamente en el inbox.
 * Futuro: Implementar envío real con JavaMail (JVM) o cliente SMTP.
 */
class EmailSender {
    
    private val useCases = SmtpFakeUseCases()
    
    /**
     * Envía un email (simulado en MVP).
     * 
     * Comportamiento actual:
     * - Valida que el destinatario esté en allowedRecipients (si está definido)
     * - Inyecta el email directamente en el inbox del proyecto
     * - Simula un "from" del usuario
     * 
     * Futuro:
     * - Enviar al servidor SMTP local (si está corriendo)
     * - Enviar a servidor SMTP externo
     * - Usar JavaMail para envío real
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto
     * @param from Remitente (simulado)
     * @param to Destinatario
     * @param subject Asunto
     * @param body Cuerpo del email
     * @return Workspace actualizado o error
     */
    fun sendEmail(
        workspace: Workspace,
        projectId: String,
        from: String,
        to: String,
        subject: String,
        body: String
    ): Result<Workspace> {
        try {
            // Obtener configuración SMTP del proyecto
            val project = workspace.projects.find { it.id == projectId }
                ?: return Result.failure(Exception("Proyecto no encontrado"))
            
            val smtpTool = project.tools.smtpFake
            
            // Si hay destinatarios permitidos, validar
            if (smtpTool != null && smtpTool.allowedRecipients.isNotEmpty()) {
                val isAllowed = smtpTool.allowedRecipients.any { allowed ->
                    to.equals(allowed, ignoreCase = true)
                }
                
                if (!isAllowed) {
                    return Result.failure(
                        Exception("Destinatario no permitido. Solo se permiten: ${smtpTool.allowedRecipients.joinToString(", ")}")
                    )
                }
            }
            
            // MVP: Simular envío inyectando directamente en el inbox
            val result = useCases.addEmailToInbox(
                workspace = workspace,
                projectId = projectId,
                from = from,
                to = listOf(to),
                subject = subject,
                bodyText = body,
                headers = mapOf(
                    "X-Mailer" to "KodeForge SMTP Fake",
                    "X-Send-Method" to "simulated",
                    "Date" to Clock.System.now().toString()
                )
            )
            
            println("[EmailSender] Email sent (simulated): $from -> $to")
            
            return result
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Envía un email real usando JavaMail (JVM only).
     * 
     * TODO: Implementar en el futuro
     * - Usar Transport.send() de JavaMail
     * - Conectar al servidor SMTP local o externo
     * - Manejar autenticación si es necesario
     */
    fun sendEmailReal(
        host: String,
        port: Int,
        from: String,
        to: String,
        subject: String,
        body: String,
        username: String? = null,
        password: String? = null
    ): Result<Unit> {
        // TODO: Implementar envío real
        return Result.failure(Exception("Envío real no implementado aún"))
    }
}

