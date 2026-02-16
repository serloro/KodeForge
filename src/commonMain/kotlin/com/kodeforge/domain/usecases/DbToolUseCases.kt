package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.*
import com.kodeforge.domain.validation.DbToolValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Casos de uso para gestionar DbTool (conexiones y queries).
 */
class DbToolUseCases {
    
    private val validator = DbToolValidator()
    
    // ===== Configuración General =====
    
    /**
     * Habilita el tool de base de datos para un proyecto.
     */
    fun enableDbTool(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        return updateDbTool(workspace, projectId) { currentTool ->
            currentTool.copy(enabled = true)
        }
    }
    
    /**
     * Deshabilita el tool de base de datos para un proyecto.
     */
    fun disableDbTool(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        return updateDbTool(workspace, projectId) { currentTool ->
            currentTool.copy(enabled = false)
        }
    }
    
    /**
     * Obtiene la configuración de DbTool de un proyecto.
     */
    fun getDbTool(workspace: Workspace, projectId: String): DbTool? {
        return workspace.projects.find { it.id == projectId }?.tools?.dbTools
    }
    
    // ===== Gestión de Conexiones =====
    
    /**
     * Añade una nueva conexión de base de datos.
     */
    fun addConnection(
        workspace: Workspace,
        projectId: String,
        name: String,
        type: String,
        host: String,
        port: Int,
        database: String,
        username: String,
        authType: String,
        authValueRef: String
    ): Result<Workspace> {
        return try {
            val connection = DbConnection(
                id = generateId("dbconn"),
                name = name,
                type = type,
                host = host,
                port = port,
                database = database,
                username = username,
                auth = AuthConfig(type = authType, valueRef = authValueRef)
            )
            
            validator.validateDbConnection(connection).getOrThrow()
            
            updateDbTool(workspace, projectId) { currentTool ->
                // Validar que no exista otra conexión con el mismo nombre
                if (currentTool.connections.any { it.name == name }) {
                    throw IllegalArgumentException("Ya existe una conexión con el nombre '$name'")
                }
                currentTool.copy(connections = currentTool.connections + connection)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza una conexión existente.
     */
    fun updateConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String,
        name: String? = null,
        type: String? = null,
        host: String? = null,
        port: Int? = null,
        database: String? = null,
        username: String? = null,
        authType: String? = null,
        authValueRef: String? = null
    ): Result<Workspace> {
        return try {
            updateDbTool(workspace, projectId) { currentTool ->
                val connectionIndex = currentTool.connections.indexOfFirst { it.id == connectionId }
                if (connectionIndex == -1) {
                    throw IllegalArgumentException("Conexión no encontrada: $connectionId")
                }
                
                val oldConnection = currentTool.connections[connectionIndex]
                val updatedConnection = oldConnection.copy(
                    name = name ?: oldConnection.name,
                    type = type ?: oldConnection.type,
                    host = host ?: oldConnection.host,
                    port = port ?: oldConnection.port,
                    database = database ?: oldConnection.database,
                    username = username ?: oldConnection.username,
                    auth = if (authType != null || authValueRef != null) {
                        AuthConfig(
                            type = authType ?: oldConnection.auth.type,
                            valueRef = authValueRef ?: oldConnection.auth.valueRef
                        )
                    } else {
                        oldConnection.auth
                    }
                )
                
                validator.validateDbConnection(updatedConnection).getOrThrow()
                
                val updatedConnections = currentTool.connections.toMutableList()
                updatedConnections[connectionIndex] = updatedConnection
                
                currentTool.copy(connections = updatedConnections)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina una conexión.
     */
    fun deleteConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String
    ): Result<Workspace> {
        return try {
            updateDbTool(workspace, projectId) { currentTool ->
                // Verificar que no haya queries que usen esta conexión
                val queriesUsingConnection = currentTool.savedQueries.filter { 
                    it.connectionId == connectionId 
                }
                
                if (queriesUsingConnection.isNotEmpty()) {
                    throw IllegalArgumentException(
                        "No se puede eliminar la conexión porque ${queriesUsingConnection.size} " +
                        "query(s) la están usando. Elimina las queries primero."
                    )
                }
                
                currentTool.copy(
                    connections = currentTool.connections.filter { it.id != connectionId }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todas las conexiones de un proyecto.
     */
    fun getConnections(workspace: Workspace, projectId: String): List<DbConnection> {
        return getDbTool(workspace, projectId)?.connections ?: emptyList()
    }
    
    /**
     * Obtiene una conexión por ID.
     */
    fun getConnectionById(
        workspace: Workspace,
        projectId: String,
        connectionId: String
    ): DbConnection? {
        return getConnections(workspace, projectId).find { it.id == connectionId }
    }
    
    // ===== Gestión de Queries =====
    
    /**
     * Añade una nueva query guardada.
     */
    fun addSavedQuery(
        workspace: Workspace,
        projectId: String,
        name: String,
        connectionId: String,
        sql: String
    ): Result<Workspace> {
        return try {
            val query = SavedQuery(
                id = generateId("query"),
                name = name,
                connectionId = connectionId,
                sql = sql
            )
            
            validator.validateSavedQuery(query).getOrThrow()
            
            updateDbTool(workspace, projectId) { currentTool ->
                // Validar que la conexión existe
                if (!currentTool.connections.any { it.id == connectionId }) {
                    throw IllegalArgumentException("Conexión no encontrada: $connectionId")
                }
                
                // Validar que no exista otra query con el mismo nombre
                if (currentTool.savedQueries.any { it.name == name }) {
                    throw IllegalArgumentException("Ya existe una query con el nombre '$name'")
                }
                
                currentTool.copy(savedQueries = currentTool.savedQueries + query)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza una query guardada.
     */
    fun updateSavedQuery(
        workspace: Workspace,
        projectId: String,
        queryId: String,
        name: String? = null,
        connectionId: String? = null,
        sql: String? = null
    ): Result<Workspace> {
        return try {
            updateDbTool(workspace, projectId) { currentTool ->
                val queryIndex = currentTool.savedQueries.indexOfFirst { it.id == queryId }
                if (queryIndex == -1) {
                    throw IllegalArgumentException("Query no encontrada: $queryId")
                }
                
                val oldQuery = currentTool.savedQueries[queryIndex]
                val updatedQuery = oldQuery.copy(
                    name = name ?: oldQuery.name,
                    connectionId = connectionId ?: oldQuery.connectionId,
                    sql = sql ?: oldQuery.sql
                )
                
                validator.validateSavedQuery(updatedQuery).getOrThrow()
                
                // Validar que la conexión existe
                if (!currentTool.connections.any { it.id == updatedQuery.connectionId }) {
                    throw IllegalArgumentException("Conexión no encontrada: ${updatedQuery.connectionId}")
                }
                
                val updatedQueries = currentTool.savedQueries.toMutableList()
                updatedQueries[queryIndex] = updatedQuery
                
                currentTool.copy(savedQueries = updatedQueries)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina una query guardada.
     */
    fun deleteSavedQuery(
        workspace: Workspace,
        projectId: String,
        queryId: String
    ): Result<Workspace> {
        return updateDbTool(workspace, projectId) { currentTool ->
            currentTool.copy(
                savedQueries = currentTool.savedQueries.filter { it.id != queryId }
            )
        }
    }
    
    /**
     * Obtiene todas las queries guardadas de un proyecto.
     */
    fun getSavedQueries(workspace: Workspace, projectId: String): List<SavedQuery> {
        return getDbTool(workspace, projectId)?.savedQueries ?: emptyList()
    }
    
    /**
     * Obtiene una query por ID.
     */
    fun getSavedQueryById(
        workspace: Workspace,
        projectId: String,
        queryId: String
    ): SavedQuery? {
        return getSavedQueries(workspace, projectId).find { it.id == queryId }
    }
    
    /**
     * Obtiene todas las queries de una conexión específica.
     */
    fun getQueriesByConnection(
        workspace: Workspace,
        projectId: String,
        connectionId: String
    ): List<SavedQuery> {
        return getSavedQueries(workspace, projectId).filter { it.connectionId == connectionId }
    }
    
    // ===== Helpers Privados =====
    
    /**
     * Actualiza el DbTool de un proyecto aplicando una transformación.
     */
    private fun updateDbTool(
        workspace: Workspace,
        projectId: String,
        update: (DbTool) -> DbTool
    ): Result<Workspace> {
        return try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado: $projectId"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentDbTool = project.tools.dbTools ?: DbTool()
            
            val updatedDbTool = update(currentDbTool)
            validator.validateDbTool(updatedDbTool).getOrThrow()
            
            val updatedTools = project.tools.copy(dbTools = updatedDbTool)
            val updatedProject = project.copy(
                tools = updatedTools,
                updatedAt = generateTimestamp()
            )
            
            val updatedProjects = workspace.projects.toMutableList()
            updatedProjects[projectIndex] = updatedProject
            
            Result.success(workspace.copy(projects = updatedProjects))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Genera un ID único.
     */
    private fun generateId(prefix: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        return "${prefix}_${timestamp}_${random}"
    }
    
    /**
     * Genera un timestamp ISO 8601.
     */
    private fun generateTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        return "${localDateTime.date}T${localDateTime.time}Z"
    }
    
    // ===== Historial de Ejecuciones =====
    
    /**
     * Añade una ejecución al historial.
     */
    fun addExecutionToHistory(
        workspace: Workspace,
        projectId: String,
        connectionId: String,
        sql: String,
        success: Boolean,
        rowCount: Int = 0,
        executionTimeMs: Long = 0,
        error: String? = null
    ): Result<Workspace> {
        return try {
            val execution = QueryExecution(
                id = generateId("exec"),
                executedAt = generateTimestamp(),
                connectionId = connectionId,
                sql = sql,
                success = success,
                rowCount = rowCount,
                executionTimeMs = executionTimeMs,
                error = error
            )
            
            updateDbTool(workspace, projectId) { currentTool ->
                // Mantener solo las últimas 50 ejecuciones
                val newHistory = (currentTool.executionHistory + execution).takeLast(50)
                currentTool.copy(executionHistory = newHistory)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el historial de ejecuciones.
     */
    fun getExecutionHistory(workspace: Workspace, projectId: String): List<QueryExecution> {
        return getDbTool(workspace, projectId)?.executionHistory ?: emptyList()
    }
    
    /**
     * Limpia el historial de ejecuciones.
     */
    fun clearExecutionHistory(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        return updateDbTool(workspace, projectId) { currentTool ->
            currentTool.copy(executionHistory = emptyList())
        }
    }
}

