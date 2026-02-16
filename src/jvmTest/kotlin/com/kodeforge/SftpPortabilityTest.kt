package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SftpUseCases
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.data.repository.JvmFileSystemAdapter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Tests de portabilidad para SftpTool.
 * 
 * Valida que al copiar el workspace JSON, todas las conexiones SFTP
 * y configuración se recuperan exactamente igual.
 */
class SftpPortabilityTest {
    
    @TempDir
    lateinit var tempDir: Path
    
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
                    id = "proj_sftp_portability",
                    name = "SFTP Portability Test",
                    description = "Test project for SFTP portability",
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
    
    @Test
    fun `basic connection persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_basic.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = SftpUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Production SFTP",
            host = "sftp.production.com",
            port = 22,
            username = "deploy",
            authType = "password",
            authValueRef = "secret:sftp_prod"
        ).getOrThrow()
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val originalTool = workspace.projects[0].tools.sftp!!
        val reloadedTool = reloadedWorkspace.projects[0].tools.sftp!!
        
        assertEquals(originalTool.enabled, reloadedTool.enabled)
        assertEquals(originalTool.connections.size, reloadedTool.connections.size)
        
        val originalConn = originalTool.connections[0]
        val reloadedConn = reloadedTool.connections[0]
        
        assertEquals(originalConn.id, reloadedConn.id)
        assertEquals(originalConn.name, reloadedConn.name)
        assertEquals(originalConn.host, reloadedConn.host)
        assertEquals(originalConn.port, reloadedConn.port)
        assertEquals(originalConn.username, reloadedConn.username)
        assertEquals(originalConn.auth.type, reloadedConn.auth.type)
        assertEquals(originalConn.auth.valueRef, reloadedConn.auth.valueRef)
        
        assertEquals("Production SFTP", reloadedConn.name)
        assertEquals("sftp.production.com", reloadedConn.host)
        assertEquals(22, reloadedConn.port)
        assertEquals("deploy", reloadedConn.username)
        assertEquals("password", reloadedConn.auth.type)
        assertEquals("secret:sftp_prod", reloadedConn.auth.valueRef)
    }
    
    @Test
    fun `multiple connections persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_multiple.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = SftpUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir 3 conexiones con diferentes configuraciones
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Production",
            host = "prod.example.com",
            port = 22,
            username = "prod_user",
            authType = "password",
            authValueRef = "secret:prod"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Staging",
            host = "staging.example.com",
            port = 2222,
            username = "staging_user",
            authType = "key",
            authValueRef = "secret:staging_key"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Development",
            host = "localhost",
            port = 22,
            username = "dev",
            authType = "none",
            authValueRef = ""
        ).getOrThrow()
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val originalConnections = workspace.projects[0].tools.sftp!!.connections
        val reloadedConnections = reloadedWorkspace.projects[0].tools.sftp!!.connections
        
        assertEquals(originalConnections.size, reloadedConnections.size)
        assertEquals(3, reloadedConnections.size)
        
        // Validar cada conexión
        for (i in originalConnections.indices) {
            val original = originalConnections[i]
            val reloaded = reloadedConnections[i]
            
            assertEquals(original.id, reloaded.id)
            assertEquals(original.name, reloaded.name)
            assertEquals(original.host, reloaded.host)
            assertEquals(original.port, reloaded.port)
            assertEquals(original.username, reloaded.username)
            assertEquals(original.auth.type, reloaded.auth.type)
            assertEquals(original.auth.valueRef, reloaded.auth.valueRef)
        }
        
        // Validar específicamente cada una
        assertEquals("Production", reloadedConnections[0].name)
        assertEquals("password", reloadedConnections[0].auth.type)
        
        assertEquals("Staging", reloadedConnections[1].name)
        assertEquals(2222, reloadedConnections[1].port)
        assertEquals("key", reloadedConnections[1].auth.type)
        
        assertEquals("Development", reloadedConnections[2].name)
        assertEquals("none", reloadedConnections[2].auth.type)
    }
    
    @Test
    fun `enabled state persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_enabled.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = SftpUseCases()
        
        var workspace = createTestWorkspace()
        
        // Habilitar tool
        workspace = useCases.enableSftpTool(workspace, "proj_sftp_portability").getOrThrow()
        
        // Añadir conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Test Server",
            host = "test.com",
            port = 22,
            username = "test",
            authType = "password",
            authValueRef = "secret:test"
        ).getOrThrow()
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val reloadedTool = reloadedWorkspace.projects[0].tools.sftp!!
        
        assertTrue(reloadedTool.enabled)
        assertEquals(1, reloadedTool.connections.size)
    }
    
    @Test
    fun `special characters in connection data persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_special_chars.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = SftpUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir conexión con caracteres especiales
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Server with 'quotes' and \"double\" and áéíóú",
            host = "sftp-ñ.example.com",
            port = 22,
            username = "user@domain.com",
            authType = "password",
            authValueRef = "secret:with/slashes\\and:colons"
        ).getOrThrow()
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val reloadedConn = reloadedWorkspace.projects[0].tools.sftp!!.connections[0]
        
        assertTrue(reloadedConn.name.contains("'quotes'"))
        assertTrue(reloadedConn.name.contains("\"double\""))
        assertTrue(reloadedConn.name.contains("áéíóú"))
        assertTrue(reloadedConn.host.contains("ñ"))
        assertTrue(reloadedConn.username.contains("@"))
        assertTrue(reloadedConn.auth.valueRef.contains("/"))
        assertTrue(reloadedConn.auth.valueRef.contains("\\"))
        assertTrue(reloadedConn.auth.valueRef.contains(":"))
    }
    
    @Test
    fun `complete workflow - add, update, delete persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_workflow.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = SftpUseCases()
        
        // 1. Crear workspace
        var workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        // 2. Habilitar tool
        workspace = useCases.enableSftpTool(workspace, "proj_sftp_portability").getOrThrow()
        
        // 3. Añadir 3 conexiones
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Connection 1",
            host = "server1.com",
            port = 22,
            username = "user1",
            authType = "password",
            authValueRef = "secret:1"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Connection 2",
            host = "server2.com",
            port = 2222,
            username = "user2",
            authType = "key",
            authValueRef = "secret:2"
        ).getOrThrow()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            name = "Connection 3",
            host = "server3.com",
            port = 22,
            username = "user3",
            authType = "password",
            authValueRef = "secret:3"
        ).getOrThrow()
        
        // 4. Actualizar la segunda conexión
        val conn2Id = useCases.getConnections(workspace, "proj_sftp_portability")[1].id
        workspace = useCases.updateConnection(
            workspace = workspace,
            projectId = "proj_sftp_portability",
            connectionId = conn2Id,
            name = "Connection 2 Updated",
            host = "server2-new.com",
            port = 3333,
            username = "user2_new",
            authType = "key",
            authValueRef = "secret:2_new"
        ).getOrThrow()
        
        // 5. Eliminar la tercera conexión
        val conn3Id = useCases.getConnections(workspace, "proj_sftp_portability")[2].id
        workspace = useCases.deleteConnection(workspace, "proj_sftp_portability", conn3Id).getOrThrow()
        
        // 6. Guardar
        repository.save(workspacePath, workspace)
        
        // 7. Recargar (simula copiar JSON a otro sistema)
        val reloadedWorkspace = repository.load(workspacePath)
        
        // 8. Validar TODO
        val reloadedTool = reloadedWorkspace.projects[0].tools.sftp!!
        
        assertTrue(reloadedTool.enabled)
        assertEquals(2, reloadedTool.connections.size)
        
        // Primera conexión (sin cambios)
        val conn1 = reloadedTool.connections[0]
        assertEquals("Connection 1", conn1.name)
        assertEquals("server1.com", conn1.host)
        assertEquals(22, conn1.port)
        assertEquals("user1", conn1.username)
        
        // Segunda conexión (actualizada)
        val conn2 = reloadedTool.connections[1]
        assertEquals("Connection 2 Updated", conn2.name)
        assertEquals("server2-new.com", conn2.host)
        assertEquals(3333, conn2.port)
        assertEquals("user2_new", conn2.username)
        assertEquals("secret:2_new", conn2.auth.valueRef)
        
        // Tercera conexión (eliminada) no debe existir
        val conn3Exists = reloadedTool.connections.any { it.id == conn3Id }
        assertTrue(!conn3Exists)
    }
    
    @Test
    fun `empty connections list persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_empty.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        
        val workspace = createTestWorkspace()
        
        // Guardar sin conexiones
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val reloadedTool = reloadedWorkspace.projects[0].tools.sftp!!
        
        assertTrue(!reloadedTool.enabled)
        assertEquals(0, reloadedTool.connections.size)
    }
}

