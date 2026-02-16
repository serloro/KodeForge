package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.*
import com.kodeforge.domain.validation.SftpValidator
import kotlinx.datetime.Clock

/**
 * Casos de uso para gestionar SftpTool.
 */
class SftpUseCases {
    
    private var idCounter = 0
    
    /**
     * Genera un ID único con prefijo.
     */
    private fun generateId(prefix: String): String {
        idCounter++
        return "${prefix}_${Clock.System.now().toEpochMilliseconds()}_$idCounter"
    }
    
    /**
     * Genera un timestamp ISO 8601.
     */
    private fun generateTimestamp(): String {
        return Clock.System.now().toString()
    }
    
    /**
     * Obtiene el SftpTool de un proyecto.
     */
    fun getSftpTool(workspace: Workspace, projectId: String): SftpTool? {
        val project = workspace.projects.find { it.id == projectId }
        return project?.tools?.sftp
    }
    
    /**
     * Actualiza el SftpTool de un proyecto.
     */
    private fun updateSftpTool(
        workspace: Workspace,
        projectId: String,
        updater: (SftpTool) -> SftpTool
    ): Result<Workspace> {
        return try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(IllegalArgumentException("Project not found: $projectId"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentTool = project.tools.sftp ?: SftpTool()
            val updatedTool = updater(currentTool)
            
            // Validar el tool actualizado
            SftpValidator.validateTool(updatedTool).getOrElse { 
                return Result.failure(it)
            }
            
            val updatedProject = project.copy(
                tools = project.tools.copy(sftp = updatedTool),
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            val updatedWorkspace = workspace.copy(
                projects = updatedProjects,
                app = workspace.app.copy(updatedAt = generateTimestamp())
            )
            
            Result.success(updatedWorkspace)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ===== Configuración =====
    
    /**
     * Habilita el SftpTool para un proyecto.
     */
    fun enableSftpTool(workspace: Workspace, projectId: String): Result<Workspace> {
        return updateSftpTool(workspace, projectId) { currentTool ->
            currentTool.copy(enabled = true)
        }
    }
    
    /**
     * Deshabilita el SftpTool para un proyecto.
     */
    fun disableSftpTool(workspace: Workspace, projectId: String): Result<Workspace> {
        return updateSftpTool(workspace, projectId) { currentTool ->
            currentTool.copy(enabled = false)
        }
    }
    
    // ===== Conexiones CRUD =====
    
    /**
     * Añade una nueva conexión SFTP.
     */
    fun addConnection(
        workspace: Workspace,
        projectId: String,
        name: String,
        host: String,
        port: Int = 22,
        username: String,
        authType: String,
        authValueRef: String
    ): Result<Workspace> {
        val newConnection = SftpConnection(
            id = generateId("sftp"),
            name = name,
            host = host,
            port = port,
            username = username,
            auth = AuthConfig(type = authType, valueRef = authValueRef)
        )
        
        // Validar la nueva conexión
        SftpValidator.validateConnection(newConnection).getOrElse {
            return Result.failure(it)
        }
        
        return updateSftpTool(workspace, projectId) { currentTool ->
            // Verificar que no exista una conexión con el mismo nombre
            if (currentTool.connections.any { it.name == name }) {
                throw IllegalArgumentException("Connection with name '$name' already exists")
            }
            
            currentTool.copy(connections = currentTool.connections + newConnection)
        }
    }
    
    /**
     * Actualiza una conexión SFTP existente.
     */
    fun updateConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String,
        name: String,
        host: String,
        port: Int,
        username: String,
        authType: String,
        authValueRef: String
    ): Result<Workspace> {
        return updateSftpTool(workspace, projectId) { currentTool ->
            val connectionIndex = currentTool.connections.indexOfFirst { it.id == connectionId }
            if (connectionIndex == -1) {
                throw IllegalArgumentException("Connection not found: $connectionId")
            }
            
            // Verificar que no exista otra conexión con el mismo nombre
            val existingWithSameName = currentTool.connections.find { 
                it.name == name && it.id != connectionId 
            }
            if (existingWithSameName != null) {
                throw IllegalArgumentException("Another connection with name '$name' already exists")
            }
            
            val updatedConnection = SftpConnection(
                id = connectionId,
                name = name,
                host = host,
                port = port,
                username = username,
                auth = AuthConfig(type = authType, valueRef = authValueRef)
            )
            
            // Validar la conexión actualizada
            SftpValidator.validateConnection(updatedConnection).getOrThrow()
            
            val updatedConnections = currentTool.connections.toMutableList()
            updatedConnections[connectionIndex] = updatedConnection
            
            currentTool.copy(connections = updatedConnections)
        }
    }
    
    /**
     * Elimina una conexión SFTP.
     */
    fun deleteConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String
    ): Result<Workspace> {
        return updateSftpTool(workspace, projectId) { currentTool ->
            val connectionExists = currentTool.connections.any { it.id == connectionId }
            if (!connectionExists) {
                throw IllegalArgumentException("Connection not found: $connectionId")
            }
            
            currentTool.copy(
                connections = currentTool.connections.filter { it.id != connectionId }
            )
        }
    }
    
    /**
     * Obtiene una conexión por ID.
     */
    fun getConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String
    ): SftpConnection? {
        val tool = getSftpTool(workspace, projectId)
        return tool?.connections?.find { it.id == connectionId }
    }
    
    /**
     * Obtiene todas las conexiones de un proyecto.
     */
    fun getConnections(workspace: Workspace, projectId: String): List<SftpConnection> {
        return getSftpTool(workspace, projectId)?.connections ?: emptyList()
    }
}

