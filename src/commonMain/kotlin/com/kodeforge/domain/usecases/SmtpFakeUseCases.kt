package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.*
import com.kodeforge.domain.validation.SmtpFakeValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para gestión de herramienta SMTP Fake.
 */
class SmtpFakeUseCases {
    
    // ===== Configuration =====
    
    /**
     * Habilita el servidor SMTP Fake.
     */
    fun enableSmtpServer(
        workspace: Workspace,
        projectId: String,
        listenHost: String = "127.0.0.1",
        listenPort: Int = 2525
    ): Result<Workspace> {
        try {
            SmtpFakeValidator.validateHost(listenHost).getOrThrow()
            SmtpFakeValidator.validatePort(listenPort).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake ?: SmtpFakeTool()
            
            val updatedSmtp = currentSmtp.copy(
                enabled = true,
                listenHost = listenHost,
                listenPort = listenPort
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Deshabilita el servidor SMTP Fake.
     */
    fun disableSmtpServer(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            val updatedSmtp = currentSmtp.copy(enabled = false)
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Actualiza la configuración del servidor SMTP.
     */
    fun updateSmtpConfig(
        workspace: Workspace,
        projectId: String,
        listenHost: String? = null,
        listenPort: Int? = null
    ): Result<Workspace> {
        try {
            listenHost?.let { SmtpFakeValidator.validateHost(it).getOrThrow() }
            listenPort?.let { SmtpFakeValidator.validatePort(it).getOrThrow() }
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            val updatedSmtp = currentSmtp.copy(
                listenHost = listenHost ?: currentSmtp.listenHost,
                listenPort = listenPort ?: currentSmtp.listenPort
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    // ===== Allowed Recipients =====
    
    /**
     * Añade un email a la lista de destinatarios permitidos.
     */
    fun addAllowedRecipient(
        workspace: Workspace,
        projectId: String,
        email: String
    ): Result<Workspace> {
        try {
            SmtpFakeValidator.validateEmail(email).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            // Verificar si ya existe
            if (currentSmtp.allowedRecipients.contains(email)) {
                return Result.failure(Exception("Email ya existe en la lista de permitidos"))
            }
            
            val updatedSmtp = currentSmtp.copy(
                allowedRecipients = currentSmtp.allowedRecipients + email
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Elimina un email de la lista de destinatarios permitidos.
     */
    fun removeAllowedRecipient(
        workspace: Workspace,
        projectId: String,
        email: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            val updatedSmtp = currentSmtp.copy(
                allowedRecipients = currentSmtp.allowedRecipients.filter { it != email }
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Obtiene la lista de destinatarios permitidos.
     */
    fun getAllowedRecipients(
        workspace: Workspace,
        projectId: String
    ): List<String> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        return project.tools.smtpFake?.allowedRecipients ?: emptyList()
    }
    
    // ===== Stored Inbox =====
    
    /**
     * Añade un email al inbox almacenado.
     */
    fun addEmailToInbox(
        workspace: Workspace,
        projectId: String,
        from: String,
        to: List<String>,
        subject: String,
        bodyText: String,
        headers: Map<String, String> = emptyMap()
    ): Result<Workspace> {
        try {
            // Validaciones
            SmtpFakeValidator.validateEmail(from).getOrThrow()
            SmtpFakeValidator.validateEmails(to).getOrThrow()
            SmtpFakeValidator.validateSubject(subject).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            // Crear email
            val newEmail = EmailMessage(
                id = generateId("mail"),
                receivedAt = generateTimestamp(),
                from = from,
                to = to,
                subject = subject,
                bodyText = bodyText,
                headers = headers
            )
            
            val updatedSmtp = currentSmtp.copy(
                storedInbox = currentSmtp.storedInbox + newEmail
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Elimina un email del inbox.
     */
    fun deleteEmailFromInbox(
        workspace: Workspace,
        projectId: String,
        emailId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            val updatedSmtp = currentSmtp.copy(
                storedInbox = currentSmtp.storedInbox.filter { it.id != emailId }
            )
            
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Limpia todo el inbox.
     */
    fun clearInbox(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentSmtp = project.tools.smtpFake
                ?: return Result.failure(Exception("SMTP Fake tool no habilitado"))
            
            val updatedSmtp = currentSmtp.copy(storedInbox = emptyList())
            val updatedTools = project.tools.copy(smtpFake = updatedSmtp)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            return Result.success(workspace.copy(projects = updatedProjects))
            
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los emails del inbox.
     */
    fun getInbox(
        workspace: Workspace,
        projectId: String
    ): List<EmailMessage> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        return project.tools.smtpFake?.storedInbox ?: emptyList()
    }
    
    /**
     * Obtiene un email específico del inbox.
     */
    fun getEmailById(
        workspace: Workspace,
        projectId: String,
        emailId: String
    ): EmailMessage? {
        val project = workspace.projects.find { it.id == projectId } ?: return null
        return project.tools.smtpFake?.storedInbox?.find { it.id == emailId }
    }
    
    // ===== Helpers =====
    
    private fun generateId(prefix: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        return "${prefix}_${timestamp}_${random}"
    }
    
    private fun generateTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        return "${localDateTime.date}T${localDateTime.time}Z"
    }
}

