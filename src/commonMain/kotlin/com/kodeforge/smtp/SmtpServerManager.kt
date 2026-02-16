package com.kodeforge.smtp

import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.SmtpFakeUseCases

/**
 * Gestor de servidores SMTP por proyecto.
 * 
 * Mantiene un servidor SMTP por proyecto y maneja el ciclo de vida.
 */
class SmtpServerManager {
    
    private val servers = mutableMapOf<String, SmtpServer>()
    private val useCases = SmtpFakeUseCases()
    
    /**
     * Inicia el servidor SMTP para un proyecto.
     * 
     * @param projectId ID del proyecto
     * @param workspace Workspace actual
     * @param host Host donde escuchar
     * @param port Puerto donde escuchar
     * @param allowedRecipients Lista de emails permitidos
     * @param onWorkspaceUpdate Callback para actualizar workspace cuando se recibe un email
     */
    fun startServer(
        projectId: String,
        workspace: Workspace,
        host: String,
        port: Int,
        allowedRecipients: List<String>,
        onWorkspaceUpdate: (Workspace) -> Unit
    ) {
        // Detener servidor existente si hay uno
        stopServer(projectId)
        
        // Crear nuevo servidor
        val server = SmtpServer()
        
        // Iniciar servidor con callback
        server.start(
            host = host,
            port = port,
            allowedRecipients = allowedRecipients,
            onEmailReceived = { emailReceived ->
                // Guardar email en el workspace
                val result = useCases.addEmailToInbox(
                    workspace = workspace,
                    projectId = projectId,
                    from = emailReceived.from,
                    to = emailReceived.to,
                    subject = emailReceived.subject,
                    bodyText = emailReceived.bodyText,
                    headers = emailReceived.headers
                )
                
                result.onSuccess { updatedWorkspace ->
                    onWorkspaceUpdate(updatedWorkspace)
                    println("[SmtpServerManager] Email saved to workspace")
                }.onFailure { error ->
                    println("[SmtpServerManager] Error saving email: ${error.message}")
                }
            }
        )
        
        // Guardar referencia
        servers[projectId] = server
        
        println("[SmtpServerManager] Server started for project $projectId")
    }
    
    /**
     * Detiene el servidor SMTP de un proyecto.
     */
    fun stopServer(projectId: String) {
        servers[projectId]?.let { server ->
            server.stop()
            servers.remove(projectId)
            println("[SmtpServerManager] Server stopped for project $projectId")
        }
    }
    
    /**
     * Detiene todos los servidores.
     */
    fun stopAllServers() {
        servers.keys.toList().forEach { projectId ->
            stopServer(projectId)
        }
    }
    
    /**
     * Verifica si un servidor está corriendo para un proyecto.
     */
    fun isServerRunning(projectId: String): Boolean {
        return servers[projectId]?.isRunning() ?: false
    }
    
    /**
     * Obtiene el servidor de un proyecto (si existe).
     */
    fun getServer(projectId: String): SmtpServer? {
        return servers[projectId]
    }
}

