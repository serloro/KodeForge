package com.kodeforge.domain.usecases

import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.validation.ProjectValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para gestión de proyectos.
 * 
 * Incluye:
 * - CRUD completo
 * - Gestión de miembros (añadir/quitar personas)
 * - Búsqueda
 */
class ProjectUseCases(
    private val workspaceRepository: WorkspaceRepository,
    private val projectValidator: ProjectValidator
) {
    
    /**
     * Crea un nuevo proyecto.
     */
    suspend fun createProject(
        workspace: Workspace,
        name: String,
        description: String? = null,
        status: String = "active",
        members: List<String> = emptyList()
    ): Pair<Workspace, List<String>> {
        val now = generateTimestamp()
        
        val newProject = Project(
            id = generateProjectId(),
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            status = status,
            members = members,
            createdAt = now,
            updatedAt = now
        )
        
        // Validar proyecto
        val errors = projectValidator.validate(newProject)
        if (errors.isNotEmpty()) {
            return Pair(workspace, errors)
        }
        
        // Validar que los miembros existen
        val memberErrors = validateMembers(workspace, members)
        if (memberErrors.isNotEmpty()) {
            return Pair(workspace, memberErrors)
        }
        
        val updatedProjects = workspace.projects + newProject
        val updatedWorkspace = workspace.copy(projects = updatedProjects)
        
        workspaceRepository.save("workspace.json", updatedWorkspace)
        
        return Pair(updatedWorkspace, emptyList())
    }
    
    /**
     * Actualiza un proyecto existente.
     */
    suspend fun updateProject(
        workspace: Workspace,
        projectId: String,
        name: String,
        description: String? = null,
        status: String = "active",
        members: List<String> = emptyList()
    ): Pair<Workspace, List<String>> {
        val existingProject = workspace.projects.find { it.id == projectId }
            ?: return Pair(workspace, listOf("Proyecto no encontrado."))
        
        val updatedProject = existingProject.copy(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotBlank() },
            status = status,
            members = members,
            updatedAt = generateTimestamp()
        )
        
        // Validar proyecto
        val errors = projectValidator.validate(updatedProject)
        if (errors.isNotEmpty()) {
            return Pair(workspace, errors)
        }
        
        // Validar que los miembros existen
        val memberErrors = validateMembers(workspace, members)
        if (memberErrors.isNotEmpty()) {
            return Pair(workspace, memberErrors)
        }
        
        val updatedProjects = workspace.projects.map {
            if (it.id == projectId) updatedProject else it
        }
        val updatedWorkspace = workspace.copy(projects = updatedProjects)
        
        workspaceRepository.save("workspace.json", updatedWorkspace)
        
        return Pair(updatedWorkspace, emptyList())
    }
    
    /**
     * Elimina un proyecto.
     * 
     * Nota: También debería eliminar las tareas asociadas,
     * pero eso se puede hacer en una versión futura.
     */
    suspend fun deleteProject(workspace: Workspace, projectId: String): Workspace {
        val updatedProjects = workspace.projects.filter { it.id != projectId }
        val updatedWorkspace = workspace.copy(projects = updatedProjects)
        
        workspaceRepository.save("workspace.json", updatedWorkspace)
        
        return updatedWorkspace
    }
    
    /**
     * Añade un miembro al proyecto.
     */
    suspend fun addMember(
        workspace: Workspace,
        projectId: String,
        personId: String
    ): Pair<Workspace, List<String>> {
        val existingProject = workspace.projects.find { it.id == projectId }
            ?: return Pair(workspace, listOf("Proyecto no encontrado."))
        
        // Validar que la persona existe
        val person = workspace.people.find { it.id == personId }
        if (person == null) {
            return Pair(workspace, listOf("Persona no encontrada."))
        }
        
        // Validar que no esté ya en el proyecto
        if (personId in existingProject.members) {
            return Pair(workspace, listOf("La persona ya es miembro del proyecto."))
        }
        
        val updatedMembers = existingProject.members + personId
        val updatedProject = existingProject.copy(
            members = updatedMembers,
            updatedAt = generateTimestamp()
        )
        
        val updatedProjects = workspace.projects.map {
            if (it.id == projectId) updatedProject else it
        }
        val updatedWorkspace = workspace.copy(projects = updatedProjects)
        
        workspaceRepository.save("workspace.json", updatedWorkspace)
        
        return Pair(updatedWorkspace, emptyList())
    }
    
    /**
     * Quita un miembro del proyecto.
     */
    suspend fun removeMember(
        workspace: Workspace,
        projectId: String,
        personId: String
    ): Workspace {
        val existingProject = workspace.projects.find { it.id == projectId }
            ?: return workspace
        
        val updatedMembers = existingProject.members.filter { it != personId }
        val updatedProject = existingProject.copy(
            members = updatedMembers,
            updatedAt = generateTimestamp()
        )
        
        val updatedProjects = workspace.projects.map {
            if (it.id == projectId) updatedProject else it
        }
        val updatedWorkspace = workspace.copy(projects = updatedProjects)
        
        workspaceRepository.save("workspace.json", updatedWorkspace)
        
        return updatedWorkspace
    }
    
    /**
     * Busca proyectos por nombre o descripción.
     */
    fun searchProjects(workspace: Workspace, query: String): List<Project> {
        if (query.isBlank()) {
            return workspace.projects
        }
        
        val lowerCaseQuery = query.lowercase()
        return workspace.projects.filter {
            it.name.lowercase().contains(lowerCaseQuery) ||
                    it.description?.lowercase()?.contains(lowerCaseQuery) == true
        }
    }
    
    /**
     * Valida que todos los miembros existen en el workspace.
     */
    private fun validateMembers(workspace: Workspace, members: List<String>): List<String> {
        val errors = mutableListOf<String>()
        
        members.forEach { personId ->
            val person = workspace.people.find { it.id == personId }
            if (person == null) {
                errors.add("Persona con ID '$personId' no encontrada.")
            }
        }
        
        return errors
    }
    
    /**
     * Genera un ID único para un proyecto.
     */
    private fun generateProjectId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        return "proj_${timestamp}_$random"
    }
    
    /**
     * Genera un timestamp ISO 8601.
     */
    private fun generateTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        return "${localDateTime.date}T${localDateTime.time}Z"
    }
}

