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
 * 
 * Futuro:
 * - PostgreSQL (requiere driver)
 * - MySQL (requiere driver)
 * - etc.
 */
actual class QueryExecutor {
    
    /**
     * Ejecuta una query SQL.
     */
    actual suspend fun execute(connection: DbConnection, sql: String): QueryResult {
        return try {
            when (connection.type.lowercase()) {
                "sqlite" -> executeSqlite(connection, sql)
                else -> QueryResult(
                    success = false,
                    error = "Tipo de base de datos '${connection.type}' no soportado en este target aún.\n" +
                            "Actualmente solo SQLite está soportado.\n" +
                            "Puedes guardar la conexión y queries para uso futuro."
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
            else -> false
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

