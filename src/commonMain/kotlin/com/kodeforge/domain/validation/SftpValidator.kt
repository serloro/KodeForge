package com.kodeforge.domain.validation

import com.kodeforge.domain.model.SftpConnection
import com.kodeforge.domain.model.SftpTool

/**
 * Validador para SftpTool y sus conexiones.
 */
object SftpValidator {
    
    /**
     * Valida una conexión SFTP.
     */
    fun validateConnection(connection: SftpConnection): Result<Unit> {
        // Nombre no puede estar vacío
        if (connection.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Connection name cannot be blank"))
        }
        
        // Host no puede estar vacío
        if (connection.host.isBlank()) {
            return Result.failure(IllegalArgumentException("Host cannot be blank"))
        }
        
        // Puerto debe estar en rango válido (1-65535)
        if (connection.port !in 1..65535) {
            return Result.failure(IllegalArgumentException("Port must be between 1 and 65535"))
        }
        
        // Username no puede estar vacío
        if (connection.username.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be blank"))
        }
        
        // Auth type debe ser "password" o "key"
        if (connection.auth.type !in listOf("password", "key", "none")) {
            return Result.failure(IllegalArgumentException("Auth type must be 'password', 'key', or 'none'"))
        }
        
        // Si auth type no es "none", valueRef no puede estar vacío
        if (connection.auth.type != "none" && connection.auth.valueRef.isBlank()) {
            return Result.failure(IllegalArgumentException("Auth valueRef cannot be blank when auth type is not 'none'"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida que no haya nombres duplicados en las conexiones.
     */
    fun validateUniqueNames(connections: List<SftpConnection>): Result<Unit> {
        val names = connections.map { it.name }
        val duplicates = names.groupingBy { it }.eachCount().filter { it.value > 1 }
        
        if (duplicates.isNotEmpty()) {
            return Result.failure(
                IllegalArgumentException("Duplicate connection names: ${duplicates.keys.joinToString()}")
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida que no haya IDs duplicados en las conexiones.
     */
    fun validateUniqueIds(connections: List<SftpConnection>): Result<Unit> {
        val ids = connections.map { it.id }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
        
        if (duplicates.isNotEmpty()) {
            return Result.failure(
                IllegalArgumentException("Duplicate connection IDs: ${duplicates.keys.joinToString()}")
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida todo el SftpTool.
     */
    fun validateTool(tool: SftpTool): Result<Unit> {
        // Validar cada conexión
        tool.connections.forEach { connection ->
            validateConnection(connection).getOrElse { return Result.failure(it) }
        }
        
        // Validar unicidad de nombres
        validateUniqueNames(tool.connections).getOrElse { return Result.failure(it) }
        
        // Validar unicidad de IDs
        validateUniqueIds(tool.connections).getOrElse { return Result.failure(it) }
        
        return Result.success(Unit)
    }
}

