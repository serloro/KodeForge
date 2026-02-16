package com.kodeforge.database

import com.kodeforge.domain.model.DbConnection

/**
 * Resultado de la ejecución de una query.
 */
data class QueryResult(
    val success: Boolean,
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val rowCount: Int = 0,
    val executionTimeMs: Long = 0,
    val error: String? = null
)

/**
 * Interfaz para ejecutar queries en bases de datos.
 * 
 * Implementación multiplataforma con expect/actual:
 * - JVM: Soporte real para SQLite (y potencialmente otros)
 * - Otros: Placeholder o no soportado
 */
expect class QueryExecutor() {
    
    /**
     * Ejecuta una query SQL en una conexión.
     * 
     * @param connection Conexión a la base de datos
     * @param sql Query SQL a ejecutar
     * @return Resultado de la ejecución
     */
    suspend fun execute(connection: DbConnection, sql: String): QueryResult
    
    /**
     * Verifica si un tipo de base de datos está soportado en esta plataforma.
     * 
     * @param dbType Tipo de BD (sqlite, postgres, mysql, etc.)
     * @return true si está soportado, false en caso contrario
     */
    fun isSupported(dbType: String): Boolean
}

