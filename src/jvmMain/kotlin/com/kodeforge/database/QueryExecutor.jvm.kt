package com.kodeforge.database

import com.kodeforge.domain.model.DbConnection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.system.measureTimeMillis

/**
 * Implementación JVM del QueryExecutor.
 * 
 * Soporta:
 * - SQLite (nativo, sin drivers externos)
 * - PostgreSQL (requiere driver JDBC)
 * - MySQL (requiere driver JDBC)
 * - MariaDB (requiere driver JDBC)
 * - SQL Server (requiere driver JDBC)
 * - Oracle (requiere driver JDBC)
 * - MongoDB (requiere driver específico)
 */
actual class QueryExecutor {
    
    /**
     * Ejecuta una query SQL.
     */
    actual suspend fun execute(connection: DbConnection, sql: String): QueryResult {
        return try {
            when (connection.type.lowercase()) {
                "sqlite" -> executeSqlite(connection, sql)
                "postgres", "postgresql" -> executeJdbc(connection, sql)
                "mysql" -> executeJdbc(connection, sql)
                "mariadb" -> executeJdbc(connection, sql)
                "sqlserver", "mssql" -> executeJdbc(connection, sql)
                "oracle" -> executeJdbc(connection, sql)
                "mongodb" -> QueryResult(
                    success = false,
                    error = "MongoDB requiere un driver específico que no está incluido.\n" +
                            "Considera usar las herramientas nativas de MongoDB."
                )
                else -> QueryResult(
                    success = false,
                    error = "Tipo de base de datos '${connection.type}' no reconocido.\n" +
                            "Tipos soportados: PostgreSQL, MySQL, MariaDB, SQL Server, Oracle, SQLite"
                )
            }
        } catch (e: Exception) {
            QueryResult(
                success = false,
                error = "Error al ejecutar query: ${e.message}\n${e.stackTraceToString().take(500)}"
            )
        }
    }
    
    /**
     * Verifica si un tipo de BD está soportado.
     */
    actual fun isSupported(dbType: String): Boolean {
        return when (dbType.lowercase()) {
            "sqlite" -> true
            "postgres", "postgresql" -> true
            "mysql" -> true
            "mariadb" -> true
            "sqlserver", "mssql" -> true
            "oracle" -> true
            else -> false
        }
    }
    
    /**
     * Ejecuta una query usando JDBC genérico (PostgreSQL, MySQL, etc.)
     */
    private fun executeJdbc(connection: DbConnection, sql: String): QueryResult {
        var executionTime = 0L
        
        return try {
            // Construir URL JDBC
            val jdbcUrl = buildJdbcUrl(connection)
            
            // Cargar driver apropiado
            loadDriver(connection.type)
            
            // Obtener credenciales
            val password = extractPassword(connection.auth.valueRef)
            
            // Conectar y ejecutar
            DriverManager.getConnection(jdbcUrl, connection.username, password).use { conn ->
                conn.createStatement().use { statement ->
                    
                    // Determinar si es una query SELECT o un comando
                    val trimmedSql = sql.trim()
                    val isQuery = trimmedSql.startsWith("SELECT", ignoreCase = true) ||
                                  trimmedSql.startsWith("WITH", ignoreCase = true) ||
                                  trimmedSql.startsWith("SHOW", ignoreCase = true) ||
                                  trimmedSql.startsWith("DESCRIBE", ignoreCase = true) ||
                                  trimmedSql.startsWith("DESC", ignoreCase = true)
                    
                    if (isQuery) {
                        // Es una SELECT, usar executeQuery
                        val resultSet: ResultSet
                        executionTime = measureTimeMillis {
                            resultSet = statement.executeQuery(sql)
                        }
                        parseResultSet(resultSet, executionTime)
                    } else {
                        // Es un comando (CREATE, INSERT, UPDATE, DELETE, etc.)
                        var affectedRows = 0
                        executionTime = measureTimeMillis {
                            statement.execute(sql)
                            affectedRows = statement.updateCount
                        }
                        QueryResult(
                            success = true,
                            rowCount = affectedRows,
                            executionTimeMs = executionTime
                        )
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            QueryResult(
                success = false,
                executionTimeMs = executionTime,
                error = "Driver JDBC no encontrado para ${connection.type}.\n" +
                        "Asegúrate de tener el driver JDBC correspondiente en el classpath.\n" +
                        "Error: ${e.message}"
            )
        } catch (e: Exception) {
            QueryResult(
                success = false,
                executionTimeMs = executionTime,
                error = "Error ${connection.type}: ${e.message}"
            )
        }
    }
    
    /**
     * Ejecuta una query en SQLite.
     */
    private fun executeSqlite(connection: DbConnection, sql: String): QueryResult {
        var executionTime = 0L
        
        return try {
            // Construir URL de conexión SQLite
            val jdbcUrl = buildSqliteUrl(connection)
            
            // Cargar driver SQLite (incluido en JVM)
            Class.forName("org.sqlite.JDBC")
            
            // Conectar y ejecutar
            DriverManager.getConnection(jdbcUrl).use { conn ->
                conn.createStatement().use { statement ->
                    
                    // Determinar si es una query SELECT o un comando (CREATE, INSERT, UPDATE, etc.)
                    val trimmedSql = sql.trim()
                    val isQuery = trimmedSql.startsWith("SELECT", ignoreCase = true) ||
                                  trimmedSql.startsWith("WITH", ignoreCase = true)
                    
                    if (isQuery) {
                        // Es una SELECT, usar executeQuery
                        val resultSet: ResultSet
                        executionTime = measureTimeMillis {
                            resultSet = statement.executeQuery(sql)
                        }
                        parseResultSet(resultSet, executionTime)
                    } else {
                        // Es un comando (CREATE, INSERT, UPDATE, DELETE, etc.), usar execute
                        var affectedRows = 0
                        executionTime = measureTimeMillis {
                            statement.execute(sql)
                            affectedRows = statement.updateCount
                        }
                        QueryResult(
                            success = true,
                            rowCount = affectedRows,
                            executionTimeMs = executionTime
                        )
                    }
                }
            }
        } catch (e: Exception) {
            QueryResult(
                success = false,
                executionTimeMs = executionTime,
                error = "Error SQLite: ${e.message}"
            )
        }
    }
    
    /**
     * Construye la URL JDBC para SQLite.
     */
    private fun buildSqliteUrl(connection: DbConnection): String {
        // Para SQLite, el "database" es la ruta al archivo
        // Si es ":memory:", usar base de datos en memoria
        return if (connection.database == ":memory:") {
            "jdbc:sqlite::memory:"
        } else {
            "jdbc:sqlite:${connection.database}"
        }
    }
    
    /**
     * Construye la URL JDBC genérica para otras bases de datos.
     */
    private fun buildJdbcUrl(connection: DbConnection): String {
        return when (connection.type.lowercase()) {
            "postgres", "postgresql" -> 
                "jdbc:postgresql://${connection.host}:${connection.port}/${connection.database}"
            "mysql" -> 
                "jdbc:mysql://${connection.host}:${connection.port}/${connection.database}"
            "mariadb" -> 
                "jdbc:mariadb://${connection.host}:${connection.port}/${connection.database}"
            "sqlserver", "mssql" -> 
                "jdbc:sqlserver://${connection.host}:${connection.port};databaseName=${connection.database}"
            "oracle" -> 
                "jdbc:oracle:thin:@${connection.host}:${connection.port}:${connection.database}"
            else -> throw IllegalArgumentException("Tipo de base de datos no soportado: ${connection.type}")
        }
    }
    
    /**
     * Carga el driver JDBC apropiado.
     */
    private fun loadDriver(dbType: String) {
        val driverClass = when (dbType.lowercase()) {
            "postgres", "postgresql" -> "org.postgresql.Driver"
            "mysql" -> "com.mysql.cj.jdbc.Driver"
            "mariadb" -> "org.mariadb.jdbc.Driver"
            "sqlserver", "mssql" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            "oracle" -> "oracle.jdbc.driver.OracleDriver"
            else -> throw IllegalArgumentException("Driver no disponible para: $dbType")
        }
        
        try {
            Class.forName(driverClass)
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException(
                "Driver JDBC '$driverClass' no encontrado. " +
                "Agrega la dependencia correspondiente al proyecto."
            )
        }
    }
    
    /**
     * Extrae la contraseña del valueRef.
     * Si comienza con "secret:", busca en el sistema de secrets.
     * Si no, asume que es la contraseña en texto plano.
     */
    private fun extractPassword(valueRef: String): String {
        return if (valueRef.startsWith("secret:")) {
            // TODO: Implementar sistema de secrets
            // Por ahora, devolver vacío
            ""
        } else {
            // Es la contraseña directamente
            valueRef
        }
    }
    
    /**
     * Parsea un ResultSet a QueryResult.
     */
    private fun parseResultSet(resultSet: ResultSet, executionTime: Long): QueryResult {
        val metaData = resultSet.metaData
        val columnCount = metaData.columnCount
        
        // Obtener nombres de columnas
        val columns = (1..columnCount).map { metaData.getColumnName(it) }
        
        // Obtener filas
        val rows = mutableListOf<List<String>>()
        var rowCount = 0
        
        while (resultSet.next() && rowCount < MAX_ROWS) {
            val row = (1..columnCount).map { index ->
                resultSet.getString(index) ?: "NULL"
            }
            rows.add(row)
            rowCount++
        }
        
        return QueryResult(
            success = true,
            columns = columns,
            rows = rows,
            rowCount = rowCount,
            executionTimeMs = executionTime
        )
    }
    
    companion object {
        // Límite de filas para evitar problemas de memoria
        private const val MAX_ROWS = 1000
    }
}

