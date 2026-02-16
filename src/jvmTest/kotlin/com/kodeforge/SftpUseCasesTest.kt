package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SftpUseCases
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests para SftpUseCases.
 */
class SftpUseCasesTest {
    
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
                    id = "proj_sftp_test",
                    name = "SFTP Test Project",
                    description = "Test project for SFTP",
                    status = "active",
                    members = emptyList(),
                    createdAt = "2026-02-16T12:00:00Z",
                    updatedAt = "2026-02-16T12:00:00Z",
                    tools = ProjectTools(
                        sftp = SftpTool(
                            enabled = false,
                            connections = emptyList()
                        )
                    )
                )
            ),
            tasks = emptyList(),
            planning = Planning(),
            uiState = UiState(),
            secrets = Secrets()
        )
    }
    
    // ===== Configuración =====
    
    @Test
    fun `enable SFTP tool`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.enableSftpTool(workspace, "proj_sftp_test")
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val tool = useCases.getSftpTool(updatedWorkspace, "proj_sftp_test")
        
        assertNotNull(tool)
        assertTrue(tool.enabled)
    }
    
    @Test
    fun `disable SFTP tool`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        // Primero habilitar
        workspace = useCases.enableSftpTool(workspace, "proj_sftp_test").getOrThrow()
        
        // Luego deshabilitar
        val result = useCases.disableSftpTool(workspace, "proj_sftp_test")
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val tool = useCases.getSftpTool(updatedWorkspace, "proj_sftp_test")
        
        assertNotNull(tool)
        assertTrue(!tool.enabled)
    }
    
    // ===== Conexiones CRUD =====
    
    @Test
    fun `add connection with password auth`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Production Server",
            host = "sftp.example.com",
            port = 22,
            username = "deploy",
            authType = "password",
            authValueRef = "secret:sftp_prod"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connections = useCases.getConnections(updatedWorkspace, "proj_sftp_test")
        
        assertEquals(1, connections.size)
        
        val connection = connections[0]
        assertEquals("Production Server", connection.name)
        assertEquals("sftp.example.com", connection.host)
        assertEquals(22, connection.port)
        assertEquals("deploy", connection.username)
        assertEquals("password", connection.auth.type)
        assertEquals("secret:sftp_prod", connection.auth.valueRef)
    }
    
    @Test
    fun `add connection with key auth`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Dev Server",
            host = "dev.example.com",
            port = 2222,
            username = "developer",
            authType = "key",
            authValueRef = "secret:sftp_dev_key"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connections = useCases.getConnections(updatedWorkspace, "proj_sftp_test")
        
        assertEquals(1, connections.size)
        
        val connection = connections[0]
        assertEquals("Dev Server", connection.name)
        assertEquals("dev.example.com", connection.host)
        assertEquals(2222, connection.port)
        assertEquals("developer", connection.username)
        assertEquals("key", connection.auth.type)
        assertEquals("secret:sftp_dev_key", connection.auth.valueRef)
    }
    
    @Test
    fun `add multiple connections`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Server 1",
            host = "server1.com",
            port = 22,
            username = "user1",
            authType = "password",
            authValueRef = "secret:1"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Server 2",
            host = "server2.com",
            port = 22,
            username = "user2",
            authType = "key",
            authValueRef = "secret:2"
        ).getOrThrow()
        
        val connections = useCases.getConnections(workspace, "proj_sftp_test")
        assertEquals(2, connections.size)
        assertEquals("Server 1", connections[0].name)
        assertEquals("Server 2", connections[1].name)
    }
    
    @Test
    fun `cannot add connection with duplicate name`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Production",
            host = "prod.com",
            port = 22,
            username = "user",
            authType = "password",
            authValueRef = "secret:prod"
        ).getOrThrow()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Production",
            host = "prod2.com",
            port = 22,
            username = "user2",
            authType = "password",
            authValueRef = "secret:prod2"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
    }
    
    @Test
    fun `cannot add connection with blank name`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "",
            host = "server.com",
            port = 22,
            username = "user",
            authType = "password",
            authValueRef = "secret:test"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("name cannot be blank") == true)
    }
    
    @Test
    fun `cannot add connection with blank host`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Test Server",
            host = "",
            port = 22,
            username = "user",
            authType = "password",
            authValueRef = "secret:test"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Host cannot be blank") == true)
    }
    
    @Test
    fun `cannot add connection with invalid port`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Test Server",
            host = "server.com",
            port = 99999,
            username = "user",
            authType = "password",
            authValueRef = "secret:test"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Port must be between") == true)
    }
    
    @Test
    fun `cannot add connection with blank username`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Test Server",
            host = "server.com",
            port = 22,
            username = "",
            authType = "password",
            authValueRef = "secret:test"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Username cannot be blank") == true)
    }
    
    @Test
    fun `update connection`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Original Name",
            host = "original.com",
            port = 22,
            username = "user",
            authType = "password",
            authValueRef = "secret:original"
        ).getOrThrow()
        
        val connectionId = useCases.getConnections(workspace, "proj_sftp_test")[0].id
        
        val result = useCases.updateConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            connectionId = connectionId,
            name = "Updated Name",
            host = "updated.com",
            port = 2222,
            username = "newuser",
            authType = "key",
            authValueRef = "secret:updated"
        )
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connection = useCases.getConnection(updatedWorkspace, "proj_sftp_test", connectionId)
        
        assertNotNull(connection)
        assertEquals("Updated Name", connection.name)
        assertEquals("updated.com", connection.host)
        assertEquals(2222, connection.port)
        assertEquals("newuser", connection.username)
        assertEquals("key", connection.auth.type)
        assertEquals("secret:updated", connection.auth.valueRef)
    }
    
    @Test
    fun `cannot update connection to duplicate name`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Connection 1",
            host = "server1.com",
            port = 22,
            username = "user1",
            authType = "password",
            authValueRef = "secret:1"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Connection 2",
            host = "server2.com",
            port = 22,
            username = "user2",
            authType = "password",
            authValueRef = "secret:2"
        ).getOrThrow()
        
        val connectionId = useCases.getConnections(workspace, "proj_sftp_test")[1].id
        
        val result = useCases.updateConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            connectionId = connectionId,
            name = "Connection 1",
            host = "server2.com",
            port = 22,
            username = "user2",
            authType = "password",
            authValueRef = "secret:2"
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
    }
    
    @Test
    fun `delete connection`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "To Delete",
            host = "delete.com",
            port = 22,
            username = "user",
            authType = "password",
            authValueRef = "secret:delete"
        ).getOrThrow()
        
        val connectionId = useCases.getConnections(workspace, "proj_sftp_test")[0].id
        
        val result = useCases.deleteConnection(workspace, "proj_sftp_test", connectionId)
        
        assertTrue(result.isSuccess)
        val updatedWorkspace = result.getOrThrow()
        val connections = useCases.getConnections(updatedWorkspace, "proj_sftp_test")
        
        assertEquals(0, connections.size)
        assertNull(useCases.getConnection(updatedWorkspace, "proj_sftp_test", connectionId))
    }
    
    @Test
    fun `cannot delete non-existent connection`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val result = useCases.deleteConnection(workspace, "proj_sftp_test", "non_existent_id")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }
    
    @Test
    fun `get connection by id`() {
        val useCases = SftpUseCases()
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_test",
            name = "Test Connection",
            host = "test.com",
            port = 22,
            username = "testuser",
            authType = "password",
            authValueRef = "secret:test"
        ).getOrThrow()
        
        val connectionId = useCases.getConnections(workspace, "proj_sftp_test")[0].id
        val connection = useCases.getConnection(workspace, "proj_sftp_test", connectionId)
        
        assertNotNull(connection)
        assertEquals("Test Connection", connection.name)
        assertEquals(connectionId, connection.id)
    }
    
    @Test
    fun `get non-existent connection returns null`() {
        val useCases = SftpUseCases()
        val workspace = createTestWorkspace()
        
        val connection = useCases.getConnection(workspace, "proj_sftp_test", "non_existent_id")
        
        assertNull(connection)
    }
}

