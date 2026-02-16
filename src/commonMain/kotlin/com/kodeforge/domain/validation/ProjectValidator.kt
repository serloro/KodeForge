package com.kodeforge.domain.validation

import com.kodeforge.domain.model.Project

/**
 * Validador para Project.
 * 
 * Reglas:
 * - name: obligatorio, max 100 chars
 * - description: opcional, max 500 chars
 * - status: debe ser "active", "paused" o "completed"
 * - members: IDs deben existir en workspace (validado en UseCase)
 */
class ProjectValidator {
    
    fun validate(project: Project): List<String> {
        val errors = mutableListOf<String>()
        
        // Validar nombre
        if (project.name.isBlank()) {
            errors.add("El nombre del proyecto es obligatorio.")
        }
        if (project.name.length > 100) {
            errors.add("El nombre del proyecto es demasiado largo (máximo 100 caracteres).")
        }
        
        // Validar descripción
        if (project.description != null && project.description.length > 500) {
            errors.add("La descripción es demasiado larga (máximo 500 caracteres).")
        }
        
        // Validar status
        val validStatuses = listOf("active", "paused", "completed")
        if (project.status !in validStatuses) {
            errors.add("El estado del proyecto no es válido. Debe ser 'active', 'paused' o 'completed'.")
        }
        
        return errors
    }
}

