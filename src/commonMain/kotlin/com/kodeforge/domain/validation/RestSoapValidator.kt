package com.kodeforge.domain.validation

/**
 * Validador para herramienta REST/SOAP.
 */
object RestSoapValidator {
    
    sealed class ValidationError(val message: String) {
        object MethodEmpty : ValidationError("El método HTTP no puede estar vacío")
        object MethodInvalid : ValidationError("Método HTTP inválido (debe ser GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)")
        object UrlEmpty : ValidationError("La URL no puede estar vacía")
        object UrlInvalid : ValidationError("URL inválida (debe empezar con http:// o https://)")
        object PathEmpty : ValidationError("El path no puede estar vacío")
        object PathInvalid : ValidationError("El path debe empezar con /")
        object PortInvalid : ValidationError("El puerto debe estar entre 1 y 65535")
        object ModeInvalid : ValidationError("Modo debe ser 'catchAll' o 'defined'")
        object StatusInvalid : ValidationError("El status HTTP debe estar entre 100 y 599")
        object TypeInvalid : ValidationError("El tipo debe ser 'REST' o 'SOAP'")
    }
    
    private val validMethods = setOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    private val validModes = setOf("catchAll", "defined")
    private val validTypes = setOf("REST", "SOAP")
    
    /**
     * Valida el método HTTP.
     */
    fun validateMethod(method: String): Result<Unit> {
        if (method.isBlank()) {
            return Result.failure(Exception(ValidationError.MethodEmpty.message))
        }
        
        if (method.uppercase() !in validMethods) {
            return Result.failure(Exception(ValidationError.MethodInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida la URL.
     */
    fun validateUrl(url: String): Result<Unit> {
        if (url.isBlank()) {
            return Result.failure(Exception(ValidationError.UrlEmpty.message))
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return Result.failure(Exception(ValidationError.UrlInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida el path (para rutas del mock server).
     */
    fun validatePath(path: String): Result<Unit> {
        if (path.isBlank()) {
            return Result.failure(Exception(ValidationError.PathEmpty.message))
        }
        
        if (!path.startsWith("/")) {
            return Result.failure(Exception(ValidationError.PathInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida el puerto.
     */
    fun validatePort(port: Int): Result<Unit> {
        if (port < 1 || port > 65535) {
            return Result.failure(Exception(ValidationError.PortInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida el modo del mock server.
     */
    fun validateMode(mode: String): Result<Unit> {
        if (mode !in validModes) {
            return Result.failure(Exception(ValidationError.ModeInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida el status HTTP.
     */
    fun validateStatus(status: Int): Result<Unit> {
        if (status < 100 || status > 599) {
            return Result.failure(Exception(ValidationError.StatusInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida el tipo de request (REST o SOAP).
     */
    fun validateType(type: String): Result<Unit> {
        if (type !in validTypes) {
            return Result.failure(Exception(ValidationError.TypeInvalid.message))
        }
        
        return Result.success(Unit)
    }
}

