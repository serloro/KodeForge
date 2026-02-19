package com.kodeforge

import com.kodeforge.database.QueryExecutor
import com.kodeforge.domain.model.AuthConfig
import com.kodeforge.domain.model.DbConnection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para QueryExecutor.
 */
class QueryExecutorTest {
    
    @TempDir
    lateinit var tempDir: Path
    
    private fun createTestConnection(dbPath: String = ":memory:"): DbConnection {
        return DbConnection(
            id = "conn_test",
            name = "Test SQLite",
            type = "sqlite",
            host = "localhost",
            port = 1,
            database = dbPath,
            username = "test",
            auth = AuthConfig(type = "none", valueRef = "")
        )
    }
    
    @Test
    fun `execute - simple SELECT query on file-based SQLite`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test.db").toString()
        val connection = createTestConnection(dbFile)
        
        // Crear tabla y insertar datos
        executor.execute(connection, "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT)")
        executor.execute(connection, "INSERT INTO users (id, name) VALUES (1, 'Alice')")
        executor.execute(connection, "INSERT INTO users (id, name) VALUES (2, 'Bob')")
        
        // Ejecutar SELECT
        val result = executor.execute(connection, "SELECT * FROM users")
        
        assertTrue(result.success)
        assertEquals(2, result.columns.size)
        assertEquals("id", result.columns[0])
        assertEquals("name", result.columns[1])
        assertEquals(2, result.rowCount)
        assertEquals(2, result.rows.size)
        assertEquals(listOf("1", "Alice"), result.rows[0])
        assertEquals(listOf("2", "Bob"), result.rows[1])
        assertTrue(result.executionTimeMs >= 0)
    }
    
    @Test
    fun `execute - query with WHERE clause`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test_where.db").toString()
        val connection = createTestConnection(dbFile)
        
        executor.execute(connection, "CREATE TABLE products (id INTEGER, name TEXT, price REAL)")
        executor.execute(connection, "INSERT INTO products VALUES (1, 'Apple', 1.5)")
        executor.execute(connection, "INSERT INTO products VALUES (2, 'Banana', 0.8)")
        executor.execute(connection, "INSERT INTO products VALUES (3, 'Orange', 1.2)")
        
        val result = executor.execute(connection, "SELECT name, price FROM products WHERE price > 1.0")
        
        assertTrue(result.success)
        assertEquals(2, result.rowCount)
        assertEquals("Apple", result.rows[0][0])
        assertEquals("Orange", result.rows[1][0])
    }
    
    @Test
    fun `execute - query with aggregation`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test_agg.db").toString()
        val connection = createTestConnection(dbFile)
        
        executor.execute(connection, "CREATE TABLE sales (product TEXT, amount INTEGER)")
        executor.execute(connection, "INSERT INTO sales VALUES ('Apple', 10)")
        executor.execute(connection, "INSERT INTO sales VALUES ('Apple', 5)")
        executor.execute(connection, "INSERT INTO sales VALUES ('Banana', 8)")
        
        val result = executor.execute(connection, "SELECT product, SUM(amount) as total FROM sales GROUP BY product")
        
        assertTrue(result.success)
        assertEquals(2, result.columns.size)
        assertEquals("product", result.columns[0])
        assertEquals("total", result.columns[1])
        assertEquals(2, result.rowCount)
    }
    
    @Test
    fun `execute - empty result set`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test_empty.db").toString()
        val connection = createTestConnection(dbFile)
        
        executor.execute(connection, "CREATE TABLE empty_table (id INTEGER, name TEXT)")
        
        val result = executor.execute(connection, "SELECT * FROM empty_table")
        
        assertTrue(result.success)
        assertEquals(2, result.columns.size)
        assertEquals(0, result.rowCount)
        assertEquals(0, result.rows.size)
    }
    
    @Test
    fun `execute - query with NULL values`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test_null.db").toString()
        val connection = createTestConnection(dbFile)
        
        executor.execute(connection, "CREATE TABLE nullable_table (id INTEGER, value TEXT)")
        executor.execute(connection, "INSERT INTO nullable_table VALUES (1, 'test')")
        executor.execute(connection, "INSERT INTO nullable_table VALUES (2, NULL)")
        
        val result = executor.execute(connection, "SELECT * FROM nullable_table")
        
        assertTrue(result.success)
        assertEquals(2, result.rowCount)
        assertEquals("test", result.rows[0][1])
        assertEquals("NULL", result.rows[1][1])
    }
    
    @Test
    fun `execute - invalid SQL returns error`() = runBlocking {
        val executor = QueryExecutor()
        val connection = createTestConnection()
        
        val result = executor.execute(connection, "SELECT * FROM nonexistent_table")
        
        assertTrue(result.success == false)
        assertTrue(result.error?.contains("no such table") == true || result.error?.contains("Error") == true)
    }
    
    @Test
    fun `execute - unsupported database type returns error`() = runBlocking {
        val executor = QueryExecutor()
        val connection = DbConnection(
            id = "conn_unknown",
            name = "Unknown DB",
            type = "unknowndb",
            host = "localhost",
            port = 9999,
            database = "testdb",
            username = "user",
            auth = AuthConfig(type = "password", valueRef = "secret:db_001")
        )
        
        val result = executor.execute(connection, "SELECT 1")
        
        assertTrue(result.success == false)
        assertTrue(result.error?.contains("no reconocido") == true || result.error?.contains("no soportado") == true)
    }
    
    @Test
    fun `isSupported - returns true for SQLite`() {
        val executor = QueryExecutor()
        
        assertTrue(executor.isSupported("sqlite"))
        assertTrue(executor.isSupported("SQLite"))
        assertTrue(executor.isSupported("SQLITE"))
    }
    
    @Test
    fun `isSupported - returns true for all supported databases`() {
        val executor = QueryExecutor()
        
        // Todos estos ahora están soportados
        assertTrue(executor.isSupported("postgres"))
        assertTrue(executor.isSupported("postgresql"))
        assertTrue(executor.isSupported("mysql"))
        assertTrue(executor.isSupported("mariadb"))
        assertTrue(executor.isSupported("sqlserver"))
        assertTrue(executor.isSupported("mssql"))
        assertTrue(executor.isSupported("oracle"))
        assertTrue(executor.isSupported("sqlite"))
        
        // MongoDB no está soportado
        assertTrue(!executor.isSupported("mongodb"))
        // Base de datos desconocida
        assertTrue(!executor.isSupported("unknowndb"))
    }
    
    @Test
    fun `execute - complex query with JOIN`() = runBlocking {
        val executor = QueryExecutor()
        val dbFile = tempDir.resolve("test_join.db").toString()
        val connection = createTestConnection(dbFile)
        
        executor.execute(connection, "CREATE TABLE authors (id INTEGER, name TEXT)")
        executor.execute(connection, "CREATE TABLE books (id INTEGER, title TEXT, author_id INTEGER)")
        
        executor.execute(connection, "INSERT INTO authors VALUES (1, 'Alice')")
        executor.execute(connection, "INSERT INTO authors VALUES (2, 'Bob')")
        executor.execute(connection, "INSERT INTO books VALUES (1, 'Book A', 1)")
        executor.execute(connection, "INSERT INTO books VALUES (2, 'Book B', 1)")
        executor.execute(connection, "INSERT INTO books VALUES (3, 'Book C', 2)")
        
        val result = executor.execute(connection, """
            SELECT a.name, COUNT(b.id) as book_count 
            FROM authors a 
            LEFT JOIN books b ON a.id = b.author_id 
            GROUP BY a.name
        """.trimIndent())
        
        assertTrue(result.success)
        assertEquals(2, result.rowCount)
        assertEquals("Alice", result.rows[0][0])
        assertEquals("2", result.rows[0][1])
    }
}

