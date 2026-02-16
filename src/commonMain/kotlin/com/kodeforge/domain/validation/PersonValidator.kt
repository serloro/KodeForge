package com.kodeforge.domain.validation

/**
 * Validador para Person según spec.md.
 * 
 * Reglas:
 * - displayName: obligatorio, no vacío, max 100 chars
 * - hoursPerDay: obligatorio, > 0, <= 24
 * - role: opcional, max 50 chars
 * - tags: max 20 tags, cada uno max 30 chars
 */
object PersonValidator {
    
    sealed class ValidationError(val message: String) {
        object NameEmpty : ValidationError("El nombre es obligatorio")
        object NameTooLong : ValidationError("Nombre muy largo (máximo 100 caracteres)")
        object HoursInvalid : ValidationError("Horas por día debe ser mayor a 0")
        object HoursTooMany : ValidationError("Máximo 24 horas por día")
        object RoleTooLong : ValidationError("Rol muy largo (máximo 50 caracteres)")
        object TooManyTags : ValidationError("Máximo 20 tags")
        data class TagTooLong(val tag: String) : ValidationError("Tag '$tag' muy largo (máximo 30 caracteres)")
    }
    
    /**
     * Valida los datos para crear una persona.
     */
    fun validateCreate(
        displayName: String,
        hoursPerDay: Double,
        role: String? = null,
        tags: List<String> = emptyList()
    ): Result<Unit> {
        // Validar displayName
        val trimmedName = displayName.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception(ValidationError.NameEmpty.message))
        }
        if (trimmedName.length > 100) {
            return Result.failure(Exception(ValidationError.NameTooLong.message))
        }
        
        // Validar hoursPerDay
        if (hoursPerDay <= 0 || hoursPerDay.isNaN() || hoursPerDay.isInfinite()) {
            return Result.failure(Exception(ValidationError.HoursInvalid.message))
        }
        if (hoursPerDay > 24) {
            return Result.failure(Exception(ValidationError.HoursTooMany.message))
        }
        
        // Validar role
        role?.let {
            if (it.length > 50) {
                return Result.failure(Exception(ValidationError.RoleTooLong.message))
            }
        }
        
        // Validar tags
        if (tags.size > 20) {
            return Result.failure(Exception(ValidationError.TooManyTags.message))
        }
        tags.forEach { tag ->
            if (tag.length > 30) {
                return Result.failure(Exception(ValidationError.TagTooLong(tag).message))
            }
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida los datos para actualizar una persona.
     * Solo valida los campos que no son null (campos a actualizar).
     */
    fun validateUpdate(
        displayName: String? = null,
        hoursPerDay: Double? = null,
        role: String? = null,
        tags: List<String>? = null
    ): Result<Unit> {
        // Validar displayName si se proporciona
        displayName?.let {
            val trimmedName = it.trim()
            if (trimmedName.isEmpty()) {
                return Result.failure(Exception(ValidationError.NameEmpty.message))
            }
            if (trimmedName.length > 100) {
                return Result.failure(Exception(ValidationError.NameTooLong.message))
            }
        }
        
        // Validar hoursPerDay si se proporciona
        hoursPerDay?.let {
            if (it <= 0 || it.isNaN() || it.isInfinite()) {
                return Result.failure(Exception(ValidationError.HoursInvalid.message))
            }
            if (it > 24) {
                return Result.failure(Exception(ValidationError.HoursTooMany.message))
            }
        }
        
        // Validar role si se proporciona
        role?.let {
            if (it.isNotEmpty() && it.length > 50) {
                return Result.failure(Exception(ValidationError.RoleTooLong.message))
            }
        }
        
        // Validar tags si se proporciona
        tags?.let {
            if (it.size > 20) {
                return Result.failure(Exception(ValidationError.TooManyTags.message))
            }
            it.forEach { tag ->
                if (tag.length > 30) {
                    return Result.failure(Exception(ValidationError.TagTooLong(tag).message))
                }
            }
        }
        
        return Result.success(Unit)
    }
}

