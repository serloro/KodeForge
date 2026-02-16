package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.RestSoapUseCases
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para RestSoapUseCases - Gestión de herramienta REST/SOAP.
 */
class RestSoapUseCasesTest {
    
    private lateinit var restSoapUseCases: RestSoapUseCases
    private lateinit var initialWorkspace: Workspace
    private lateinit var project: Project
    
    @BeforeEach
    fun setup() {
        restSoapUseCases = RestSoapUseCases()
        
        project = Project(
            id = "proj1",
            name = "Test Project",
            description = "Test project for REST/SOAP",
            status = "active",
            members = emptyList(),
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z",
            tools = ProjectTools(
                restSoap = RestSoapTool(enabled = true)
            )
        )
        
        initialWorkspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(project),
            tasks = emptyList()
        )
    }
    
    // ===== Client History =====
    
    @Test
    fun `addRequestToHistory - adds request to client history`() {
        val result = restSoapUseCases.addRequestToHistory(
            workspace = initialWorkspace,
            projectId = project.id,
            type = "REST",
            method = "GET",
            url = "https://api.test.com/health",
            headers = mapOf("accept" to "application/json"),
            response = HttpResponse(status = 200, body = "{\"ok\":true}")
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrNull()!!
        
        val history = restSoapUseCases.getHistory(updatedWorkspace, project.id)
        assertEquals(1, history.size)
        
        val request = history[0]
        assertEquals("REST", request.type)
        assertEquals("GET", request.method)
        assertEquals("https://api.test.com/health", request.url)
        assertNotNull(request.response)
        assertEquals(200, request.response?.status)
    }
    
    @Test
    fun `addRequestToHistory - validates method`() {
        val result = restSoapUseCases.addRequestToHistory(
            workspace = initialWorkspace,
            projectId = project.id,
            type = "REST",
            method = "INVALID",
            url = "https://api.test.com/health"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Método HTTP inválido") == true)
    }
    
    @Test
    fun `clearHistory - removes all requests`() {
        // Añadir 2 requests
        var workspace = initialWorkspace
        
        val result1 = restSoapUseCases.addRequestToHistory(
            workspace = workspace,
            projectId = project.id,
            type = "REST",
            method = "GET",
            url = "https://api.test.com/users"
        )
        workspace = result1.getOrNull()!!
        
        val result2 = restSoapUseCases.addRequestToHistory(
            workspace = workspace,
            projectId = project.id,
            type = "REST",
            method = "POST",
            url = "https://api.test.com/users"
        )
        workspace = result2.getOrNull()!!
        
        assertEquals(2, restSoapUseCases.getHistory(workspace, project.id).size)
        
        // Limpiar historial
        val clearResult = restSoapUseCases.clearHistory(workspace, project.id)
        assertTrue(clearResult.isSuccess)
        
        val clearedWorkspace = clearResult.getOrNull()!!
        assertEquals(0, restSoapUseCases.getHistory(clearedWorkspace, project.id).size)
    }
    
    // ===== Mock Server Config =====
    
    @Test
    fun `enableMockServer - enables mock server with config`() {
        val result = restSoapUseCases.enableMockServer(
            workspace = initialWorkspace,
            projectId = project.id,
            listenHost = "0.0.0.0",
            listenPort = 9000
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrNull()!!
        
        val mockServer = updatedWorkspace.projects[0].tools.restSoap?.mockServer
        assertNotNull(mockServer)
        assertTrue(mockServer.enabled)
        assertEquals("0.0.0.0", mockServer.listenHost)
        assertEquals(9000, mockServer.listenPort)
    }
    
    @Test
    fun `disableMockServer - disables mock server`() {
        // Primero habilitar
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        assertTrue(workspace.projects[0].tools.restSoap?.mockServer?.enabled == true)
        
        // Deshabilitar
        val disableResult = restSoapUseCases.disableMockServer(workspace, project.id)
        assertTrue(disableResult.isSuccess)
        
        val updatedWorkspace = disableResult.getOrNull()!!
        assertTrue(updatedWorkspace.projects[0].tools.restSoap?.mockServer?.enabled == false)
    }
    
    @Test
    fun `setMockServerMode - changes mode`() {
        // Habilitar mock server primero
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        // Cambiar modo
        val result = restSoapUseCases.setMockServerMode(workspace, project.id, "defined")
        assertTrue(result.isSuccess)
        
        val updatedWorkspace = result.getOrNull()!!
        assertEquals("defined", updatedWorkspace.projects[0].tools.restSoap?.mockServer?.mode)
    }
    
    @Test
    fun `setMockServerMode - validates mode`() {
        // Habilitar mock server primero
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        // Modo inválido
        val result = restSoapUseCases.setMockServerMode(workspace, project.id, "invalid")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Modo debe ser") == true)
    }
    
    // ===== Mock Routes =====
    
    @Test
    fun `addRoute - adds route to mock server`() {
        // Habilitar mock server primero
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        // Añadir ruta
        val result = restSoapUseCases.addRoute(
            workspace = workspace,
            projectId = project.id,
            method = "POST",
            path = "/v1/login",
            responseStatus = 200,
            responseBody = "{\"token\":\"fake-token\"}",
            responseHeaders = mapOf("content-type" to "application/json")
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrNull()!!
        
        val routes = restSoapUseCases.getRoutes(updatedWorkspace, project.id)
        assertEquals(1, routes.size)
        
        val route = routes[0]
        assertEquals("POST", route.method)
        assertEquals("/v1/login", route.path)
        assertEquals(200, route.response.status)
        assertEquals("{\"token\":\"fake-token\"}", route.response.body)
    }
    
    @Test
    fun `updateRoute - updates existing route`() {
        // Habilitar mock server y añadir ruta
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        val addResult = restSoapUseCases.addRoute(
            workspace = workspace,
            projectId = project.id,
            method = "GET",
            path = "/api/users",
            responseStatus = 200
        )
        workspace = addResult.getOrNull()!!
        
        val routeId = restSoapUseCases.getRoutes(workspace, project.id)[0].id
        
        // Actualizar ruta
        val updateResult = restSoapUseCases.updateRoute(
            workspace = workspace,
            projectId = project.id,
            routeId = routeId,
            responseStatus = 404,
            responseBody = "{\"error\":\"not found\"}"
        )
        
        assertTrue(updateResult.isSuccess)
        val updatedWorkspace = updateResult.getOrNull()!!
        
        val updatedRoute = restSoapUseCases.getRoutes(updatedWorkspace, project.id)[0]
        assertEquals(404, updatedRoute.response.status)
        assertEquals("{\"error\":\"not found\"}", updatedRoute.response.body)
        assertEquals("GET", updatedRoute.method) // No cambió
        assertEquals("/api/users", updatedRoute.path) // No cambió
    }
    
    @Test
    fun `deleteRoute - removes route`() {
        // Habilitar mock server y añadir 2 rutas
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        val add1 = restSoapUseCases.addRoute(workspace, project.id, "GET", "/api/users", 200)
        workspace = add1.getOrNull()!!
        
        val add2 = restSoapUseCases.addRoute(workspace, project.id, "POST", "/api/users", 201)
        workspace = add2.getOrNull()!!
        
        assertEquals(2, restSoapUseCases.getRoutes(workspace, project.id).size)
        
        // Eliminar primera ruta
        val routeId = restSoapUseCases.getRoutes(workspace, project.id)[0].id
        val deleteResult = restSoapUseCases.deleteRoute(workspace, project.id, routeId)
        
        assertTrue(deleteResult.isSuccess)
        val updatedWorkspace = deleteResult.getOrNull()!!
        
        val routes = restSoapUseCases.getRoutes(updatedWorkspace, project.id)
        assertEquals(1, routes.size)
        assertEquals("POST", routes[0].method)
    }
    
    // ===== Captured Requests =====
    
    @Test
    fun `addCapturedRequest - adds captured request`() {
        // Habilitar mock server primero
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        // Añadir request capturada
        val result = restSoapUseCases.addCapturedRequest(
            workspace = workspace,
            projectId = project.id,
            method = "POST",
            path = "/anything",
            headers = mapOf("content-type" to "application/json"),
            body = "{\"hello\":\"world\"}"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrNull()!!
        
        val captured = restSoapUseCases.getCapturedRequests(updatedWorkspace, project.id)
        assertEquals(1, captured.size)
        
        val request = captured[0]
        assertEquals("POST", request.method)
        assertEquals("/anything", request.path)
        assertEquals("{\"hello\":\"world\"}", request.body)
    }
    
    @Test
    fun `clearCapturedRequests - removes all captured requests`() {
        // Habilitar mock server y añadir 2 captured requests
        var workspace = initialWorkspace
        val enableResult = restSoapUseCases.enableMockServer(workspace, project.id)
        workspace = enableResult.getOrNull()!!
        
        val add1 = restSoapUseCases.addCapturedRequest(workspace, project.id, "GET", "/test1")
        workspace = add1.getOrNull()!!
        
        val add2 = restSoapUseCases.addCapturedRequest(workspace, project.id, "POST", "/test2")
        workspace = add2.getOrNull()!!
        
        assertEquals(2, restSoapUseCases.getCapturedRequests(workspace, project.id).size)
        
        // Limpiar
        val clearResult = restSoapUseCases.clearCapturedRequests(workspace, project.id)
        assertTrue(clearResult.isSuccess)
        
        val clearedWorkspace = clearResult.getOrNull()!!
        assertEquals(0, restSoapUseCases.getCapturedRequests(clearedWorkspace, project.id).size)
    }
}

