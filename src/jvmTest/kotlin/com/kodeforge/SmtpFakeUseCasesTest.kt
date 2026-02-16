package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SmtpFakeUseCases
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests para SmtpFakeUseCases.
 */
class SmtpFakeUseCasesTest {
    
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
                    id = "proj_test",
                    name = "Test Project",
                    description = "Test",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools()
                )
            ),
            tasks = emptyList()
        )
    }
    
    // ===== Configuration Tests =====
    
    @Test
    fun `enableSmtpServer - enables server with default config`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val result = useCases.enableSmtpServer(workspace, "proj_test")
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(true, smtp.enabled)
        assertEquals("127.0.0.1", smtp.listenHost)
        assertEquals(2525, smtp.listenPort)
    }
    
    @Test
    fun `enableSmtpServer - enables server with custom config`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val result = useCases.enableSmtpServer(
            workspace = workspace,
            projectId = "proj_test",
            listenHost = "0.0.0.0",
            listenPort = 1025
        )
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(true, smtp.enabled)
        assertEquals("0.0.0.0", smtp.listenHost)
        assertEquals(1025, smtp.listenPort)
    }
    
    @Test
    fun `enableSmtpServer - fails with invalid port`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val result = useCases.enableSmtpServer(
            workspace = workspace,
            projectId = "proj_test",
            listenPort = 99999
        )
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `disableSmtpServer - disables enabled server`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        // Primero habilitar
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        // Luego deshabilitar
        val result = useCases.disableSmtpServer(enabled, "proj_test")
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(false, smtp.enabled)
    }
    
    @Test
    fun `updateSmtpConfig - updates host and port`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        // Habilitar primero
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        // Actualizar config
        val result = useCases.updateSmtpConfig(
            workspace = enabled,
            projectId = "proj_test",
            listenHost = "localhost",
            listenPort = 3025
        )
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals("localhost", smtp.listenHost)
        assertEquals(3025, smtp.listenPort)
    }
    
    // ===== Allowed Recipients Tests =====
    
    @Test
    fun `addAllowedRecipient - adds valid email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        // Habilitar primero
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        // Añadir email
        val result = useCases.addAllowedRecipient(
            workspace = enabled,
            projectId = "proj_test",
            email = "dev@local.test"
        )
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(1, smtp.allowedRecipients.size)
        assertEquals("dev@local.test", smtp.allowedRecipients[0])
    }
    
    @Test
    fun `addAllowedRecipient - fails with invalid email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        val result = useCases.addAllowedRecipient(
            workspace = enabled,
            projectId = "proj_test",
            email = "invalid-email"
        )
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `addAllowedRecipient - fails with duplicate email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        val added = useCases.addAllowedRecipient(enabled, "proj_test", "dev@local.test").getOrThrow()
        
        // Intentar añadir duplicado
        val result = useCases.addAllowedRecipient(added, "proj_test", "dev@local.test")
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `removeAllowedRecipient - removes existing email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        val added = useCases.addAllowedRecipient(enabled, "proj_test", "dev@local.test").getOrThrow()
        
        // Eliminar
        val result = useCases.removeAllowedRecipient(added, "proj_test", "dev@local.test")
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(0, smtp.allowedRecipients.size)
    }
    
    @Test
    fun `getAllowedRecipients - returns list`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        val added1 = useCases.addAllowedRecipient(enabled, "proj_test", "dev@local.test").getOrThrow()
        val added2 = useCases.addAllowedRecipient(added1, "proj_test", "qa@local.test").getOrThrow()
        
        val recipients = useCases.getAllowedRecipients(added2, "proj_test")
        
        assertEquals(2, recipients.size)
        assertTrue(recipients.contains("dev@local.test"))
        assertTrue(recipients.contains("qa@local.test"))
    }
    
    // ===== Stored Inbox Tests =====
    
    @Test
    fun `addEmailToInbox - adds valid email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        val result = useCases.addEmailToInbox(
            workspace = enabled,
            projectId = "proj_test",
            from = "sender@example.com",
            to = listOf("dev@local.test"),
            subject = "Test Email",
            bodyText = "This is a test email.",
            headers = mapOf("X-Test" to "value")
        )
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(1, smtp.storedInbox.size)
        
        val email = smtp.storedInbox[0]
        assertEquals("sender@example.com", email.from)
        assertEquals(1, email.to.size)
        assertEquals("dev@local.test", email.to[0])
        assertEquals("Test Email", email.subject)
        assertEquals("This is a test email.", email.bodyText)
        assertEquals("value", email.headers["X-Test"])
    }
    
    @Test
    fun `addEmailToInbox - fails with invalid from email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        val result = useCases.addEmailToInbox(
            workspace = enabled,
            projectId = "proj_test",
            from = "invalid",
            to = listOf("dev@local.test"),
            subject = "Test",
            bodyText = "Test"
        )
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `addEmailToInbox - fails with invalid to email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        val result = useCases.addEmailToInbox(
            workspace = enabled,
            projectId = "proj_test",
            from = "sender@example.com",
            to = listOf("invalid"),
            subject = "Test",
            bodyText = "Test"
        )
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `deleteEmailFromInbox - deletes existing email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        val added = useCases.addEmailToInbox(
            workspace = enabled,
            projectId = "proj_test",
            from = "sender@example.com",
            to = listOf("dev@local.test"),
            subject = "Test",
            bodyText = "Test"
        ).getOrThrow()
        
        val emailId = added.projects[0].tools.smtpFake!!.storedInbox[0].id
        
        // Eliminar
        val result = useCases.deleteEmailFromInbox(added, "proj_test", emailId)
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(0, smtp.storedInbox.size)
    }
    
    @Test
    fun `clearInbox - clears all emails`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        // Añadir múltiples emails
        var current = enabled
        for (i in 1..3) {
            current = useCases.addEmailToInbox(
                workspace = current,
                projectId = "proj_test",
                from = "sender$i@example.com",
                to = listOf("dev@local.test"),
                subject = "Test $i",
                bodyText = "Test $i"
            ).getOrThrow()
        }
        
        assertEquals(3, current.projects[0].tools.smtpFake!!.storedInbox.size)
        
        // Limpiar
        val result = useCases.clearInbox(current, "proj_test")
        
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        val smtp = updated.projects[0].tools.smtpFake
        
        assertNotNull(smtp)
        assertEquals(0, smtp.storedInbox.size)
    }
    
    @Test
    fun `getInbox - returns all emails`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        var current = enabled
        for (i in 1..2) {
            current = useCases.addEmailToInbox(
                workspace = current,
                projectId = "proj_test",
                from = "sender$i@example.com",
                to = listOf("dev@local.test"),
                subject = "Test $i",
                bodyText = "Test $i"
            ).getOrThrow()
        }
        
        val inbox = useCases.getInbox(current, "proj_test")
        
        assertEquals(2, inbox.size)
        assertEquals("sender1@example.com", inbox[0].from)
        assertEquals("sender2@example.com", inbox[1].from)
    }
    
    @Test
    fun `getEmailById - returns specific email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        val added = useCases.addEmailToInbox(
            workspace = enabled,
            projectId = "proj_test",
            from = "sender@example.com",
            to = listOf("dev@local.test"),
            subject = "Test Email",
            bodyText = "Test"
        ).getOrThrow()
        
        val emailId = added.projects[0].tools.smtpFake!!.storedInbox[0].id
        
        val email = useCases.getEmailById(added, "proj_test", emailId)
        
        assertNotNull(email)
        assertEquals(emailId, email.id)
        assertEquals("sender@example.com", email.from)
    }
    
    @Test
    fun `getEmailById - returns null for non-existent email`() {
        val workspace = createTestWorkspace()
        val useCases = SmtpFakeUseCases()
        
        val enabled = useCases.enableSmtpServer(workspace, "proj_test").getOrThrow()
        
        val email = useCases.getEmailById(enabled, "proj_test", "non_existent")
        
        assertNull(email)
    }
}

