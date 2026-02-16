package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.Task
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.validation.TaskValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

/**
 * Use cases para CRUD de Tareas (parte inicial de T5).
 * 
 * Responsabilidades:
 * - Validar datos de entrada
 * - Generar IDs únicos y timestamps
 * - Crear/actualizar/eliminar tareas
 * - Asignar/desasignar tareas a personas
 * - Actualizar workspace
 * 
 * Nota: El scheduler (distribución en calendario) se implementará en la siguiente fase.
 */
class TaskUseCases {
    
    /**
     * Crea una nueva tarea.
     * 
     * @param assigneeId Si se proporciona, se asigna la tarea a esa persona
     * @return Result con el workspace actualizado o error de validación
     * 
     * NUEVA VALIDACIÓN (T6B):
     * - Si hay assigneeId, debe ser miembro del proyecto
     * - costHours > 0 obligatorio si hay assigneeId
     */
    fun createTask(
        workspace: Workspace,
        projectId: String,
        title: String,
        costHours: Double,
        description: String? = null,
        status: String = "todo",
        priority: Int = 0,
        assigneeId: String? = null
    ): Result<Workspace> {
        // Validar datos básicos
        val validationResult = TaskValidator.validateCreate(
            workspace = workspace,
            title = title,
            costHours = costHours,
            projectId = projectId,
            status = status,
            priority = priority,
            assigneeId = assigneeId
        )
        
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        
        // Validar asignación con proyecto si hay assigneeId (T6B)
        assigneeId?.let {
            val assignmentValidation = TaskValidator.validateAssignmentInProject(
                workspace = workspace,
                personId = it,
                costHours = costHours,
                projectId = projectId
            )
            
            if (assignmentValidation.isFailure) {
                return Result.failure(assignmentValidation.exceptionOrNull()!!)
            }
        }
        
        // Generar ID único
        val id = generateTaskId(workspace.tasks)
        
        // Generar timestamps (ISO 8601)
        val now = generateTimestamp()
        
        // Crear tarea
        val newTask = Task(
            id = id,
            projectId = projectId,
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            status = status,
            priority = priority,
            costHours = costHours,
            doneHours = 0.0,
            assigneeId = assigneeId,
            createdAt = now,
            updatedAt = now
        )
        
        // Actualizar workspace
        val updatedWorkspace = workspace.copy(
            tasks = workspace.tasks + newTask
        )
        
        return Result.success(updatedWorkspace)
    }
    
    /**
     * Actualiza una tarea existente.
     * Solo actualiza los campos que no son null.
     * 
     * Auto-recalcula el schedule si cambian campos que afectan planning.
     */
    fun updateTask(
        workspace: Workspace,
        taskId: String,
        title: String? = null,
        costHours: Double? = null,
        description: String? = null,
        status: String? = null,
        priority: Int? = null,
        doneHours: Double? = null
    ): Result<Workspace> {
        // Buscar tarea existente
        val existingTask = workspace.tasks.find { it.id == taskId }
            ?: return Result.failure(Exception("Tarea no encontrada"))
        
        // Validar datos a actualizar
        val validationResult = TaskValidator.validateUpdate(
            workspace = workspace,
            title = title,
            costHours = costHours,
            status = status,
            priority = priority
        )
        
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        
        // Actualizar tarea (solo campos modificados)
        val updatedTask = existingTask.copy(
            title = title?.trim() ?: existingTask.title,
            costHours = costHours ?: existingTask.costHours,
            description = description?.trim()?.takeIf { it.isNotEmpty() } ?: existingTask.description,
            status = status ?: existingTask.status,
            priority = priority ?: existingTask.priority,
            doneHours = doneHours ?: existingTask.doneHours,
            updatedAt = generateTimestamp()
        )
        
        // Actualizar workspace
        val updatedTasks = workspace.tasks.map { 
            if (it.id == taskId) updatedTask else it 
        }
        val updatedWorkspace = workspace.copy(tasks = updatedTasks)
        
        // Auto-recalculo si cambió algo que afecta planning
        val needsReschedule = costHours != null || 
                              priority != null || 
                              doneHours != null ||
                              status != null
        
        return if (needsReschedule && existingTask.assigneeId != null) {
            println("🔄 Auto-recalculando schedule (tarea actualizada)...")
            val planningUseCases = PlanningUseCases()
            planningUseCases.generateSchedule(updatedWorkspace)
        } else {
            Result.success(updatedWorkspace)
        }
    }
    
    /**
     * Elimina una tarea.
     * 
     * Auto-recalcula el schedule para limpiar bloques huérfanos.
     */
    fun deleteTask(
        workspace: Workspace,
        taskId: String
    ): Result<Workspace> {
        // Buscar tarea existente
        val existingTask = workspace.tasks.find { it.id == taskId }
            ?: return Result.failure(Exception("Tarea no encontrada"))
        
        // Eliminar tarea
        val updatedTasks = workspace.tasks.filter { it.id != taskId }
        val updatedWorkspace = workspace.copy(tasks = updatedTasks)
        
        // Auto-recalculo para limpiar bloques huérfanos
        return if (existingTask.assigneeId != null) {
            println("🔄 Auto-recalculando schedule (tarea eliminada)...")
            val planningUseCases = PlanningUseCases()
            planningUseCases.generateSchedule(updatedWorkspace)
        } else {
            Result.success(updatedWorkspace)
        }
    }
    
    /**
     * Asigna una tarea a una persona.
     * Según spec: "al asignar tarea → se indica costHours" (obligatorio).
     * 
     * NUEVA VALIDACIÓN (T6B):
     * - La persona debe ser miembro del proyecto
     * - costHours > 0 obligatorio
     */
    fun assignTaskToPerson(
        workspace: Workspace,
        taskId: String,
        personId: String,
        costHours: Double? = null // Si es null, usa el existente de la tarea
    ): Result<Workspace> {
        // Buscar tarea existente
        val existingTask = workspace.tasks.find { it.id == taskId }
            ?: return Result.failure(Exception("Tarea no encontrada"))
        
        // Determinar costHours (usar el proporcionado o el existente)
        val finalCostHours = costHours ?: existingTask.costHours
        
        // Validar asignación CON PROYECTO (T6B)
        val validationResult = TaskValidator.validateAssignmentInProject(
            workspace = workspace,
            personId = personId,
            costHours = finalCostHours,
            projectId = existingTask.projectId
        )
        
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        
        // Actualizar tarea con asignación
        val updatedTask = existingTask.copy(
            assigneeId = personId,
            costHours = finalCostHours,
            updatedAt = generateTimestamp()
        )
        
        // Actualizar workspace
        val updatedTasks = workspace.tasks.map { 
            if (it.id == taskId) updatedTask else it 
        }
        val updatedWorkspace = workspace.copy(tasks = updatedTasks)
        
        // Auto-recalculo para generar bloques de la nueva asignación
        println("🔄 Auto-recalculando schedule (tarea asignada)...")
        val planningUseCases = PlanningUseCases()
        return planningUseCases.generateSchedule(updatedWorkspace)
    }
    
    /**
     * Desasigna una tarea (quita la persona asignada).
     * 
     * Auto-recalcula el schedule para limpiar bloques de esta tarea.
     */
    fun unassignTask(
        workspace: Workspace,
        taskId: String
    ): Result<Workspace> {
        // Buscar tarea existente
        val existingTask = workspace.tasks.find { it.id == taskId }
            ?: return Result.failure(Exception("Tarea no encontrada"))
        
        if (existingTask.assigneeId == null) {
            return Result.failure(Exception("La tarea no está asignada"))
        }
        
        // Actualizar tarea quitando asignación
        val updatedTask = existingTask.copy(
            assigneeId = null,
            updatedAt = generateTimestamp()
        )
        
        // Actualizar workspace
        val updatedTasks = workspace.tasks.map { 
            if (it.id == taskId) updatedTask else it 
        }
        val updatedWorkspace = workspace.copy(tasks = updatedTasks)
        
        // Auto-recalculo para limpiar bloques de esta tarea
        println("🔄 Auto-recalculando schedule (tarea desasignada)...")
        val planningUseCases = PlanningUseCases()
        return planningUseCases.generateSchedule(updatedWorkspace)
    }
    
    /**
     * Obtiene todas las tareas de un proyecto.
     */
    fun getTasksByProject(
        workspace: Workspace,
        projectId: String
    ): List<Task> {
        return workspace.tasks
            .filter { it.projectId == projectId }
            .sortedBy { it.priority } // Ordenar por prioridad (menor primero)
    }
    
    /**
     * Obtiene todas las tareas asignadas a una persona.
     */
    fun getTasksByPerson(
        workspace: Workspace,
        personId: String
    ): List<Task> {
        return workspace.tasks
            .filter { it.assigneeId == personId }
            .sortedBy { it.priority }
    }
    
    /**
     * Genera un ID único para una tarea.
     * Formato: task_{timestamp}_{random}
     */
    private fun generateTaskId(existingTasks: List<Task>): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        val id = "task_${timestamp}_$random"
        
        // Verificar unicidad
        return if (existingTasks.any { it.id == id }) {
            generateTaskId(existingTasks)
        } else {
            id
        }
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

