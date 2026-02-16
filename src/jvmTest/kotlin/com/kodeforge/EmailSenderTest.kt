package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.smtp.EmailSender
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para EmailSender.
 */
class EmailSenderTest {
    
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
                    id = "proj_sender_test",
                    name = "Sender Test Project",
                    description = "Test",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        smtpFake = SmtpFakeTool(
                            enabled = true,
                            listenHost = "127.0.0.1",
                            listenPort = 2525,
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
    fun `sendEmail - sends and stores email in inbox`() {
        val workspace = createTestWorkspace()
        val sender = EmailSender()
        
        val result = sender.sendEmail(
            workspace = workspace,
            projectId = "proj_sender_test",
            from = "sender@example.com",
            to = "recipient@example.com",
            subject = "Test Email",
            body = "This is a test email body."
        )
        
        assertTrue(result.isSuccess)
        
        val updatedWorkspace = result.getOrThrow()
        val inbox = updatedWorkspace.projects[0].tools.smtpFake?.storedInbox ?: emptyList()
        
        assertEquals(1, inbox.size)
        
        val email = inbox[0]
        assertEquals("sender@example.com", email.from)
        assertEquals(1, email.to.size)
        assertEquals("recipient@example.com", email.to[0])
        assertEquals("Test Email", email.subject)
        assertEquals("This is a test email body.", email.bodyText)
        
        // Verificar headers especiales
        assertTrue(email.headers.containsKey("X-Mailer"))
        assertEquals("KodeForge SMTP Fake", email.headers["X-Mailer"])
        assertEquals("simulated", email.headers["X-Send-Method"])
    }
    
    @Test
    fun `sendEmail - respects allowed recipients`() {
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_restricted",
                    name = "Restricted Project",
                    description = "Test",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        smtpFake = SmtpFakeTool(
                            enabled = true,
                            listenHost = "127.0.0.1",
                            listenPort = 2525,
                            allowedRecipients = listOf("allowed@example.com"),
                            storedInbox = emptyList()
                        )
                    )
                )
            ),
            tasks = emptyList()
        )
        
        val sender = EmailSender()
        
        // Enviar a destinatario permitido
        val result1 = sender.sendEmail(
            workspace = workspace,
            projectId = "proj_restricted",
            from = "sender@example.com",
            to = "allowed@example.com",
            subject = "Allowed Email",
            body = "This should work."
        )
        
        assertTrue(result1.isSuccess)
        
        // Enviar a destinatario NO permitido
        val result2 = sender.sendEmail(
            workspace = workspace,
            projectId = "proj_restricted",
            from = "sender@example.com",
            to = "notallowed@example.com",
            subject = "Not Allowed Email",
            body = "This should fail."
        )
        
        assertTrue(result2.isFailure)
        assertTrue(result2.exceptionOrNull()?.message?.contains("no permitido") == true)
    }
    
    @Test
    fun `sendEmail - multiple emails accumulate in inbox`() {
        val workspace = createTestWorkspace()
        val sender = EmailSender()
        
        // Enviar primer email
        val result1 = sender.sendEmail(
            workspace = workspace,
            projectId = "proj_sender_test",
            from = "sender1@example.com",
            to = "recipient1@example.com",
            subject = "Email 1",
            body = "First email"
        )
        
        assertTrue(result1.isSuccess)
        val workspace1 = result1.getOrThrow()
        
        // Enviar segundo email
        val result2 = sender.sendEmail(
            workspace = workspace1,
            projectId = "proj_sender_test",
            from = "sender2@example.com",
            to = "recipient2@example.com",
            subject = "Email 2",
            body = "Second email"
        )
        
        assertTrue(result2.isSuccess)
        val workspace2 = result2.getOrThrow()
        
        // Verificar que ambos están en el inbox
        val inbox = workspace2.projects[0].tools.smtpFake?.storedInbox ?: emptyList()
        
        assertEquals(2, inbox.size)
        assertEquals("Email 1", inbox[0].subject)
        assertEquals("Email 2", inbox[1].subject)
    }
}

