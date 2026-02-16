package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.*
import com.kodeforge.domain.validation.RestSoapValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para gestión de herramienta REST/SOAP.
 */
class RestSoapUseCases {
    
    // ===== Client History =====
    
    /**
     * Añade una request al historial del cliente.
     */
    fun addRequestToHistory(
        workspace: Workspace,
        projectId: String,
        type: String, // REST, SOAP
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        response: HttpResponse? = null
    ): Result<Workspace> {
        try {
            // Validaciones
            RestSoapValidator.validateType(type).getOrThrow()
            RestSoapValidator.validateMethod(method).getOrThrow()
            RestSoapValidator.validateUrl(url).getOrThrow()
            
            // Buscar proyecto
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap ?: RestSoapTool(enabled = true)
            
            // Crear request
            val newRequest = HttpRequest(
                id = generateId("req"),
                at = generateTimestamp(),
                type = type,
                method = method.uppercase(),
                url = url,
                headers = headers,
                body = body,
                response = response
            )
            
            // Actualizar proyecto
            val updatedRestSoap = currentRestSoap.copy(
                clientHistory = currentRestSoap.clientHistory + newRequest
            )
            
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Limpia el historial del cliente.
     */
    fun clearHistory(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val updatedRestSoap = currentRestSoap.copy(clientHistory = emptyList())
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Obtiene el historial del cliente.
     */
    fun getHistory(
        workspace: Workspace,
        projectId: String
    ): List<HttpRequest> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        return project.tools.restSoap?.clientHistory ?: emptyList()
    }
    
    // ===== Mock Server Config =====
    
    /**
     * Habilita el mock server.
     */
    fun enableMockServer(
        workspace: Workspace,
        projectId: String,
        listenHost: String = "127.0.0.1",
        listenPort: Int = 8089
    ): Result<Workspace> {
        try {
            RestSoapValidator.validatePort(listenPort).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap ?: RestSoapTool(enabled = true)
            val currentMockServer = currentRestSoap.mockServer
            
            val updatedMockServer = if (currentMockServer != null) {
                currentMockServer.copy(
                    enabled = true,
                    listenHost = listenHost,
                    listenPort = listenPort
                )
            } else {
                MockServer(
                    enabled = true,
                    listenHost = listenHost,
                    listenPort = listenPort
                )
            }
            
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Deshabilita el mock server.
     */
    fun disableMockServer(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val updatedMockServer = currentMockServer.copy(enabled = false)
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Cambia el modo del mock server.
     */
    fun setMockServerMode(
        workspace: Workspace,
        projectId: String,
        mode: String // "catchAll" | "defined"
    ): Result<Workspace> {
        try {
            RestSoapValidator.validateMode(mode).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val updatedMockServer = currentMockServer.copy(mode = mode)
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
    
    // ===== Mock Routes =====
    
    /**
     * Añade una ruta al mock server.
     */
    fun addRoute(
        workspace: Workspace,
        projectId: String,
        method: String,
        path: String,
        responseStatus: Int,
        responseBody: String? = null,
        responseHeaders: Map<String, String> = emptyMap()
    ): Result<Workspace> {
        try {
            // Validaciones
            RestSoapValidator.validateMethod(method).getOrThrow()
            RestSoapValidator.validatePath(path).getOrThrow()
            RestSoapValidator.validateStatus(responseStatus).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            // Crear ruta
            val newRoute = MockRoute(
                id = generateId("route"),
                method = method.uppercase(),
                path = path,
                response = HttpResponse(
                    status = responseStatus,
                    body = responseBody,
                    headers = responseHeaders
                )
            )
            
            val updatedMockServer = currentMockServer.copy(
                routes = currentMockServer.routes + newRoute
            )
            
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Actualiza una ruta existente.
     */
    fun updateRoute(
        workspace: Workspace,
        projectId: String,
        routeId: String,
        method: String? = null,
        path: String? = null,
        responseStatus: Int? = null,
        responseBody: String? = null,
        responseHeaders: Map<String, String>? = null
    ): Result<Workspace> {
        try {
            // Validaciones opcionales
            method?.let { RestSoapValidator.validateMethod(it).getOrThrow() }
            path?.let { RestSoapValidator.validatePath(it).getOrThrow() }
            responseStatus?.let { RestSoapValidator.validateStatus(it).getOrThrow() }
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val routeIndex = currentMockServer.routes.indexOfFirst { it.id == routeId }
            if (routeIndex == -1) {
                return Result.failure(Exception("Ruta no encontrada"))
            }
            
            val existingRoute = currentMockServer.routes[routeIndex]
            
            val updatedRoute = existingRoute.copy(
                method = method?.uppercase() ?: existingRoute.method,
                path = path ?: existingRoute.path,
                response = existingRoute.response.copy(
                    status = responseStatus ?: existingRoute.response.status,
                    body = responseBody ?: existingRoute.response.body,
                    headers = responseHeaders ?: existingRoute.response.headers
                )
            )
            
            val updatedRoutes = currentMockServer.routes.toMutableList()
            updatedRoutes[routeIndex] = updatedRoute
            
            val updatedMockServer = currentMockServer.copy(routes = updatedRoutes)
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Elimina una ruta.
     */
    fun deleteRoute(
        workspace: Workspace,
        projectId: String,
        routeId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val updatedRoutes = currentMockServer.routes.filter { it.id != routeId }
            
            val updatedMockServer = currentMockServer.copy(routes = updatedRoutes)
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Obtiene todas las rutas.
     */
    fun getRoutes(
        workspace: Workspace,
        projectId: String
    ): List<MockRoute> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        return project.tools.restSoap?.mockServer?.routes ?: emptyList()
    }
    
    // ===== Captured Requests =====
    
    /**
     * Añade una request capturada.
     */
    fun addCapturedRequest(
        workspace: Workspace,
        projectId: String,
        method: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ): Result<Workspace> {
        try {
            RestSoapValidator.validateMethod(method).getOrThrow()
            RestSoapValidator.validatePath(path).getOrThrow()
            
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val newCaptured = CapturedRequest(
                id = generateId("cap"),
                at = generateTimestamp(),
                method = method.uppercase(),
                path = path,
                headers = headers,
                body = body
            )
            
            val updatedMockServer = currentMockServer.copy(
                capturedRequests = currentMockServer.capturedRequests + newCaptured
            )
            
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Limpia las requests capturadas.
     */
    fun clearCapturedRequests(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace> {
        try {
            val projectIndex = workspace.projects.indexOfFirst { it.id == projectId }
            if (projectIndex == -1) {
                return Result.failure(Exception("Proyecto no encontrado"))
            }
            
            val project = workspace.projects[projectIndex]
            val currentRestSoap = project.tools.restSoap
                ?: return Result.failure(Exception("REST/SOAP tool no habilitado"))
            
            val currentMockServer = currentRestSoap.mockServer
                ?: return Result.failure(Exception("Mock server no configurado"))
            
            val updatedMockServer = currentMockServer.copy(capturedRequests = emptyList())
            val updatedRestSoap = currentRestSoap.copy(mockServer = updatedMockServer)
            val updatedTools = project.tools.copy(restSoap = updatedRestSoap)
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
     * Obtiene las requests capturadas.
     */
    fun getCapturedRequests(
        workspace: Workspace,
        projectId: String
    ): List<CapturedRequest> {
        val project = workspace.projects.find { it.id == projectId } ?: return emptyList()
        return project.tools.restSoap?.mockServer?.capturedRequests ?: emptyList()
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

