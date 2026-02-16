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
}

