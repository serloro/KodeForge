package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.smtp.SmtpServerManager
import org.junit.jupiter.api.Test
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para el servidor SMTP real.
 * 
 * Valida que:
 * - El servidor se inicia correctamente
 * - Los emails se capturan y guardan en el workspace
 * - Los destinatarios permitidos se respetan
 */
class SmtpServerTest {
    
    private fun createTestWorkspace(): Workspace {
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_smtp_test",
                    name = "SMTP Test Project",
                    description = "Test",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        smtpFake = SmtpFakeTool(
                            enabled = true,
                            listenHost = "127.0.0.1",
                            listenPort = 2526, // Puerto diferente para tests
                            allowedRecipients = emptyList(),
                            storedInbox = emptyList()
                        )
                    )
                )
            ),
            tasks = emptyList()
        )
    }
    
    @Test
    fun `smtp server - receives and stores email`() {
        val manager = SmtpServerManager()
        var updatedWorkspace = createTestWorkspace()
        
        try {
            // Iniciar servidor
            manager.startServer(
                projectId = "proj_smtp_test",
                workspace = updatedWorkspace,
                host = "127.0.0.1",
                port = 2526,
                allowedRecipients = emptyList(),
                onWorkspaceUpdate = { ws -> updatedWorkspace = ws }
            )
            
            // Esperar a que el servidor esté listo
            Thread.sleep(500)
            
            assertTrue(manager.isServerRunning("proj_smtp_test"))
            
            // Enviar email de prueba
            sendTestEmail(
                host = "127.0.0.1",
                port = 2526,
                from = "sender@example.com",
                to = "recipient@example.com",
                subject = "Test Email",
                body = "This is a test email body."
            )
            
            // Esperar a que se procese
            Thread.sleep(1000)
            
            // Verificar que el email se guardó
            val inbox = updatedWorkspace.projects[0].tools.smtpFake?.storedInbox ?: emptyList()
            
            assertEquals(1, inbox.size, "Debe haber 1 email en el inbox")
            
            val email = inbox[0]
            assertEquals("sender@example.com", email.from)
            assertTrue(email.to.contains("recipient@example.com"))
            assertEquals("Test Email", email.subject)
            assertTrue(email.bodyText.contains("This is a test email body"))
            
        } finally {
            // Detener servidor
            manager.stopServer("proj_smtp_test")
        }
    }
    
    @Test
    fun `smtp server - respects allowed recipients`() {
        val manager = SmtpServerManager()
        var updatedWorkspace = createTestWorkspace()
        
        try {
            // Iniciar servidor con destinatarios permitidos
            manager.startServer(
                projectId = "proj_smtp_test",
                workspace = updatedWorkspace,
                host = "127.0.0.1",
                port = 2527, // Puerto diferente
                allowedRecipients = listOf("allowed@example.com"),
                onWorkspaceUpdate = { ws -> updatedWorkspace = ws }
            )
            
            Thread.sleep(500)
            
            // Enviar email a destinatario permitido
            sendTestEmail(
                host = "127.0.0.1",
                port = 2527,
                from = "sender@example.com",
                to = "allowed@example.com",
                subject = "Allowed Email",
                body = "This should be received."
            )
            
            Thread.sleep(1000)
            
            // Verificar que se guardó
            var inbox = updatedWorkspace.projects[0].tools.smtpFake?.storedInbox ?: emptyList()
            assertEquals(1, inbox.size, "Email a destinatario permitido debe guardarse")
            
            // Enviar email a destinatario NO permitido
            sendTestEmail(
                host = "127.0.0.1",
                port = 2527,
                from = "sender@example.com",
                to = "notallowed@example.com",
                subject = "Not Allowed Email",
                body = "This should be rejected."
            )
            
            Thread.sleep(1000)
            
            // Verificar que NO se guardó
            inbox = updatedWorkspace.projects[0].tools.smtpFake?.storedInbox ?: emptyList()
            assertEquals(1, inbox.size, "Email a destinatario no permitido NO debe guardarse")
            
        } finally {
            manager.stopServer("proj_smtp_test")
        }
    }
    
    /**
     * Envía un email de prueba usando JavaMail.
     */
    private fun sendTestEmail(
        host: String,
        port: Int,
        from: String,
        to: String,
        subject: String,
        body: String
    ) {
        val props = Properties()
        props["mail.smtp.host"] = host
        props["mail.smtp.port"] = port.toString()
        props["mail.smtp.auth"] = "false"
        
        val session = Session.getInstance(props)
        
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(from))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
        message.subject = subject
        message.setText(body)
        
        Transport.send(message)
        
        println("[Test] Email sent: $from -> $to")
    }
}

