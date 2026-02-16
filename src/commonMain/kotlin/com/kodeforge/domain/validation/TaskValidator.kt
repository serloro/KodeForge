package com.kodeforge.domain.validation

import com.kodeforge.domain.model.Workspace

/**
 * Validador para Task según spec.md.
 * 
 * Reglas:
 * - title: obligatorio, no vacío, max 200 chars
 * - costHours: obligatorio si hay assigneeId, > 0, max 1000
 * - projectId: obligatorio, debe existir en workspace
 * - assigneeId: si existe, persona debe estar activa
 * - status: valores válidos (todo, in_progress, completed)
 * - priority: >= 0
 */
object TaskValidator {
    
    sealed class ValidationError(val message: String) {
        object TitleEmpty : ValidationError("El título es obligatorio")
        object TitleTooLong : ValidationError("Título muy largo (máximo 200 caracteres)")
        object CostHoursInvalid : ValidationError("Costo en horas debe ser mayor a 0")
        object CostHoursTooHigh : ValidationError("Costo en horas excesivo (máximo 1000h)")
        object CostHoursRequiredForAssignment : ValidationError("costHours es obligatorio si se asigna una persona")
        object ProjectNotFound : ValidationError("Proyecto no encontrado")
        object PersonNotFound : ValidationError("Persona no encontrada")
        object PersonInactive : ValidationError("La persona está inactiva")
        object StatusInvalid : ValidationError("Estado inválido (debe ser: todo, in_progress, completed)")
        object PriorityInvalid : ValidationError("Prioridad debe ser mayor o igual a 0")
    }
    
    private val validStatuses = setOf("todo", "in_progress", "completed")
    
    /**
     * Valida los datos para crear una tarea.
     */
    fun validateCreate(
        workspace: Workspace,
        title: String,
        costHours: Double,
        projectId: String,
        status: String = "todo",
        priority: Int = 0,
        assigneeId: String? = null
    ): Result<Unit> {
        // Validar title
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(Exception(ValidationError.TitleEmpty.message))
        }
        if (trimmedTitle.length > 200) {
            return Result.failure(Exception(ValidationError.TitleTooLong.message))
        }
        
        // Validar costHours
        if (costHours <= 0 || costHours.isNaN() || costHours.isInfinite()) {
            return Result.failure(Exception(ValidationError.CostHoursInvalid.message))
        }
        if (costHours > 1000) {
            return Result.failure(Exception(ValidationError.CostHoursTooHigh.message))
        }
        
        // Validar projectId
        if (!workspace.projects.any { it.id == projectId }) {
            return Result.failure(Exception(ValidationError.ProjectNotFound.message))
        }
        
        // Validar status
        if (status !in validStatuses) {
            return Result.failure(Exception(ValidationError.StatusInvalid.message))
        }
        
        // Validar priority
        if (priority < 0) {
            return Result.failure(Exception(ValidationError.PriorityInvalid.message))
        }
        
        // Validar assigneeId si existe
        assigneeId?.let {
            return validateAssignment(workspace, it, costHours)
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida los datos para actualizar una tarea.
     */
    fun validateUpdate(
        workspace: Workspace,
        title: String? = null,
        costHours: Double? = null,
        status: String? = null,
        priority: Int? = null,
        assigneeId: String? = null
    ): Result<Unit> {
        // Validar title si se proporciona
        title?.let {
            val trimmedTitle = it.trim()
            if (trimmedTitle.isEmpty()) {
                return Result.failure(Exception(ValidationError.TitleEmpty.message))
            }
            if (trimmedTitle.length > 200) {
                return Result.failure(Exception(ValidationError.TitleTooLong.message))
            }
        }
        
        // Validar costHours si se proporciona
        costHours?.let {
            if (it <= 0 || it.isNaN() || it.isInfinite()) {
                return Result.failure(Exception(ValidationError.CostHoursInvalid.message))
            }
            if (it > 1000) {
                return Result.failure(Exception(ValidationError.CostHoursTooHigh.message))
            }
        }
        
        // Validar status si se proporciona
        status?.let {
            if (it !in validStatuses) {
                return Result.failure(Exception(ValidationError.StatusInvalid.message))
            }
        }
        
        // Validar priority si se proporciona
        priority?.let {
            if (it < 0) {
                return Result.failure(Exception(ValidationError.PriorityInvalid.message))
            }
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Valida la asignación de una tarea a una persona.
     */
    fun validateAssignment(
        workspace: Workspace,
        personId: String,
        costHours: Double
    ): Result<Unit> {
        // Verificar que la persona existe
        val person = workspace.people.find { it.id == personId }
            ?: return Result.failure(Exception(ValidationError.PersonNotFound.message))
        
        // Verificar que la persona está activa
        if (!person.active) {
            return Result.failure(Exception(ValidationError.PersonInactive.message))
        }
        
        // Verificar que costHours > 0 (obligatorio para asignación según spec)
        if (costHours <= 0) {
            return Result.failure(Exception(ValidationError.CostHoursRequiredForAssignment.message))
        }
        
        return Result.success(Unit)
    }
}

