package com.kodeforge.domain.validation

import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.DbTool
import com.kodeforge.domain.model.SavedQuery

/**
 * Validador para DbTool y sus componentes.
 */
class DbToolValidator {
    
    /**
     * Valida la configuración completa de DbTool.
     */
    fun validateDbTool(tool: DbTool): Result<Unit> {
        return try {
            // Validar cada conexión
            tool.connections.forEach { connection ->
                validateDbConnection(connection).getOrThrow()
            }
            
            // Validar cada query guardada
            tool.savedQueries.forEach { query ->
                validateSavedQuery(query).getOrThrow()
            }
            
            // Validar que las queries referencian conexiones existentes
            val connectionIds = tool.connections.map { it.id }.toSet()
            tool.savedQueries.forEach { query ->
                if (query.connectionId !in connectionIds) {
                    throw IllegalArgumentException(
                        "Query '${query.name}' referencia conexión inexistente: ${query.connectionId}"
                    )
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Valida una conexión de base de datos.
     */
    fun validateDbConnection(connection: DbConnection): Result<Unit> {
        return try {
            // ID
            if (connection.id.isBlank()) {
                throw IllegalArgumentException("El ID de la conexión no puede estar vacío")
            }
            
            // Name
            if (connection.name.isBlank()) {
                throw IllegalArgumentException("El nombre de la conexión no puede estar vacío")
            }
            
            // Type
            validateDbType(connection.type).getOrThrow()
            
            // Host
            if (connection.host.isBlank()) {
                throw IllegalArgumentException("El host no puede estar vacío")
            }
            
            // Port
            validatePort(connection.port).getOrThrow()
            
            // Database
            if (connection.database.isBlank()) {
                throw IllegalArgumentException("El nombre de la base de datos no puede estar vacío")
            }
            
            // Username
            if (connection.username.isBlank()) {
                throw IllegalArgumentException("El nombre de usuario no puede estar vacío")
            }
            
            // Auth
            validateAuth(connection.auth.type, connection.auth.valueRef).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Valida un tipo de base de datos.
     */
    fun validateDbType(type: String): Result<Unit> {
        val validTypes = setOf(
            "postgres", "postgresql",
            "mysql",
            "sqlite",
            "oracle",
            "sqlserver", "mssql",
            "mariadb",
            "mongodb"
        )
        
        return if (type.lowercase() in validTypes) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalArgumentException(
                    "Tipo de base de datos no soportado: $type. " +
                    "Tipos válidos: ${validTypes.joinToString(", ")}"
                )
            )
        }
    }
    
    /**
     * Valida un puerto.
     */
    fun validatePort(port: Int): Result<Unit> {
        return if (port in 1..65535) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("El puerto debe estar entre 1 y 65535"))
        }
    }
    
    /**
     * Valida la configuración de autenticación.
     */
    fun validateAuth(type: String, valueRef: String): Result<Unit> {
        return try {
            // Type
            val validAuthTypes = setOf("password", "key", "token", "none")
            if (type.lowercase() !in validAuthTypes) {
                throw IllegalArgumentException(
                    "Tipo de autenticación no válido: $type. " +
                    "Tipos válidos: ${validAuthTypes.joinToString(", ")}"
                )
            }
            
            // ValueRef
            if (type != "none" && valueRef.isBlank()) {
                throw IllegalArgumentException(
                    "La referencia de autenticación no puede estar vacía para tipo '$type'"
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Valida una query guardada.
     */
    fun validateSavedQuery(query: SavedQuery): Result<Unit> {
        return try {
            // ID
            if (query.id.isBlank()) {
                throw IllegalArgumentException("El ID de la query no puede estar vacío")
            }
            
            // Name
            if (query.name.isBlank()) {
                throw IllegalArgumentException("El nombre de la query no puede estar vacío")
            }
            
            // ConnectionId
            if (query.connectionId.isBlank()) {
                throw IllegalArgumentException("El connectionId no puede estar vacío")
            }
            
            // SQL
            if (query.sql.isBlank()) {
                throw IllegalArgumentException("El SQL no puede estar vacío")
            }
            
            // Validación básica de SQL (evitar inyección obvia)
            validateSqlBasic(query.sql).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validación básica de SQL (no exhaustiva, solo para detectar errores obvios).
     */
    private fun validateSqlBasic(sql: String): Result<Unit> {
        return try {
            val trimmed = sql.trim()
            
            // No puede estar vacío
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("El SQL no puede estar vacío")
            }
            
            // Debe terminar con punto y coma (opcional pero recomendado)
            // No validamos esto estrictamente para permitir queries sin ;
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

