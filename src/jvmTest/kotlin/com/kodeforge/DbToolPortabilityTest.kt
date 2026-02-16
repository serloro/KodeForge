package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.DbToolUseCases
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
 * Tests de portabilidad para DbTool.
 * 
 * Valida que al copiar el workspace JSON, todas las conexiones,
 * queries guardadas y configuración se recuperan exactamente igual.
 */
class DbToolPortabilityTest {
    
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
                    id = "proj_db_portability",
                    name = "DB Portability Test",
                    description = "Test project for DB portability",
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
    
    @Test
    fun `full workflow - connections and queries persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        // 1. Crear workspace inicial
        var workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        // 2. Habilitar DbTool
        workspace = useCases.enableDbTool(workspace, "proj_db_portability").getOrThrow()
        
        // 3. Añadir conexiones
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Postgres Local",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "kodeforge",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val conn1Id = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "MySQL Production",
            type = "mysql",
            host = "prod.example.com",
            port = 3306,
            database = "production_db",
            username = "admin",
            authType = "key",
            authValueRef = "secret:db_002"
        ).getOrThrow()
        
        val conn2Id = workspace.projects[0].tools.dbTools?.connections?.get(1)?.id!!
        
        // 4. Añadir queries
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Health Check",
            connectionId = conn1Id,
            sql = "SELECT 1 AS health;"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "List Users",
            connectionId = conn1Id,
            sql = "SELECT id, username, email FROM users WHERE active = true ORDER BY created_at DESC;"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Production Stats",
            connectionId = conn2Id,
            sql = "SELECT COUNT(*) as total, DATE(created_at) as date FROM orders GROUP BY DATE(created_at);"
        ).getOrThrow()
        
        // 5. Guardar workspace
        repository.save(workspacePath, workspace)
        
        // 6. Recargar workspace (simula copiar JSON)
        val reloadedWorkspace = repository.load(workspacePath)
        
        // 7. Validar que TODO se recuperó exactamente igual
        val originalProject = workspace.projects.find { it.id == "proj_db_portability" }!!
        val reloadedProject = reloadedWorkspace.projects.find { it.id == "proj_db_portability" }!!
        
        val originalDb = originalProject.tools.dbTools!!
        val reloadedDb = reloadedProject.tools.dbTools!!
        
        // Configuración
        assertEquals(originalDb.enabled, reloadedDb.enabled)
        assertTrue(reloadedDb.enabled)
        
        // Conexiones
        assertEquals(originalDb.connections.size, reloadedDb.connections.size)
        assertEquals(2, reloadedDb.connections.size)
        
        // Conexión 1
        val conn1Original = originalDb.connections[0]
        val conn1Reloaded = reloadedDb.connections[0]
        assertEquals(conn1Original.id, conn1Reloaded.id)
        assertEquals(conn1Original.name, conn1Reloaded.name)
        assertEquals(conn1Original.type, conn1Reloaded.type)
        assertEquals(conn1Original.host, conn1Reloaded.host)
        assertEquals(conn1Original.port, conn1Reloaded.port)
        assertEquals(conn1Original.database, conn1Reloaded.database)
        assertEquals(conn1Original.username, conn1Reloaded.username)
        assertEquals(conn1Original.auth.type, conn1Reloaded.auth.type)
        assertEquals(conn1Original.auth.valueRef, conn1Reloaded.auth.valueRef)
        
        assertEquals("Postgres Local", conn1Reloaded.name)
        assertEquals("postgres", conn1Reloaded.type)
        assertEquals("127.0.0.1", conn1Reloaded.host)
        assertEquals(5432, conn1Reloaded.port)
        assertEquals("kodeforge", conn1Reloaded.database)
        assertEquals("postgres", conn1Reloaded.username)
        assertEquals("password", conn1Reloaded.auth.type)
        assertEquals("secret:db_001", conn1Reloaded.auth.valueRef)
        
        // Conexión 2
        val conn2Original = originalDb.connections[1]
        val conn2Reloaded = reloadedDb.connections[1]
        assertEquals(conn2Original.id, conn2Reloaded.id)
        assertEquals("MySQL Production", conn2Reloaded.name)
        assertEquals("mysql", conn2Reloaded.type)
        assertEquals("prod.example.com", conn2Reloaded.host)
        assertEquals(3306, conn2Reloaded.port)
        assertEquals("key", conn2Reloaded.auth.type)
        
        // Queries
        assertEquals(originalDb.savedQueries.size, reloadedDb.savedQueries.size)
        assertEquals(3, reloadedDb.savedQueries.size)
        
        // Query 1
        val query1Original = originalDb.savedQueries[0]
        val query1Reloaded = reloadedDb.savedQueries[0]
        assertEquals(query1Original.id, query1Reloaded.id)
        assertEquals(query1Original.name, query1Reloaded.name)
        assertEquals(query1Original.connectionId, query1Reloaded.connectionId)
        assertEquals(query1Original.sql, query1Reloaded.sql)
        
        assertEquals("Health Check", query1Reloaded.name)
        assertEquals(conn1Id, query1Reloaded.connectionId)
        assertEquals("SELECT 1 AS health;", query1Reloaded.sql)
        
        // Query 2 - SQL complejo
        val query2Reloaded = reloadedDb.savedQueries[1]
        assertEquals("List Users", query2Reloaded.name)
        assertTrue(query2Reloaded.sql.contains("WHERE active = true"))
        assertTrue(query2Reloaded.sql.contains("ORDER BY created_at DESC"))
        
        // Query 3
        val query3Reloaded = reloadedDb.savedQueries[2]
        assertEquals("Production Stats", query3Reloaded.name)
        assertEquals(conn2Id, query3Reloaded.connectionId)
        assertTrue(query3Reloaded.sql.contains("GROUP BY"))
    }
    
    @Test
    fun `empty configuration persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_empty.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        
        val workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedDb = reloadedWorkspace.projects[0].tools.dbTools!!
        
        assertEquals(false, reloadedDb.enabled)
        assertEquals(0, reloadedDb.connections.size)
        assertEquals(0, reloadedDb.savedQueries.size)
    }
    
    @Test
    fun `multiple connections with different types persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_multi_types.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir conexiones de diferentes tipos
        val types = listOf(
            Triple("postgres", 5432, "PostgreSQL"),
            Triple("mysql", 3306, "MySQL"),
            Triple("sqlite", 1, "SQLite"), // SQLite no usa puerto real, pero ponemos 1 para validación
            Triple("oracle", 1521, "Oracle"),
            Triple("mongodb", 27017, "MongoDB")
        )
        
        types.forEach { (type, port, name) ->
            workspace = useCases.addConnection(
                workspace = workspace,
                projectId = "proj_db_portability",
                name = "$name Connection",
                type = type,
                host = "127.0.0.1",
                port = port,
                database = "testdb",
                username = "user",
                authType = "password",
                authValueRef = "secret:$type"
            ).getOrThrow()
        }
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedConnections = reloadedWorkspace.projects[0].tools.dbTools?.connections ?: emptyList()
        
        assertEquals(5, reloadedConnections.size)
        
        // Verificar cada tipo
        assertEquals("postgres", reloadedConnections[0].type)
        assertEquals(5432, reloadedConnections[0].port)
        
        assertEquals("mysql", reloadedConnections[1].type)
        assertEquals(3306, reloadedConnections[1].port)
        
        assertEquals("sqlite", reloadedConnections[2].type)
        
        assertEquals("oracle", reloadedConnections[3].type)
        assertEquals(1521, reloadedConnections[3].port)
        
        assertEquals("mongodb", reloadedConnections[4].type)
        assertEquals(27017, reloadedConnections[4].port)
    }
    
    @Test
    fun `complex SQL queries persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_complex_sql.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Test DB",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "testdb",
            username = "postgres",
            authType = "password",
            authValueRef = "secret:db_001"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        // SQL con caracteres especiales, multilinea, etc.
        val complexSql = """
            WITH recent_orders AS (
                SELECT 
                    o.id,
                    o.customer_id,
                    o.total,
                    o.created_at,
                    c.name AS customer_name
                FROM orders o
                INNER JOIN customers c ON o.customer_id = c.id
                WHERE o.created_at >= NOW() - INTERVAL '30 days'
                    AND o.status IN ('completed', 'shipped')
            )
            SELECT 
                customer_id,
                customer_name,
                COUNT(*) as order_count,
                SUM(total) as total_spent,
                AVG(total) as avg_order_value
            FROM recent_orders
            GROUP BY customer_id, customer_name
            HAVING COUNT(*) > 5
            ORDER BY total_spent DESC
            LIMIT 100;
        """.trimIndent()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Top Customers Report",
            connectionId = connectionId,
            sql = complexSql
        ).getOrThrow()
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedQuery = reloadedWorkspace.projects[0].tools.dbTools?.savedQueries?.get(0)
        
        assertNotNull(reloadedQuery)
        assertEquals("Top Customers Report", reloadedQuery.name)
        assertEquals(complexSql, reloadedQuery.sql)
        
        // Verificar que el SQL complejo se preservó
        assertTrue(reloadedQuery.sql.contains("WITH recent_orders AS"))
        assertTrue(reloadedQuery.sql.contains("INNER JOIN"))
        assertTrue(reloadedQuery.sql.contains("GROUP BY"))
        assertTrue(reloadedQuery.sql.contains("HAVING"))
        assertTrue(reloadedQuery.sql.contains("LIMIT 100"))
    }
    
    @Test
    fun `special characters in names and values persist correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_special_chars.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        var workspace = createTestWorkspace()
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Conexión con áéíóú ñ 中文",
            type = "postgres",
            host = "127.0.0.1",
            port = 5432,
            database = "test_db_áéíóú",
            username = "user_ñ",
            authType = "password",
            authValueRef = "secret:db_special_áéíóú"
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Query con 'comillas' y \"dobles\" y áéíóú",
            connectionId = connectionId,
            sql = "SELECT * FROM users WHERE name = 'José García' AND city = \"São Paulo\";"
        ).getOrThrow()
        
        repository.save(workspacePath, workspace)
        val reloadedWorkspace = repository.load(workspacePath)
        
        val reloadedConnection = reloadedWorkspace.projects[0].tools.dbTools?.connections?.get(0)
        val reloadedQuery = reloadedWorkspace.projects[0].tools.dbTools?.savedQueries?.get(0)
        
        assertNotNull(reloadedConnection)
        assertTrue(reloadedConnection.name.contains("áéíóú ñ 中文"))
        assertTrue(reloadedConnection.database.contains("áéíóú"))
        assertTrue(reloadedConnection.username.contains("ñ"))
        
        assertNotNull(reloadedQuery)
        assertTrue(reloadedQuery.name.contains("'comillas'"))
        assertTrue(reloadedQuery.name.contains("\"dobles\""))
        assertTrue(reloadedQuery.name.contains("áéíóú"))
        assertTrue(reloadedQuery.sql.contains("José García"))
        assertTrue(reloadedQuery.sql.contains("São Paulo"))
    }
    
    @Test
    fun `execution history persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_history.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "SQLite Test",
            type = "sqlite",
            host = "localhost",
            port = 1,
            database = ":memory:",
            username = "test",
            authType = "none",
            authValueRef = ""
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        // Añadir ejecuciones al historial
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = connectionId,
            sql = "SELECT * FROM users;",
            success = true,
            rowCount = 10,
            executionTimeMs = 25,
            error = null
        ).getOrThrow()
        
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = connectionId,
            sql = "SELECT COUNT(*) FROM orders;",
            success = true,
            rowCount = 1,
            executionTimeMs = 15,
            error = null
        ).getOrThrow()
        
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = connectionId,
            sql = "SELECT * FROM nonexistent;",
            success = false,
            rowCount = 0,
            executionTimeMs = 5,
            error = "no such table: nonexistent"
        ).getOrThrow()
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar
        val originalHistory = workspace.projects[0].tools.dbTools?.executionHistory ?: emptyList()
        val reloadedHistory = reloadedWorkspace.projects[0].tools.dbTools?.executionHistory ?: emptyList()
        
        assertEquals(originalHistory.size, reloadedHistory.size)
        assertEquals(3, reloadedHistory.size)
        
        // Ejecución 1 (exitosa)
        val exec1Original = originalHistory[0]
        val exec1Reloaded = reloadedHistory[0]
        assertEquals(exec1Original.id, exec1Reloaded.id)
        assertEquals(exec1Original.executedAt, exec1Reloaded.executedAt)
        assertEquals(exec1Original.connectionId, exec1Reloaded.connectionId)
        assertEquals(exec1Original.sql, exec1Reloaded.sql)
        assertEquals(exec1Original.success, exec1Reloaded.success)
        assertEquals(exec1Original.rowCount, exec1Reloaded.rowCount)
        assertEquals(exec1Original.executionTimeMs, exec1Reloaded.executionTimeMs)
        assertEquals(exec1Original.error, exec1Reloaded.error)
        
        assertTrue(exec1Reloaded.success)
        assertEquals("SELECT * FROM users;", exec1Reloaded.sql)
        assertEquals(10, exec1Reloaded.rowCount)
        assertEquals(25, exec1Reloaded.executionTimeMs)
        
        // Ejecución 2 (exitosa)
        val exec2Reloaded = reloadedHistory[1]
        assertTrue(exec2Reloaded.success)
        assertEquals("SELECT COUNT(*) FROM orders;", exec2Reloaded.sql)
        assertEquals(1, exec2Reloaded.rowCount)
        
        // Ejecución 3 (error)
        val exec3Reloaded = reloadedHistory[2]
        assertTrue(!exec3Reloaded.success)
        assertEquals("SELECT * FROM nonexistent;", exec3Reloaded.sql)
        assertEquals("no such table: nonexistent", exec3Reloaded.error)
    }
    
    @Test
    fun `large execution history persists correctly`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_large_history.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        var workspace = createTestWorkspace()
        
        // Añadir conexión
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Test DB",
            type = "sqlite",
            host = "localhost",
            port = 1,
            database = "test.db",
            username = "test",
            authType = "none",
            authValueRef = ""
        ).getOrThrow()
        
        val connectionId = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        // Añadir 60 ejecuciones (más del límite de 50)
        repeat(60) { i ->
            workspace = useCases.addExecutionToHistory(
                workspace = workspace,
                projectId = "proj_db_portability",
                connectionId = connectionId,
                sql = "SELECT * FROM table_$i;",
                success = i % 5 != 0, // Cada 5 es error
                rowCount = i,
                executionTimeMs = (i * 10).toLong(),
                error = if (i % 5 == 0) "Error $i" else null
            ).getOrThrow()
        }
        
        // Guardar
        repository.save(workspacePath, workspace)
        
        // Recargar
        val reloadedWorkspace = repository.load(workspacePath)
        
        // Validar que solo se mantienen las últimas 50
        val reloadedHistory = reloadedWorkspace.projects[0].tools.dbTools?.executionHistory ?: emptyList()
        
        assertEquals(50, reloadedHistory.size)
        
        // Verificar que son las últimas 50 (del 10 al 59)
        assertEquals("SELECT * FROM table_10;", reloadedHistory[0].sql)
        assertEquals("SELECT * FROM table_59;", reloadedHistory[49].sql)
        
        // Verificar que los errores se preservaron
        val errorsCount = reloadedHistory.count { !it.success }
        assertTrue(errorsCount > 0)
    }
    
    @Test
    fun `complete workflow - connections, queries and history persist together`() = runBlocking {
        val workspacePath = tempDir.resolve("workspace_complete.json").toString()
        val fileSystem = JvmFileSystemAdapter()
        val repository = WorkspaceRepository(fileSystem)
        val useCases = DbToolUseCases()
        
        // 1. Crear workspace
        var workspace = createTestWorkspace()
        repository.save(workspacePath, workspace)
        
        // 2. Habilitar DbTool
        workspace = useCases.enableDbTool(workspace, "proj_db_portability").getOrThrow()
        
        // 3. Añadir 2 conexiones
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "SQLite Local",
            type = "sqlite",
            host = "localhost",
            port = 1,
            database = "/path/to/local.db",
            username = "user",
            authType = "none",
            authValueRef = ""
        ).getOrThrow()
        
        val conn1Id = workspace.projects[0].tools.dbTools?.connections?.get(0)?.id!!
        
        workspace = useCases.addConnection(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "SQLite Memory",
            type = "sqlite",
            host = "localhost",
            port = 1,
            database = ":memory:",
            username = "user",
            authType = "none",
            authValueRef = ""
        ).getOrThrow()
        
        val conn2Id = workspace.projects[0].tools.dbTools?.connections?.get(1)?.id!!
        
        // 4. Añadir 3 queries
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "List All Users",
            connectionId = conn1Id,
            sql = "SELECT id, name, email FROM users ORDER BY created_at DESC;"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Count Orders",
            connectionId = conn1Id,
            sql = "SELECT COUNT(*) as total FROM orders WHERE status = 'completed';"
        ).getOrThrow()
        
        workspace = useCases.addSavedQuery(
            workspace = workspace,
            projectId = "proj_db_portability",
            name = "Memory Test",
            connectionId = conn2Id,
            sql = "SELECT 1 AS health;"
        ).getOrThrow()
        
        // 5. Añadir historial de ejecuciones
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = conn1Id,
            sql = "SELECT * FROM users;",
            success = true,
            rowCount = 50,
            executionTimeMs = 120,
            error = null
        ).getOrThrow()
        
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = conn2Id,
            sql = "SELECT 1;",
            success = true,
            rowCount = 1,
            executionTimeMs = 5,
            error = null
        ).getOrThrow()
        
        workspace = useCases.addExecutionToHistory(
            workspace = workspace,
            projectId = "proj_db_portability",
            connectionId = conn1Id,
            sql = "SELECT * FROM invalid_table;",
            success = false,
            rowCount = 0,
            executionTimeMs = 10,
            error = "Error SQLite: no such table: invalid_table"
        ).getOrThrow()
        
        // 6. Guardar
        repository.save(workspacePath, workspace)
        
        // 7. Recargar (simula copiar JSON a otro sistema)
        val reloadedWorkspace = repository.load(workspacePath)
        
        // 8. Validar TODO
        val originalDb = workspace.projects[0].tools.dbTools!!
        val reloadedDb = reloadedWorkspace.projects[0].tools.dbTools!!
        
        // Configuración
        assertEquals(originalDb.enabled, reloadedDb.enabled)
        assertTrue(reloadedDb.enabled)
        
        // Conexiones
        assertEquals(originalDb.connections.size, reloadedDb.connections.size)
        assertEquals(2, reloadedDb.connections.size)
        assertEquals("SQLite Local", reloadedDb.connections[0].name)
        assertEquals("SQLite Memory", reloadedDb.connections[1].name)
        assertEquals("/path/to/local.db", reloadedDb.connections[0].database)
        assertEquals(":memory:", reloadedDb.connections[1].database)
        
        // Queries
        assertEquals(originalDb.savedQueries.size, reloadedDb.savedQueries.size)
        assertEquals(3, reloadedDb.savedQueries.size)
        assertEquals("List All Users", reloadedDb.savedQueries[0].name)
        assertEquals("Count Orders", reloadedDb.savedQueries[1].name)
        assertEquals("Memory Test", reloadedDb.savedQueries[2].name)
        assertTrue(reloadedDb.savedQueries[0].sql.contains("ORDER BY created_at DESC"))
        assertTrue(reloadedDb.savedQueries[1].sql.contains("WHERE status = 'completed'"))
        
        // Historial
        assertEquals(originalDb.executionHistory.size, reloadedDb.executionHistory.size)
        assertEquals(3, reloadedDb.executionHistory.size)
        
        // Primera ejecución (exitosa, conn1)
        assertEquals(conn1Id, reloadedDb.executionHistory[0].connectionId)
        assertTrue(reloadedDb.executionHistory[0].success)
        assertEquals(50, reloadedDb.executionHistory[0].rowCount)
        assertEquals(120, reloadedDb.executionHistory[0].executionTimeMs)
        
        // Segunda ejecución (exitosa, conn2)
        assertEquals(conn2Id, reloadedDb.executionHistory[1].connectionId)
        assertTrue(reloadedDb.executionHistory[1].success)
        assertEquals("SELECT 1;", reloadedDb.executionHistory[1].sql)
        
        // Tercera ejecución (error, conn1)
        assertEquals(conn1Id, reloadedDb.executionHistory[2].connectionId)
        assertTrue(!reloadedDb.executionHistory[2].success)
        assertEquals("Error SQLite: no such table: invalid_table", reloadedDb.executionHistory[2].error)
        
        // Validar integridad referencial
        reloadedDb.savedQueries.forEach { query ->
            val connectionExists = reloadedDb.connections.any { it.id == query.connectionId }
            assertTrue(connectionExists, "Query '${query.name}' references non-existent connection")
        }
        
        reloadedDb.executionHistory.forEach { execution ->
            val connectionExists = reloadedDb.connections.any { it.id == execution.connectionId }
            assertTrue(connectionExists, "Execution references non-existent connection")
        }
    }
}

