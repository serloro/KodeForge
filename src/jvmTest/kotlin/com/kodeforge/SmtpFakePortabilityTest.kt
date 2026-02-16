package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SmtpFakeUseCases
import com.kodeforge.smtp.EmailSender
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.data.repository.JvmFileSystemAdapter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Tests de portabilidad para SMTP Fake.
 * 
 * Valida que al copiar el workspace JSON, toda la configuración,
 * destinatarios permitidos y emails capturados se recuperan exactamente igual.
 */
class SmtpFakePortabilityTest {
    
    @TempDir
    lateinit var tempDir: Path
    
    private fun createTestWorkspace(): Workspace {
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_smtp_portability",
                    name = "SMTP Portability Test",
                    description = "Test project for SMTP portability",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T10:00:00Z",
                    updatedAt = "2026-02-16T10:00:00Z",
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
    }
    
    @Test
    fun `full workflow - config, recipients, and emails persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val smtpUseCases = SmtpFakeUseCases()
        val emailSender = EmailSender()
        
        // 1. Crear workspace inicial
        var workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        // 2. Habilitar SMTP con configuración específica
        workspace = smtpUseCases.enableSmtpServer(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            listenHost = "0.0.0.0",
            listenPort = 3025
        ).getOrThrow()
        
        // 3. Añadir destinatarios permitidos
        workspace = smtpUseCases.addAllowedRecipient(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            email = "allowed1@example.com"
        ).getOrThrow()
        
        workspace = smtpUseCases.addAllowedRecipient(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            email = "allowed2@test.org"
        ).getOrThrow()
        
        // 4. Simular captura de emails
        workspace = emailSender.sendEmail(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            from = "sender1@example.com",
            to = "allowed1@example.com",
            subject = "Test Email 1",
            body = "This is the first test email with special chars: áéíóú ñ"
        ).getOrThrow()
        
        workspace = emailSender.sendEmail(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            from = "sender2@test.org",
            to = "allowed2@test.org",
            subject = "Test Email 2 - Important",
            body = "Second email with\nmultiple\nlines\nand special: <html>&nbsp;</html>"
        ).getOrThrow()
        
        // 5. Guardar workspace
        repository.save(workspacePath, workspace)
        
        // 6. Recargar workspace (simula copiar JSON a otro sistema)
        val reloadedWorkspace = repository.load(workspacePath)
        
        // 7. Validar que TODO se recuperó exactamente igual
        val originalProject = workspace.projects.find { it.id == "proj_smtp_portability" }!!
        val reloadedProject = reloadedWorkspace.projects.find { it.id == "proj_smtp_portability" }!!
        
        val originalSmtp = originalProject.tools.smtpFake!!
        val reloadedSmtp = reloadedProject.tools.smtpFake!!
        
        // Configuración
        assertEquals(originalSmtp.enabled, reloadedSmtp.enabled)
        assertEquals(originalSmtp.listenHost, reloadedSmtp.listenHost)
        assertEquals(originalSmtp.listenPort, reloadedSmtp.listenPort)
        assertTrue(reloadedSmtp.enabled)
        assertEquals("0.0.0.0", reloadedSmtp.listenHost)
        assertEquals(3025, reloadedSmtp.listenPort)
        
        // Destinatarios permitidos
        assertEquals(originalSmtp.allowedRecipients.size, reloadedSmtp.allowedRecipients.size)
        assertEquals(2, reloadedSmtp.allowedRecipients.size)
        assertEquals(originalSmtp.allowedRecipients, reloadedSmtp.allowedRecipients)
        assertTrue(reloadedSmtp.allowedRecipients.contains("allowed1@example.com"))
        assertTrue(reloadedSmtp.allowedRecipients.contains("allowed2@test.org"))
        
        // Inbox
        assertEquals(originalSmtp.storedInbox.size, reloadedSmtp.storedInbox.size)
        assertEquals(2, reloadedSmtp.storedInbox.size)
        
        // Email 1
        val email1Original = originalSmtp.storedInbox[0]
        val email1Reloaded = reloadedSmtp.storedInbox[0]
        assertEquals(email1Original.id, email1Reloaded.id)
        assertEquals(email1Original.from, email1Reloaded.from)
        assertEquals(email1Original.to, email1Reloaded.to)
        assertEquals(email1Original.subject, email1Reloaded.subject)
        assertEquals(email1Original.bodyText, email1Reloaded.bodyText)
        assertEquals(email1Original.receivedAt, email1Reloaded.receivedAt)
        assertEquals(email1Original.headers, email1Reloaded.headers)
        
        // Verificar contenido específico del email 1
        assertEquals("sender1@example.com", email1Reloaded.from)
        assertEquals(listOf("allowed1@example.com"), email1Reloaded.to)
        assertEquals("Test Email 1", email1Reloaded.subject)
        assertTrue(email1Reloaded.bodyText.contains("áéíóú ñ"))
        
        // Email 2
        val email2Original = originalSmtp.storedInbox[1]
        val email2Reloaded = reloadedSmtp.storedInbox[1]
        assertEquals(email2Original.id, email2Reloaded.id)
        assertEquals(email2Original.from, email2Reloaded.from)
        assertEquals(email2Original.to, email2Reloaded.to)
        assertEquals(email2Original.subject, email2Reloaded.subject)
        assertEquals(email2Original.bodyText, email2Reloaded.bodyText)
        assertEquals(email2Original.receivedAt, email2Reloaded.receivedAt)
        assertEquals(email2Original.headers, email2Reloaded.headers)
        
        // Verificar contenido específico del email 2
        assertEquals("sender2@test.org", email2Reloaded.from)
        assertEquals(listOf("allowed2@test.org"), email2Reloaded.to)
        assertEquals("Test Email 2 - Important", email2Reloaded.subject)
        assertTrue(email2Reloaded.bodyText.contains("multiple\nlines"))
        assertTrue(email2Reloaded.bodyText.contains("<html>&nbsp;</html>"))
        
        // Headers especiales
        assertNotNull(email1Reloaded.headers["X-Mailer"])
        assertEquals("KodeForge SMTP Fake", email1Reloaded.headers["X-Mailer"])
        assertEquals("simulated", email1Reloaded.headers["X-Send-Method"])
        assertNotNull(email1Reloaded.headers["Date"])
    }
    
    @Test
    fun `empty configuration persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_empty.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        
        // Workspace con SMTP deshabilitado y sin datos
        val workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedSmtp = reloadedWorkspace.projects[0].tools.smtpFake!!
        
        assertEquals(false, reloadedSmtp.enabled)
        assertEquals("127.0.0.1", reloadedSmtp.listenHost)
        assertEquals(2525, reloadedSmtp.listenPort)
        assertEquals(0, reloadedSmtp.allowedRecipients.size)
        assertEquals(0, reloadedSmtp.storedInbox.size)
    }
    
    @Test
    fun `large inbox persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_large.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val emailSender = EmailSender()
        
        var workspace = createTestWorkspace()
        
        // Añadir 50 emails
        repeat(50) { i ->
            workspace = emailSender.sendEmail(
                workspace = workspace,
                projectId = "proj_smtp_portability",
                from = "sender$i@example.com",
                to = "recipient$i@example.com",
                subject = "Test Email $i",
                body = "Body of email number $i with some content"
            ).getOrThrow()
        }
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedInbox = reloadedWorkspace.projects[0].tools.smtpFake!!.storedInbox
        
        assertEquals(50, reloadedInbox.size)
        
        // Verificar primer y último email
        assertEquals("sender0@example.com", reloadedInbox[0].from)
        assertEquals("Test Email 0", reloadedInbox[0].subject)
        
        assertEquals("sender49@example.com", reloadedInbox[49].from)
        assertEquals("Test Email 49", reloadedInbox[49].subject)
        
        // Verificar que todos los IDs son únicos
        val ids = reloadedInbox.map { it.id }.toSet()
        assertEquals(50, ids.size)
    }
    
    @Test
    fun `special characters in emails persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_special.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val emailSender = EmailSender()
        
        var workspace = createTestWorkspace()
        
        // Email con caracteres especiales
        workspace = emailSender.sendEmail(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            from = "sender@example.com",
            to = "recipient@example.com",
            subject = "Special chars: áéíóú ñ ü ç 中文 日本語 한글 🎉 emoji",
            body = """
                Body with special content:
                - Quotes: "double" and 'single'
                - Symbols: @#$%^&*()
                - Unicode: áéíóú ñ ü ç
                - Asian: 中文 日本語 한글
                - Emoji: 🎉 🚀 ✅
                - HTML: <div class="test">content</div>
                - JSON: {"key": "value", "number": 123}
                - Newlines and\ttabs
            """.trimIndent()
        ).getOrThrow()
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedEmail = reloadedWorkspace.projects[0].tools.smtpFake!!.storedInbox[0]
        
        // Verificar que todos los caracteres especiales se preservaron
        assertTrue(reloadedEmail.subject.contains("áéíóú ñ ü ç"))
        assertTrue(reloadedEmail.subject.contains("中文 日本語 한글"))
        assertTrue(reloadedEmail.subject.contains("🎉"))
        
        assertTrue(reloadedEmail.bodyText.contains("\"double\" and 'single'"))
        assertTrue(reloadedEmail.bodyText.contains("@#$%^&*()"))
        assertTrue(reloadedEmail.bodyText.contains("<div class=\"test\">content</div>"))
        assertTrue(reloadedEmail.bodyText.contains("{\"key\": \"value\", \"number\": 123}"))
        assertTrue(reloadedEmail.bodyText.contains("🎉 🚀 ✅"))
    }
    
    @Test
    fun `headers persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_headers.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val smtpUseCases = SmtpFakeUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir email con headers personalizados
        workspace = smtpUseCases.addEmailToInbox(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            from = "sender@example.com",
            to = listOf("recipient@example.com"),
            subject = "Test with custom headers",
            bodyText = "Body content",
            headers = mapOf(
                "X-Custom-Header" to "custom value",
                "X-Priority" to "high",
                "Content-Type" to "text/plain; charset=utf-8",
                "Message-ID" to "<12345@example.com>",
                "X-Special-Chars" to "áéíóú ñ 中文"
            )
        ).getOrThrow()
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedEmail = reloadedWorkspace.projects[0].tools.smtpFake!!.storedInbox[0]
        
        assertEquals(5, reloadedEmail.headers.size)
        assertEquals("custom value", reloadedEmail.headers["X-Custom-Header"])
        assertEquals("high", reloadedEmail.headers["X-Priority"])
        assertEquals("text/plain; charset=utf-8", reloadedEmail.headers["Content-Type"])
        assertEquals("<12345@example.com>", reloadedEmail.headers["Message-ID"])
        assertEquals("áéíóú ñ 中文", reloadedEmail.headers["X-Special-Chars"])
    }
    
    @Test
    fun `multiple recipients persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_multi_recipients.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val smtpUseCases = SmtpFakeUseCases()
        
        var workspace = createTestWorkspace()
        
        // Email con múltiples destinatarios
        workspace = smtpUseCases.addEmailToInbox(
            workspace = workspace,
            projectId = "proj_smtp_portability",
            from = "sender@example.com",
            to = listOf(
                "recipient1@example.com",
                "recipient2@test.org",
                "recipient3@company.com"
            ),
            subject = "Email to multiple recipients",
            bodyText = "This email goes to 3 people"
        ).getOrThrow()
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedEmail = reloadedWorkspace.projects[0].tools.smtpFake!!.storedInbox[0]
        
        assertEquals(3, reloadedEmail.to.size)
        assertEquals("recipient1@example.com", reloadedEmail.to[0])
        assertEquals("recipient2@test.org", reloadedEmail.to[1])
        assertEquals("recipient3@company.com", reloadedEmail.to[2])
    }
}
