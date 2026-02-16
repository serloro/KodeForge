package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.PersonMeta
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.validation.PersonValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para CRUD de Personas según T3.
 * 
 * Responsabilidades:
 * - Validar datos de entrada
 * - Generar IDs únicos
 * - Crear/actualizar/eliminar personas
 * - Actualizar workspace
 */
class PersonUseCases {
    
    /**
     * Crea una nueva persona.
     * 
     * @return Result con el workspace actualizado o error de validación
     */
    fun createPerson(
        workspace: Workspace,
        displayName: String,
        hoursPerDay: Double,
        role: String? = null,
        avatar: String? = null,
        tags: List<String> = emptyList(),
        active: Boolean = true
    ): Result<Workspace> {
        // Validar datos
        val validationResult = PersonValidator.validateCreate(
            displayName = displayName,
            hoursPerDay = hoursPerDay,
            role = role,
            tags = tags
        )
        
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        
        // Generar ID único
        val id = generatePersonId(workspace.people)
        
        // Generar timestamp actual (ISO 8601)
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        val createdAt = "${localDateTime.date}T${localDateTime.time}Z"
        
        // Crear persona
        val newPerson = Person(
            id = id,
            displayName = displayName.trim(),
            avatar = avatar,
            role = role?.trim()?.takeIf { it.isNotEmpty() },
            hoursPerDay = hoursPerDay,
            active = active,
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
            meta = PersonMeta(createdAt = createdAt)
        )
        
        // Actualizar workspace
        val updatedWorkspace = workspace.copy(
            people = workspace.people + newPerson
        )
        
        return Result.success(updatedWorkspace)
    }
    
    /**
     * Actualiza una persona existente.
     * Solo actualiza los campos que no son null.
     * 
     * @return Result con el workspace actualizado o error
     */
    fun updatePerson(
        workspace: Workspace,
        personId: String,
        displayName: String? = null,
        hoursPerDay: Double? = null,
        role: String? = null,
        avatar: String? = null,
        tags: List<String>? = null,
        active: Boolean? = null
    ): Result<Workspace> {
        // Buscar persona existente
        val existingPerson = workspace.people.find { it.id == personId }
            ?: return Result.failure(Exception("Persona no encontrada"))
        
        // Validar datos a actualizar
        val validationResult = PersonValidator.validateUpdate(
            displayName = displayName,
            hoursPerDay = hoursPerDay,
            role = role,
            tags = tags
        )
        
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        
        // Actualizar persona (solo campos modificados)
        val updatedPerson = existingPerson.copy(
            displayName = displayName?.trim() ?: existingPerson.displayName,
            hoursPerDay = hoursPerDay ?: existingPerson.hoursPerDay,
            role = role?.trim()?.takeIf { it.isNotEmpty() } ?: existingPerson.role,
            avatar = avatar ?: existingPerson.avatar,
            tags = tags?.map { it.trim() }?.filter { it.isNotEmpty() } ?: existingPerson.tags,
            active = active ?: existingPerson.active
        )
        
        // Actualizar workspace
        val updatedPeople = workspace.people.map { 
            if (it.id == personId) updatedPerson else it 
        }
        val updatedWorkspace = workspace.copy(people = updatedPeople)
        
        return Result.success(updatedWorkspace)
    }
    
    /**
     * Elimina una persona.
     * 
     * Nota: En T5 se deberá verificar si tiene tareas asignadas.
     * Por ahora, elimina directamente.
     * 
     * @return Result con el workspace actualizado o error
     */
    fun deletePerson(
        workspace: Workspace,
        personId: String
    ): Result<Workspace> {
        // Buscar persona existente
        val existingPerson = workspace.people.find { it.id == personId }
            ?: return Result.failure(Exception("Persona no encontrada"))
        
        // Verificar si tiene tareas asignadas (warning, no bloquea en T3)
        val hasTasks = workspace.tasks.any { it.assigneeId == personId }
        if (hasTasks) {
            // En T5 se podría mostrar confirmación o reasignar tareas
            println("⚠️ Warning: La persona '${existingPerson.displayName}' tiene tareas asignadas")
        }
        
        // Eliminar persona
        val updatedPeople = workspace.people.filter { it.id != personId }
        val updatedWorkspace = workspace.copy(people = updatedPeople)
        
        return Result.success(updatedWorkspace)
    }
    
    /**
     * Busca personas por nombre, rol o tags.
     */
    fun searchPeople(
        workspace: Workspace,
        query: String
    ): List<Person> {
        if (query.isBlank()) {
            return workspace.people
        }
        
        val lowerQuery = query.trim().lowercase()
        return workspace.people.filter { person ->
            person.displayName.lowercase().contains(lowerQuery) ||
            person.role?.lowercase()?.contains(lowerQuery) == true ||
            person.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }
    
    /**
     * Genera un ID único para una persona.
     * Formato: person_{timestamp}_{random}
     */
    private fun generatePersonId(existingPeople: List<Person>): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        val id = "person_${timestamp}_$random"
        
        // Verificar unicidad (muy improbable colisión, pero por seguridad)
        return if (existingPeople.any { it.id == id }) {
            generatePersonId(existingPeople)
        } else {
            id
        }
    }
}

