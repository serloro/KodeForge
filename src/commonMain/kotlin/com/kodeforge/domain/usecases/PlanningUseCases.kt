package com.kodeforge.domain.usecases

import com.kodeforge.domain.model.Planning
import com.kodeforge.domain.model.PlanningStrategy
import com.kodeforge.domain.model.ScheduleBlock
import com.kodeforge.domain.model.Workspace
import kotlinx.datetime.*
import kotlin.random.Random

/**
 * Use cases para Planning (Scheduler Secuencial MVP).
 * 
 * Algoritmo:
 * 1. Agrupar tareas por persona (solo asignadas y no completadas)
 * 2. Ordenar tareas por prioridad (menor = más prioritario)
 * 3. Calcular horas pendientes (costHours - doneHours)
 * 4. Distribuir secuencialmente en días, consumiendo hoursPerDay
 * 5. Saltar fines de semana (workingDays)
 * 6. Si tarea excede el día, dividir en días siguientes
 * 
 * Fuera de alcance MVP:
 * - Dependencias entre tareas
 * - Paralelización
 * - Festivos (solo fines de semana)
 * - Optimización IA
 */
class PlanningUseCases {
    
    /**
     * Genera el schedule completo para todas las personas.
     * 
     * @param workspace Workspace actual
     * @param startDate Fecha de inicio (default: hoy)
     * @param workingDays Días laborables (1=Lun, 7=Dom). Default: Lun-Vie
     * @return Workspace actualizado con planning generado
     */
    fun generateSchedule(
        workspace: Workspace,
        startDate: LocalDate? = null,
        workingDays: List<Int> = listOf(1, 2, 3, 4, 5) // Lun-Vie
    ): Result<Workspace> {
        try {
            val scheduleBlocks = mutableListOf<ScheduleBlock>()
            
            // Fecha de inicio (default: hoy)
            val start = startDate ?: Clock.System.now().toLocalDateTime(TimeZone.UTC).date
            
            // 1. Filtrar tareas asignadas y no completadas
            val assignedTasks = workspace.tasks.filter { task ->
                task.assigneeId != null && task.status != "completed"
            }
            
            if (assignedTasks.isEmpty()) {
                // No hay tareas asignadas, generar planning vacío
                val planning = Planning(
                    generatedAt = generateTimestamp(),
                    strategy = PlanningStrategy(
                        type = "sequential",
                        splitAcrossDays = true
                    ),
                    scheduleBlocks = emptyList()
                )
                
                return Result.success(workspace.copy(planning = planning))
            }
            
            // 2. Agrupar tareas por persona
            val tasksPerPerson = assignedTasks.groupBy { it.assigneeId!! }
            
            // 3. Para cada persona, generar schedule
            for ((personId, tasks) in tasksPerPerson) {
                val person = workspace.people.find { it.id == personId }
                
                if (person == null || !person.active || person.hoursPerDay <= 0) {
                    // Persona no encontrada, inactiva o sin horas disponibles
                    println("⚠️ Warning: Persona '$personId' no válida para scheduling, saltando...")
                    continue
                }
                
                // Ordenar tareas por prioridad (menor = más prioritario)
                val sortedTasks = tasks.sortedBy { it.priority }
                
                // Calcular horas pendientes por tarea
                val pendingTasks = sortedTasks.mapNotNull { task ->
                    val pendingHours = task.costHours - task.doneHours
                    if (pendingHours > 0) {
                        task to pendingHours
                    } else {
                        null
                    }
                }
                
                // Distribuir tareas en días
                var currentDate = start
                
                for ((task, pendingHours) in pendingTasks) {
                    var remainingHours = pendingHours
                    
                    while (remainingHours > 0) {
                        // Saltar fines de semana
                        currentDate = skipToWorkingDay(currentDate, workingDays)
                        
                        // Calcular horas a asignar este día
                        val hoursThisDay = minOf(remainingHours, person.hoursPerDay)
                        
                        // Crear ScheduleBlock
                        val block = ScheduleBlock(
                            id = generateScheduleBlockId(scheduleBlocks),
                            personId = personId,
                            taskId = task.id,
                            projectId = task.projectId,
                            date = currentDate.toString(), // YYYY-MM-DD
                            hoursPlanned = hoursThisDay
                        )
                        
                        scheduleBlocks.add(block)
                        remainingHours -= hoursThisDay
                        
                        // Si quedan horas, pasar al siguiente día
                        if (remainingHours > 0) {
                            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                        }
                    }
                    
                    // Siguiente tarea empieza en el día actual (sin saltar)
                    // Esto permite que múltiples tareas compartan el mismo día si hay capacidad
                }
            }
            
            // 4. Generar Planning
            val planning = Planning(
                generatedAt = generateTimestamp(),
                strategy = PlanningStrategy(
                    type = "sequential",
                    splitAcrossDays = true
                ),
                scheduleBlocks = scheduleBlocks
            )
            
            val updatedWorkspace = workspace.copy(planning = planning)
            
            println("✅ Schedule generado: ${scheduleBlocks.size} bloques para ${tasksPerPerson.size} personas")
            
            return Result.success(updatedWorkspace)
            
        } catch (e: Exception) {
            return Result.failure(Exception("Error generando schedule: ${e.message}", e))
        }
    }
    
    /**
     * Limpia el schedule actual (vacía scheduleBlocks).
     */
    fun clearSchedule(workspace: Workspace): Result<Workspace> {
        val planning = Planning(
            generatedAt = generateTimestamp(),
            strategy = PlanningStrategy(
                type = "sequential",
                splitAcrossDays = true
            ),
            scheduleBlocks = emptyList()
        )
        
        val updatedWorkspace = workspace.copy(planning = planning)
        
        println("🗑️ Schedule limpiado")
        
        return Result.success(updatedWorkspace)
    }
    
    /**
     * Obtiene los bloques de schedule para una persona específica.
     */
    fun getScheduleForPerson(workspace: Workspace, personId: String): List<ScheduleBlock> {
        return workspace.planning.scheduleBlocks
            .filter { it.personId == personId }
            .sortedBy { it.date }
    }
    
    /**
     * Obtiene los bloques de schedule para una fecha específica.
     */
    fun getScheduleForDate(workspace: Workspace, date: String): List<ScheduleBlock> {
        return workspace.planning.scheduleBlocks
            .filter { it.date == date }
            .sortedBy { it.personId }
    }
    
    /**
     * Calcula la fecha de finalización estimada para una persona.
     */
    fun getEstimatedEndDate(workspace: Workspace, personId: String): String? {
        val blocks = getScheduleForPerson(workspace, personId)
        return blocks.maxByOrNull { it.date }?.date
    }
    
    /**
     * Salta al siguiente día laborable.
     */
    private fun skipToWorkingDay(date: LocalDate, workingDays: List<Int>): LocalDate {
        var current = date
        while (current.dayOfWeek.isoDayNumber !in workingDays) {
            current = current.plus(1, DateTimeUnit.DAY)
        }
        return current
    }
    
    /**
     * Genera un ID único para un ScheduleBlock.
     */
    private fun generateScheduleBlockId(existingBlocks: List<ScheduleBlock>): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(1000, 9999)
        val id = "sb_${timestamp}_$random"
        
        return if (existingBlocks.any { it.id == id }) {
            generateScheduleBlockId(existingBlocks)
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
    
    /**
     * Detecta sobrecargas de personas en un rango de fechas.
     * 
     * Una persona está excedida en un día si:
     * Σ hoursPlanned (en ese día) > person.hoursPerDay
     * 
     * @param workspace Workspace actual
     * @param projectId ID del proyecto (opcional, null = todos)
     * @param startDate Fecha de inicio del rango
     * @param endDate Fecha de fin del rango
     * @return Map de personId → OverloadInfo
     */
    fun detectOverloads(
        workspace: Workspace,
        projectId: String? = null,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<String, OverloadInfo> {
        val overloads = mutableMapOf<String, OverloadInfo>()
        
        // Filtrar scheduleBlocks por proyecto si se especifica
        val relevantBlocks = if (projectId != null) {
            workspace.planning.scheduleBlocks.filter { it.projectId == projectId }
        } else {
            workspace.planning.scheduleBlocks
        }
        
        // Agrupar por persona
        val blocksByPerson = relevantBlocks.groupBy { it.personId }
        
        blocksByPerson.forEach { (personId, blocks) ->
            val person = workspace.people.find { it.id == personId } ?: return@forEach
            
            val overloadedDates = mutableSetOf<LocalDate>()
            val detailsByDate = mutableMapOf<LocalDate, DayOverload>()
            
            // Agrupar por fecha
            val blocksByDate = blocks.groupBy { LocalDate.parse(it.date) }
            
            blocksByDate.forEach { (date, dayBlocks) ->
                if (date in startDate..endDate) {
                    val totalHours = dayBlocks.sumOf { it.hoursPlanned }
                    
                    if (totalHours > person.hoursPerDay) {
                        overloadedDates.add(date)
                        detailsByDate[date] = DayOverload(
                            date = date,
                            hoursPlanned = totalHours,
                            hoursAvailable = person.hoursPerDay,
                            excess = totalHours - person.hoursPerDay
                        )
                    }
                }
            }
            
            if (overloadedDates.isNotEmpty()) {
                overloads[personId] = OverloadInfo(
                    personId = personId,
                    overloadedDates = overloadedDates,
                    detailsByDate = detailsByDate
                )
            }
        }
        
        return overloads
    }
}

/**
 * Información de sobrecarga de una persona.
 */
data class OverloadInfo(
    val personId: String,
    val overloadedDates: Set<LocalDate>,
    val detailsByDate: Map<LocalDate, DayOverload>
)

/**
 * Detalle de sobrecarga de un día específico.
 */
data class DayOverload(
    val date: LocalDate,
    val hoursPlanned: Double,
    val hoursAvailable: Double,
    val excess: Double
)

