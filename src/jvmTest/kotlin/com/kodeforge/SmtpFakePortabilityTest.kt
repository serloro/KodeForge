package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SmtpFakeUseCases
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests de portabilidad para herramienta SMTP Fake.
 * 
 * Valida que al copiar el workspace JSON:
 * - La configuración SMTP se recupera exactamente igual
 * - Los destinatarios permitidos se preservan
 * - El inbox almacenado se preserva
 * 
 * Flujo: load → save → reload → assert
 */
class SmtpFakePortabilityTest {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
    }
    
    private fun createWorkspaceWithSmtp(): Workspace {
        val project = Project(
            id = "proj_test",
            name = "Test Project",
            description = "Project for SMTP portability testing",
            status = "active",
            members = emptyList(),
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z",
            tools = ProjectTools(
                smtpFake = SmtpFakeTool(
                    enabled = true,
                    listenHost = "127.0.0.1",
                    listenPort = 2525,
                    allowedRecipients = listOf("dev@local.test", "qa@local.test"),
                    storedInbox = listOf(
                        EmailMessage(
                            id = "mail_001",
                            receivedAt = "2026-02-15T14:22:00Z",
                            from = "noreply@local.test",
                            to = listOf("dev@local.test"),
                            subject = "Build finished",
                            bodyText = "Your build finished successfully.",
                            headers = mapOf("x-env" to "local")
                        ),
                        EmailMessage(
                            id = "mail_002",
                            receivedAt = "2026-02-15T15:10:00Z",
                            from = "alerts@system.test",
                            to = listOf("dev@local.test", "qa@local.test"),
                            subject = "System Alert",
                            bodyText = "Disk usage is at 85%",
                            headers = mapOf(
                                "x-priority" to "high",
                                "x-category" to "alert"
                            )
                        )
                    )
                )
            )
        )
        
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T12:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(project),
            tasks = emptyList()
        )
    }
    
    @Test
    fun `portable persistence - smtp config survives save and reload`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithSmtp()
        val smtp1 = workspace1.projects[0].tools.smtpFake!!
        
        assertNotNull(smtp1)
        assertEquals(true, smtp1.enabled)
        assertEquals("127.0.0.1", smtp1.listenHost)
        assertEquals(2525, smtp1.listenPort)
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val smtp2 = workspace2.projects[0].tools.smtpFake!!
        
        // 4. ASSERT: Configuración idéntica
        assertEquals(smtp1.enabled, smtp2.enabled)
        assertEquals(smtp1.listenHost, smtp2.listenHost)
        assertEquals(smtp1.listenPort, smtp2.listenPort)
    }
    
    @Test
    fun `portable persistence - allowed recipients preserved`() {
        // 1. LOAD
        val workspace1 = createWorkspaceWithSmtp()
        val recipients1 = workspace1.projects[0].tools.smtpFake?.allowedRecipients!!
        
        assertEquals(2, recipients1.size)
        
        // 2. SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val recipients2 = workspace2.projects[0].tools.smtpFake?.allowedRecipients!!
        
        // 4. ASSERT: Lista idéntica
        assertEquals(recipients1.size, recipients2.size)
        assertEquals(recipients1, recipients2)
        
        // Verificar contenido específico
        assertEquals("dev@local.test", recipients2[0])
        assertEquals("qa@local.test", recipients2[1])
    }
    
    @Test
    fun `portable persistence - stored inbox preserved`() {
        // 1. LOAD
        val workspace1 = createWorkspaceWithSmtp()
        val inbox1 = workspace1.projects[0].tools.smtpFake?.storedInbox!!
        
        assertEquals(2, inbox1.size)
        
        // 2. SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val inbox2 = workspace2.projects[0].tools.smtpFake?.storedInbox!!
        
        // 4. ASSERT: Inbox idéntico
        assertEquals(inbox1.size, inbox2.size)
        
        for (i in inbox1.indices) {
            val email1 = inbox1[i]
            val email2 = inbox2[i]
            
            assertEquals(email1.id, email2.id)
            assertEquals(email1.receivedAt, email2.receivedAt)
            assertEquals(email1.from, email2.from)
            assertEquals(email1.to, email2.to)
            assertEquals(email1.subject, email2.subject)
            assertEquals(email1.bodyText, email2.bodyText)
            assertEquals(email1.headers, email2.headers)
        }
        
        // Verificar contenido específico
        assertEquals("mail_001", inbox2[0].id)
        assertEquals("noreply@local.test", inbox2[0].from)
        assertEquals("Build finished", inbox2[0].subject)
        
        assertEquals("mail_002", inbox2[1].id)
        assertEquals("alerts@system.test", inbox2[1].from)
        assertEquals(2, inbox2[1].to.size)
        assertEquals("high", inbox2[1].headers["x-priority"])
    }
    
    @Test
    fun `portable persistence - complete workflow with use cases`() {
        // Flujo completo: load -> enable -> add recipients -> add emails -> save -> reload -> assert
        
        // 1. LOAD: Crear workspace inicial vacío
        val workspace1 = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_workflow",
                    name = "Workflow Test",
                    description = "Test complete workflow",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools()
                )
            ),
            tasks = emptyList()
        )
        
        val useCases = SmtpFakeUseCases()
        
        // 2. ENABLE: Habilitar servidor SMTP
        val workspace2 = useCases.enableSmtpServer(
            workspace = workspace1,
            projectId = "proj_workflow",
            listenHost = "0.0.0.0",
            listenPort = 1025
        ).getOrThrow()
        
        // 3. ADD RECIPIENTS: Añadir destinatarios permitidos
        val workspace3 = useCases.addAllowedRecipient(
            workspace = workspace2,
            projectId = "proj_workflow",
            email = "admin@example.com"
        ).getOrThrow()
        
        val workspace4 = useCases.addAllowedRecipient(
            workspace = workspace3,
            projectId = "proj_workflow",
            email = "support@example.com"
        ).getOrThrow()
        
        // 4. ADD EMAILS: Añadir emails al inbox
        val workspace5 = useCases.addEmailToInbox(
            workspace = workspace4,
            projectId = "proj_workflow",
            from = "user@example.com",
            to = listOf("admin@example.com"),
            subject = "Help Request",
            bodyText = "I need help with my account.",
            headers = mapOf("X-Request-ID" to "req-123")
        ).getOrThrow()
        
        val workspace6 = useCases.addEmailToInbox(
            workspace = workspace5,
            projectId = "proj_workflow",
            from = "system@example.com",
            to = listOf("admin@example.com", "support@example.com"),
            subject = "Daily Report",
            bodyText = "Summary of today's activities:\n- 10 new users\n- 5 support tickets",
            headers = mapOf(
                "X-Report-Type" to "daily",
                "X-Generated-At" to "2026-02-16T23:00:00Z"
            )
        ).getOrThrow()
        
        // 5. SAVE: Serializar a JSON
        val jsonString = json.encodeToString(workspace6)
        
        // 6. RELOAD: Deserializar desde JSON
        val workspaceReloaded = json.decodeFromString<Workspace>(jsonString)
        
        // 7. ASSERT: Verificar que TODO se preservó exactamente
        val project = workspaceReloaded.projects.find { it.id == "proj_workflow" }
        assertNotNull(project)
        
        val smtp = project.tools.smtpFake
        assertNotNull(smtp)
        
        // Verificar configuración
        assertEquals(true, smtp.enabled)
        assertEquals("0.0.0.0", smtp.listenHost)
        assertEquals(1025, smtp.listenPort)
        
        // Verificar destinatarios permitidos
        assertEquals(2, smtp.allowedRecipients.size)
        assertEquals("admin@example.com", smtp.allowedRecipients[0])
        assertEquals("support@example.com", smtp.allowedRecipients[1])
        
        // Verificar inbox
        assertEquals(2, smtp.storedInbox.size)
        
        val email1 = smtp.storedInbox[0]
        assertEquals("user@example.com", email1.from)
        assertEquals(1, email1.to.size)
        assertEquals("admin@example.com", email1.to[0])
        assertEquals("Help Request", email1.subject)
        assertEquals("I need help with my account.", email1.bodyText)
        assertEquals("req-123", email1.headers["X-Request-ID"])
        
        val email2 = smtp.storedInbox[1]
        assertEquals("system@example.com", email2.from)
        assertEquals(2, email2.to.size)
        assertEquals("Daily Report", email2.subject)
        assert(email2.bodyText.contains("10 new users"))
        assertEquals("daily", email2.headers["X-Report-Type"])
    }
    
    @Test
    fun `portable persistence - complex email content preserved`() {
        // Test con contenido complejo en emails
        
        val workspace1 = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_complex",
                    name = "Complex Test",
                    description = "Test complex data",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        smtpFake = SmtpFakeTool(
                            enabled = true,
                            listenHost = "127.0.0.1",
                            listenPort = 2525,
                            allowedRecipients = listOf(
                                "user+tag@example.com",
                                "first.last@sub.domain.com"
                            ),
                            storedInbox = listOf(
                                EmailMessage(
                                    id = "mail_complex",
                                    receivedAt = "2026-02-16T10:00:00Z",
                                    from = "noreply@example.com",
                                    to = listOf("user+tag@example.com"),
                                    subject = "Email with \"quotes\" and special chars: <>&",
                                    bodyText = """
                                        Hello,
                                        
                                        This is a test email with:
                                        - Multiple lines
                                        - Special characters: <>&"'
                                        - Unicode: 你好 🎉
                                        - Tabs:	indented
                                        
                                        Best regards,
                                        System
                                    """.trimIndent(),
                                    headers = mapOf(
                                        "Content-Type" to "text/plain; charset=utf-8",
                                        "X-Custom-Header" to "value with spaces",
                                        "X-List" to "item1,item2,item3"
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            tasks = emptyList()
        )
        
        // SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        
        // ASSERT: Verificar datos complejos
        val smtp = workspace2.projects[0].tools.smtpFake!!
        
        // Recipients con caracteres especiales
        assertEquals("user+tag@example.com", smtp.allowedRecipients[0])
        assertEquals("first.last@sub.domain.com", smtp.allowedRecipients[1])
        
        // Email con contenido complejo
        val email = smtp.storedInbox[0]
        assert(email.subject.contains("\"quotes\""))
        assert(email.subject.contains("<>&"))
        assert(email.bodyText.contains("Multiple lines"))
        assert(email.bodyText.contains("你好"))
        assert(email.bodyText.contains("🎉"))
        assert(email.bodyText.contains("Special characters: <>&\"'"))
        
        // Headers complejos
        assertEquals("text/plain; charset=utf-8", email.headers["Content-Type"])
        assertEquals("value with spaces", email.headers["X-Custom-Header"])
        assertEquals("item1,item2,item3", email.headers["X-List"])
    }
    
    @Test
    fun `portable persistence - empty values preserved`() {
        // Test casos edge: valores vacíos
        
        val workspace1 = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_empty",
                    name = "Empty Test",
                    description = "Test empty values",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        smtpFake = SmtpFakeTool(
                            enabled = false,
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
        
        // SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        
        // ASSERT
        val smtp = workspace2.projects[0].tools.smtpFake!!
        
        assertEquals(false, smtp.enabled)
        assertEquals(0, smtp.allowedRecipients.size)
        assertEquals(0, smtp.storedInbox.size)
    }
}

