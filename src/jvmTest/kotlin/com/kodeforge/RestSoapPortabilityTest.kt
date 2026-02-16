package com.kodeforge

import com.kodeforge.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests de portabilidad para herramienta REST/SOAP.
 * 
 * Valida que al copiar el workspace JSON:
 * - La configuración REST/SOAP se recupera exactamente igual
 * - El historial del cliente se preserva
 * - Las rutas del mock server se preservan
 * - Las requests capturadas se preservan
 * 
 * Flujo: load → save → reload → assert
 */
class RestSoapPortabilityTest {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
    }
    
    private fun createWorkspaceWithRestSoap(): Workspace {
        val project = Project(
            id = "proj_test",
            name = "Test Project",
            description = "Project for REST/SOAP portability testing",
            status = "active",
            members = emptyList(),
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z",
            tools = ProjectTools(
                restSoap = RestSoapTool(
                    enabled = true,
                    clientHistory = listOf(
                        HttpRequest(
                            id = "req_001",
                            at = "2026-02-15T12:10:00Z",
                            type = "REST",
                            method = "GET",
                            url = "https://api.local.test/health",
                            headers = mapOf("accept" to "application/json"),
                            body = null,
                            response = HttpResponse(
                                status = 200,
                                body = "{\"ok\":true}",
                                headers = mapOf("content-type" to "application/json")
                            )
                        ),
                        HttpRequest(
                            id = "req_002",
                            at = "2026-02-15T12:15:00Z",
                            type = "SOAP",
                            method = "POST",
                            url = "https://soap.test.com/service",
                            headers = mapOf("content-type" to "text/xml"),
                            body = "<soap:Envelope>...</soap:Envelope>",
                            response = HttpResponse(
                                status = 200,
                                body = "<soap:Response>...</soap:Response>"
                            )
                        )
                    ),
                    mockServer = MockServer(
                        enabled = true,
                        listenHost = "127.0.0.1",
                        listenPort = 8089,
                        mode = "catchAll",
                        routes = listOf(
                            MockRoute(
                                id = "route_001",
                                method = "POST",
                                path = "/v1/login",
                                response = HttpResponse(
                                    status = 200,
                                    headers = mapOf("content-type" to "application/json"),
                                    body = "{\"token\":\"fake-token\"}"
                                )
                            ),
                            MockRoute(
                                id = "route_002",
                                method = "GET",
                                path = "/v1/users",
                                response = HttpResponse(
                                    status = 200,
                                    headers = mapOf("content-type" to "application/json"),
                                    body = "[{\"id\":1,\"name\":\"John\"}]"
                                )
                            )
                        ),
                        capturedRequests = listOf(
                            CapturedRequest(
                                id = "cap_001",
                                at = "2026-02-15T13:00:00Z",
                                method = "POST",
                                path = "/anything",
                                headers = mapOf("content-type" to "application/json"),
                                body = "{\"hello\":\"world\"}"
                            ),
                            CapturedRequest(
                                id = "cap_002",
                                at = "2026-02-15T13:05:00Z",
                                method = "GET",
                                path = "/test",
                                headers = emptyMap(),
                                body = null
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
    fun `portable persistence - rest soap config survives save and reload`() {
        // 1. LOAD: Crear workspace
        val workspace1 = createWorkspaceWithRestSoap()
        val restSoap1 = workspace1.projects[0].tools.restSoap!!
        
        assertNotNull(restSoap1)
        assertEquals(true, restSoap1.enabled)
        assertEquals(2, restSoap1.clientHistory.size)
        assertNotNull(restSoap1.mockServer)
        
        // 2. SAVE: Serializar
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD: Deserializar
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val restSoap2 = workspace2.projects[0].tools.restSoap!!
        
        // 4. ASSERT: Configuración idéntica
        assertEquals(restSoap1.enabled, restSoap2.enabled)
        assertEquals(restSoap1.clientHistory.size, restSoap2.clientHistory.size)
        assertNotNull(restSoap2.mockServer)
        assertEquals(restSoap1.mockServer?.enabled, restSoap2.mockServer?.enabled)
        assertEquals(restSoap1.mockServer?.listenHost, restSoap2.mockServer?.listenHost)
        assertEquals(restSoap1.mockServer?.listenPort, restSoap2.mockServer?.listenPort)
        assertEquals(restSoap1.mockServer?.mode, restSoap2.mockServer?.mode)
    }
    
    @Test
    fun `portable persistence - client history preserved`() {
        // 1. LOAD
        val workspace1 = createWorkspaceWithRestSoap()
        val history1 = workspace1.projects[0].tools.restSoap?.clientHistory!!
        
        assertEquals(2, history1.size)
        
        // 2. SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val history2 = workspace2.projects[0].tools.restSoap?.clientHistory!!
        
        // 4. ASSERT: Historial idéntico
        assertEquals(history1.size, history2.size)
        
        for (i in history1.indices) {
            val req1 = history1[i]
            val req2 = history2[i]
            
            assertEquals(req1.id, req2.id)
            assertEquals(req1.at, req2.at)
            assertEquals(req1.type, req2.type)
            assertEquals(req1.method, req2.method)
            assertEquals(req1.url, req2.url)
            assertEquals(req1.headers, req2.headers)
            assertEquals(req1.body, req2.body)
            assertEquals(req1.response?.status, req2.response?.status)
            assertEquals(req1.response?.body, req2.response?.body)
        }
        
        // Verificar contenido específico
        assertEquals("REST", history2[0].type)
        assertEquals("GET", history2[0].method)
        assertEquals("https://api.local.test/health", history2[0].url)
        assertEquals(200, history2[0].response?.status)
        
        assertEquals("SOAP", history2[1].type)
        assertEquals("POST", history2[1].method)
        assertEquals("<soap:Envelope>...</soap:Envelope>", history2[1].body)
    }
    
    @Test
    fun `portable persistence - mock routes preserved`() {
        // 1. LOAD
        val workspace1 = createWorkspaceWithRestSoap()
        val routes1 = workspace1.projects[0].tools.restSoap?.mockServer?.routes!!
        
        assertEquals(2, routes1.size)
        
        // 2. SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val routes2 = workspace2.projects[0].tools.restSoap?.mockServer?.routes!!
        
        // 4. ASSERT: Rutas idénticas
        assertEquals(routes1.size, routes2.size)
        
        for (i in routes1.indices) {
            val route1 = routes1[i]
            val route2 = routes2[i]
            
            assertEquals(route1.id, route2.id)
            assertEquals(route1.method, route2.method)
            assertEquals(route1.path, route2.path)
            assertEquals(route1.response.status, route2.response.status)
            assertEquals(route1.response.body, route2.response.body)
            assertEquals(route1.response.headers, route2.response.headers)
        }
        
        // Verificar contenido específico
        assertEquals("POST", routes2[0].method)
        assertEquals("/v1/login", routes2[0].path)
        assertEquals(200, routes2[0].response.status)
        assertEquals("{\"token\":\"fake-token\"}", routes2[0].response.body)
        
        assertEquals("GET", routes2[1].method)
        assertEquals("/v1/users", routes2[1].path)
        assertEquals("[{\"id\":1,\"name\":\"John\"}]", routes2[1].response.body)
    }
    
    @Test
    fun `portable persistence - captured requests preserved`() {
        // 1. LOAD
        val workspace1 = createWorkspaceWithRestSoap()
        val captured1 = workspace1.projects[0].tools.restSoap?.mockServer?.capturedRequests!!
        
        assertEquals(2, captured1.size)
        
        // 2. SAVE
        val jsonString = json.encodeToString(workspace1)
        
        // 3. RELOAD
        val workspace2 = json.decodeFromString<Workspace>(jsonString)
        val captured2 = workspace2.projects[0].tools.restSoap?.mockServer?.capturedRequests!!
        
        // 4. ASSERT: Requests capturadas idénticas
        assertEquals(captured1.size, captured2.size)
        
        for (i in captured1.indices) {
            val cap1 = captured1[i]
            val cap2 = captured2[i]
            
            assertEquals(cap1.id, cap2.id)
            assertEquals(cap1.at, cap2.at)
            assertEquals(cap1.method, cap2.method)
            assertEquals(cap1.path, cap2.path)
            assertEquals(cap1.headers, cap2.headers)
            assertEquals(cap1.body, cap2.body)
        }
        
        // Verificar contenido específico
        assertEquals("POST", captured2[0].method)
        assertEquals("/anything", captured2[0].path)
        assertEquals("{\"hello\":\"world\"}", captured2[0].body)
        
        assertEquals("GET", captured2[1].method)
        assertEquals("/test", captured2[1].path)
        assertEquals(null, captured2[1].body)
    }
    
    @Test
    fun `portable persistence - complete workflow with use cases`() {
        // Flujo completo: load -> add route -> add capture -> add history -> save -> reload -> assert
        
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
        
        val useCases = com.kodeforge.domain.usecases.RestSoapUseCases()
        
        // 2. ADD: Añadir request al historial
        val workspace2 = useCases.addRequestToHistory(
            workspace = workspace1,
            projectId = "proj_workflow",
            type = "REST",
            method = "POST",
            url = "https://api.example.com/users",
            headers = mapOf("Authorization" to "Bearer token123", "Content-Type" to "application/json"),
            body = "{\"name\":\"Alice\",\"email\":\"alice@example.com\"}",
            response = HttpResponse(
                status = 201,
                body = "{\"id\":42,\"name\":\"Alice\"}",
                headers = mapOf("Content-Type" to "application/json")
            )
        ).getOrThrow()
        
        // 3. ENABLE: Habilitar mock server
        val workspace3 = useCases.enableMockServer(
            workspace = workspace2,
            projectId = "proj_workflow",
            listenHost = "0.0.0.0",
            listenPort = 9090
        ).getOrThrow()
        
        // 4. ADD ROUTE: Añadir ruta al mock server
        val workspace4 = useCases.addRoute(
            workspace = workspace3,
            projectId = "proj_workflow",
            method = "DELETE",
            path = "/v2/items/{id}",
            responseStatus = 204,
            responseBody = null,
            responseHeaders = emptyMap()
        ).getOrThrow()
        
        // 5. ADD CAPTURE: Añadir request capturada
        val workspace5 = useCases.addCapturedRequest(
            workspace = workspace4,
            projectId = "proj_workflow",
            method = "PATCH",
            path = "/v2/items/123",
            headers = mapOf("X-Custom-Header" to "custom-value"),
            body = "{\"status\":\"updated\"}"
        ).getOrThrow()
        
        // 6. SET MODE: Cambiar modo a defined
        val workspace6 = useCases.setMockServerMode(
            workspace = workspace5,
            projectId = "proj_workflow",
            mode = "defined"
        ).getOrThrow()
        
        // 7. SAVE: Serializar a JSON
        val jsonString = json.encodeToString(workspace6)
        
        // 8. RELOAD: Deserializar desde JSON
        val workspaceReloaded = json.decodeFromString<Workspace>(jsonString)
        
        // 9. ASSERT: Verificar que TODO se preservó exactamente
        val project = workspaceReloaded.projects.find { it.id == "proj_workflow" }
        assertNotNull(project)
        
        val restSoap = project.tools.restSoap
        assertNotNull(restSoap)
        assertEquals(true, restSoap.enabled)
        
        // Verificar historial
        assertEquals(1, restSoap.clientHistory.size)
        val historyItem = restSoap.clientHistory[0]
        assertEquals("REST", historyItem.type)
        assertEquals("POST", historyItem.method)
        assertEquals("https://api.example.com/users", historyItem.url)
        assertEquals("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}", historyItem.body)
        assertEquals(201, historyItem.response?.status)
        assertEquals("{\"id\":42,\"name\":\"Alice\"}", historyItem.response?.body)
        assertEquals("Bearer token123", historyItem.headers["Authorization"])
        
        // Verificar mock server config
        val mockServer = restSoap.mockServer
        assertNotNull(mockServer)
        assertEquals(true, mockServer.enabled)
        assertEquals("0.0.0.0", mockServer.listenHost)
        assertEquals(9090, mockServer.listenPort)
        assertEquals("defined", mockServer.mode)
        
        // Verificar rutas
        assertEquals(1, mockServer.routes.size)
        val route = mockServer.routes[0]
        assertEquals("DELETE", route.method)
        assertEquals("/v2/items/{id}", route.path)
        assertEquals(204, route.response.status)
        assertEquals(null, route.response.body)
        
        // Verificar capturas
        assertEquals(1, mockServer.capturedRequests.size)
        val capture = mockServer.capturedRequests[0]
        assertEquals("PATCH", capture.method)
        assertEquals("/v2/items/123", capture.path)
        assertEquals("{\"status\":\"updated\"}", capture.body)
        assertEquals("custom-value", capture.headers["X-Custom-Header"])
    }
    
    @Test
    fun `portable persistence - complex headers and body preserved`() {
        // Test con headers y body complejos
        
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
                        restSoap = RestSoapTool(
                            enabled = true,
                            clientHistory = listOf(
                                HttpRequest(
                                    id = "req_complex",
                                    at = "2026-02-16T10:00:00Z",
                                    type = "SOAP",
                                    method = "POST",
                                    url = "https://soap.example.com/service",
                                    headers = mapOf(
                                        "Content-Type" to "text/xml; charset=utf-8",
                                        "SOAPAction" to "\"http://example.com/GetUser\"",
                                        "Authorization" to "Basic dXNlcjpwYXNz",
                                        "X-Custom-1" to "value with spaces",
                                        "X-Custom-2" to "value,with,commas"
                                    ),
                                    body = """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                                          <soap:Body>
                                            <GetUser>
                                              <userId>12345</userId>
                                              <includeDetails>true</includeDetails>
                                            </GetUser>
                                          </soap:Body>
                                        </soap:Envelope>
                                    """.trimIndent(),
                                    response = HttpResponse(
                                        status = 200,
                                        body = """
                                            <?xml version="1.0" encoding="UTF-8"?>
                                            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                                              <soap:Body>
                                                <GetUserResponse>
                                                  <user>
                                                    <id>12345</id>
                                                    <name>John Doe</name>
                                                    <email>john@example.com</email>
                                                  </user>
                                                </GetUserResponse>
                                              </soap:Body>
                                            </soap:Envelope>
                                        """.trimIndent(),
                                        headers = mapOf(
                                            "Content-Type" to "text/xml; charset=utf-8",
                                            "Server" to "Apache/2.4.41"
                                        )
                                    )
                                )
                            ),
                            mockServer = MockServer(
                                enabled = true,
                                listenHost = "127.0.0.1",
                                listenPort = 8089,
                                mode = "defined",
                                routes = listOf(
                                    MockRoute(
                                        id = "route_complex",
                                        method = "POST",
                                        path = "/api/v1/data",
                                        response = HttpResponse(
                                            status = 200,
                                            body = """
                                                {
                                                  "data": [
                                                    {"id": 1, "value": "test \"quoted\" value"},
                                                    {"id": 2, "value": "test with\nnewline"},
                                                    {"id": 3, "value": "test with\ttab"}
                                                  ],
                                                  "meta": {
                                                    "total": 3,
                                                    "page": 1
                                                  }
                                                }
                                            """.trimIndent(),
                                            headers = mapOf(
                                                "Content-Type" to "application/json; charset=utf-8",
                                                "X-Rate-Limit" to "1000",
                                                "X-Rate-Remaining" to "999"
                                            )
                                        )
                                    )
                                ),
                                capturedRequests = listOf(
                                    CapturedRequest(
                                        id = "cap_complex",
                                        at = "2026-02-16T11:00:00Z",
                                        method = "POST",
                                        path = "/webhook",
                                        headers = mapOf(
                                            "Content-Type" to "application/json",
                                            "X-Webhook-Signature" to "sha256=abc123def456"
                                        ),
                                        body = """
                                            {
                                              "event": "user.created",
                                              "data": {
                                                "user_id": "usr_123",
                                                "email": "test@example.com",
                                                "metadata": {
                                                  "source": "api",
                                                  "ip": "192.168.1.1"
                                                }
                                              }
                                            }
                                        """.trimIndent()
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
        val restSoap = workspace2.projects[0].tools.restSoap!!
        
        // Headers complejos
        val request = restSoap.clientHistory[0]
        assertEquals("text/xml; charset=utf-8", request.headers["Content-Type"])
        assertEquals("\"http://example.com/GetUser\"", request.headers["SOAPAction"])
        assertEquals("value with spaces", request.headers["X-Custom-1"])
        assertEquals("value,with,commas", request.headers["X-Custom-2"])
        
        // Body XML con formato
        assert(request.body!!.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
        assert(request.body!!.contains("<GetUser>"))
        assert(request.body!!.contains("<userId>12345</userId>"))
        
        // Response XML
        assert(request.response!!.body!!.contains("<GetUserResponse>"))
        assert(request.response!!.body!!.contains("<name>John Doe</name>"))
        
        // Route con JSON complejo
        val route = restSoap.mockServer!!.routes[0]
        assert(route.response.body!!.contains("quoted"))
        assert(route.response.body!!.contains("newline"))
        assert(route.response.body!!.contains("tab"))
        
        // Captured request con JSON anidado
        val capture = restSoap.mockServer!!.capturedRequests[0]
        assert(capture.body!!.contains("user.created"))
        assert(capture.body!!.contains("metadata"))
        assertEquals("sha256=abc123def456", capture.headers["X-Webhook-Signature"])
    }
    
    @Test
    fun `portable persistence - empty and null values preserved`() {
        // Test casos edge: valores vacíos y null
        
        val workspace1 = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T09:00:00Z",
                updatedAt = "2026-02-16T09:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_edge",
                    name = "Edge Cases Test",
                    description = "Test edge cases",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T09:00:00Z",
                    updatedAt = "2026-02-16T09:00:00Z",
                    tools = ProjectTools(
                        restSoap = RestSoapTool(
                            enabled = true,
                            clientHistory = listOf(
                                // Request sin body ni response
                                HttpRequest(
                                    id = "req_empty",
                                    at = "2026-02-16T10:00:00Z",
                                    type = "REST",
                                    method = "GET",
                                    url = "https://api.example.com/status",
                                    headers = emptyMap(),
                                    body = null,
                                    response = null
                                ),
                                // Request con body vacío
                                HttpRequest(
                                    id = "req_empty_body",
                                    at = "2026-02-16T10:01:00Z",
                                    type = "REST",
                                    method = "POST",
                                    url = "https://api.example.com/ping",
                                    headers = mapOf("Content-Length" to "0"),
                                    body = "",
                                    response = HttpResponse(
                                        status = 204,
                                        body = null,
                                        headers = emptyMap()
                                    )
                                )
                            ),
                            mockServer = MockServer(
                                enabled = false,
                                listenHost = "127.0.0.1",
                                listenPort = 8089,
                                mode = "catchAll",
                                routes = emptyList(),
                                capturedRequests = emptyList()
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
        
        // ASSERT
        val restSoap = workspace2.projects[0].tools.restSoap!!
        
        // Request sin body ni response
        val req1 = restSoap.clientHistory[0]
        assertEquals(null, req1.body)
        assertEquals(null, req1.response)
        assertEquals(0, req1.headers.size)
        
        // Request con body vacío
        val req2 = restSoap.clientHistory[1]
        assertEquals("", req2.body)
        assertEquals(204, req2.response?.status)
        assertEquals(null, req2.response?.body)
        assertEquals(0, req2.response?.headers?.size)
        
        // Mock server deshabilitado con listas vacías
        val mockServer = restSoap.mockServer!!
        assertEquals(false, mockServer.enabled)
        assertEquals(0, mockServer.routes.size)
        assertEquals(0, mockServer.capturedRequests.size)
    }
}

