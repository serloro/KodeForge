package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.DbToolUseCases
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests para DbToolUseCases.
 */
class DbToolUseCasesTest {
    
    private val useCases = DbToolUseCases()
    
    private fun createTestWorkspace(): Workspace {
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T12:00:00Z",
                updatedAt = "2026-02-16T12:00:00Z"
            ),
            people = emptyList(),
            projects = listOf(
                Project(
                    id = "proj_db_test",
                    name = "DB Test Project",
                    description = "Test project for DB tools",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T12:00:00Z",
                    updatedAt = "2026-02-16T12:00:00Z",
                    tools = ProjectTools(
                        dbTools = DbTool(
                            enabled = false,
                            connections = emptyList(),
                            savedQueries = emptyList()
                        )
                    )
                )
            ),
            tasks = emptyList()
        )
    }
    
    // ===== Tests de Configuración =====
    
    @Test
    fun `enableDbTool - enables the tool`() {
        val workspace = createTestWorkspace()
        
        val result = useCases.enableDbTool(workspace, "proj_db_test")
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val dbTool = updatedWorkspace.projects[0].tools.dbTools
        
        assertNotNull(dbTool)
        assertTrue(dbTool.enabled)
    }
    
    @Test
    fun `disableDbTool - disables the tool`() {
        var workspace = createTestWorkspace()
        workspace = useCases.enableDbTool(workspace, "proj_db_test").getOrThrow()
        
        val result = useCases.disableDbTool(workspace, "proj_db_test")
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val dbTool = updatedWorkspace.projects[0].tools.dbTools
        
        assertNotNull(dbTool)
        assertTrue(!dbTool.enabled)
    }
    
    // ===== Tests de Conexiones =====
    
    @Test
    fun `addConnection - adds a new connection`() {
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connections = updatedWorkspace.projects[0].tools.dbTools?.connections ?: emptyList()
        
        assertEquals(1, connections.size)
        
        val connection = connections[0]
        assertEquals("Postgres Local", connection.name)
        assertEquals("postgres", connection.type)
        assertEquals("127.0.0.1", connection.host)
        assertEquals(5432, connection.port)
        assertEquals("testdb", connection.database)
        assertEquals("postgres", connection.username)
        assertEquals("password", connection.auth.type)
        assertEquals("secret:db_001", connection.auth.valueRef)
    }
    
    @Test
    fun `addConnection - fails with duplicate name`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local", // Mismo nombre
            type = "mysql",
            host = "localhost",
            port = 3306,
            database = "otherdb",
            username = "root",
            authType = "password",
            authValueRef = "secret:db_002"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Ya existe") == true)
    }
    
    @Test
    fun `addConnection - validates port range`() {
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Invalid Port",
            type = "postgres",
            host = "127.0.0.1",
            port = 99999, // Puerto inválido
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("puerto") == true)
    }
    
    @Test
    fun `addConnection - validates database type`() {
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Invalid Type",
            type = "invaliddb", // Tipo inválido
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("no soportado") == true)
    }
    
    @Test
    fun `updateConnection - updates existing connection`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        val result = useCases.updateConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            connectionId = connectionId,
            name = "Postgres Production",
            port = 5433
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connection = updatedWorkspace.projects[0].tools.dbTools?.connections?.get(0)
        
        assertNotNull(connection)
        assertEquals("Postgres Production", connection.name)
        assertEquals(5433, connection.port)
        assertEquals("postgres", connection.type) // No cambió
        assertEquals("127.0.0.1", connection.host) // No cambió
    }
    
    @Test
    fun `deleteConnection - removes connection`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        val result = useCases.deleteConnection(workspace, "proj_db_test", connectionId)
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connections = updatedWorkspace.projects[0].tools.dbTools?.connections ?: emptyList()
        
        assertEquals(0, connections.size)
    }
    
    @Test
    fun `deleteConnection - fails if queries use the connection`() {
        var workspace = createTestWorkspace()
        
        // Añadir conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        // Añadir query que usa la conexión
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = connectionId,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        // Intentar eliminar la conexión
        val result = useCases.deleteConnection(workspace, "proj_db_test", connectionId)
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("query") == true)
    }
    
    // ===== Tests de Queries =====
    
    @Test
    fun `addSavedQuery - adds a new query`() {
        var workspace = createTestWorkspace()
        
        // Primero añadir una conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        val result = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = connectionId,
            sql = "SELECT 1;"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val queries = updatedWorkspace.projects[0].tools.dbTools?.savedQueries ?: emptyList()
        
        assertEquals(1, queries.size)
        
        val query = queries[0]
        assertEquals("Health Check", query.name)
        assertEquals(connectionId, query.connectionId)
        assertEquals("SELECT 1;", query.sql)
    }
    
    @Test
    fun `addSavedQuery - fails with non-existent connection`() {
        val workspace = createTestWorkspace()
        
        val result = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = "nonexistent_conn",
            sql = "SELECT 1;"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("no encontrada") == true)
    }
    
    @Test
    fun `addSavedQuery - fails with duplicate name`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = connectionId,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        val result = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check", // Mismo nombre
            connectionId = connectionId,
            sql = "SELECT 2;"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Ya existe") == true)
    }
    
    @Test
    fun `updateSavedQuery - updates existing query`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = connectionId,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        val queryId = workspace.projects[0].tools.dbTools?.savedQueries?.get(0)?.id!!
        
        val result = useCases.updateSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            queryId = queryId,
            name = "Health Check v2",
            sql = "SELECT 1 AS health;"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val query = updatedWorkspace.projects[0].tools.dbTools?.savedQueries?.get(0)
        
        assertNotNull(query)
        assertEquals("Health Check v2", query.name)
        assertEquals("SELECT 1 AS health;", query.sql)
    }
    
    @Test
    fun `deleteSavedQuery - removes query`() {
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Health Check",
            connectionId = connectionId,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        val queryId = workspace.projects[0].tools.dbTools?.savedQueries?.get(0)?.id!!
        
        val result = useCases.deleteSavedQuery(workspace, "proj_db_test", queryId)
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val queries = updatedWorkspace.projects[0].tools.dbTools?.savedQueries ?: emptyList()
        
        assertEquals(0, queries.size)
    }
    
    @Test
    fun `getQueriesByConnection - returns queries for specific connection`() {
        var workspace = createTestWorkspace()
        
        // Añadir dos conexiones
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val conn1Id = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "MySQL Local",
            type = "mysql",
            host = "127.0.0.1",
            port = 3306,
            database = "testdb",
            username = "root",
            authType = "password",
            authValueRef = "secret:db_002"
        ).getOrThrow()
        
        val conn2Id = workspace.projects[0].tools.dbTools?.connections?.get(1)?.id!!
        
        // Añadir queries a cada conexión
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Query 1",
            connectionId = conn1Id,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "Postgres Query 2",
            connectionId = conn1Id,
            sql = "SELECT 2;"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_test",
            name = "MySQL Query 1",
            connectionId = conn2Id,
            sql = "SELECT 1;"
        ).getOrThrow()
        
        // Obtener queries de la primera conexión
        val queries = useCases.getQueriesByConnection(workspace, "proj_db_test", conn1Id)
        
        assertEquals(2, queries.size)
        assertTrue(queries.all { it.connectionId == conn1Id })
    }
}

