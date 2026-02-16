package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.PersonUseCases
import com.kodeforge.domain.usecases.PlanningUseCases
import com.kodeforge.domain.usecases.TaskUseCases
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests para validar el hardening del scheduler.
 * 
 * Escenarios críticos:
 * 1. hoursPerDay cambia → recalcula
 * 2. priority cambia → recalcula
 * 3. tarea se elimina → limpia bloques
 * 4. tarea se reasigna → mueve bloques
 * 5. costHours cambia → recalcula
 * 6. doneHours cambia → recalcula
 * 7. no hay bloques huérfanos
 * 8. integridad referencial
 * 9. distribución optimizada (capacidad residual)
 */
class SchedulerHardeningTest {
    
    private val personUseCases = PersonUseCases()
    private val taskUseCases = TaskUseCases()
    private val planningUseCases = PlanningUseCases()
    
    /**
     * Crea un workspace de prueba con 2 personas y 3 tareas.
     */
    private fun createTestWorkspace(): Workspace {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val timestamp = "${now.date}T${now.time}Z"
        
        val person1 = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0,
            role = "Developer",
            avatar = null,
            tags = emptyList(),
            active = true
        )
        
        val person2 = Person(
            id = "p2",
            displayName = "Bob",
            hoursPerDay = 6.0,
            role = "Designer",
            avatar = null,
            tags = emptyList(),
            active = true
        )
        
        val task1 = Task(
            id = "t1",
            projectId = "",
            title = "Task 1",
            costHours = 16.0,
            doneHours = 0.0,
            description = null,
            status = "todo",
            priority = 1,
            assigneeId = "p1",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val task2 = Task(
            id = "t2",
            projectId = "",
            title = "Task 2",
            costHours = 8.0,
            doneHours = 0.0,
            description = null,
            status = "todo",
            priority = 2,
            assigneeId = "p1",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val task3 = Task(
            id = "t3",
            projectId = "",
            title = "Task 3",
            costHours = 12.0,
            doneHours = 0.0,
            description = null,
            status = "todo",
            priority = 1,
            assigneeId = "p2",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            people = listOf(person1, person2),
            projects = emptyList(),
            tasks = listOf(task1, task2, task3),
            planning = Planning(
                generatedAt = timestamp,
                strategy = PlanningStrategy(type = "sequential", splitAcrossDays = true),
                scheduleBlocks = emptyList()
            ),
            uiState = UiState(),
            secrets = Secrets()
        )
    }
    
    @Test
    fun `hoursPerDay cambia - recalcula automáticamente`() {
        // Arrange
        var workspace = createTestWorkspace()
        
        // Generar schedule inicial
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        
        // Act: Cambiar hoursPerDay de Alice de 8 a 4
        workspace = personUseCases.updatePerson(workspace, "p1", hoursPerDay = 4.0).getOrThrow()
        
        // Assert: Schedule debe haberse recalculado
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        
        // Con 4h/día en vez de 8h/día, Alice necesita más días
        assertTrue(updatedBlocks.size > initialBlocks.size, 
            "Con menos hoursPerDay, debe haber más bloques (más días)")
        
        // Verificar que ningún bloque excede 4h
        updatedBlocks.forEach { block ->
            assertTrue(block.hoursPlanned <= 4.0, 
                "Ningún bloque debe exceder las nuevas hoursPerDay (4h)")
        }
    }
    
    @Test
    fun `priority cambia - recalcula y reordena tareas`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        // Verificar orden inicial: Task 1 (priority 1) antes que Task 2 (priority 2)
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        val firstTaskId = initialBlocks.first().taskId
        assertEquals("t1", firstTaskId, "Task 1 debe ser la primera (priority 1)")
        
        // Act: Cambiar prioridad de Task 2 a 0 (más urgente que Task 1)
        workspace = taskUseCases.updateTask(workspace, "t2", priority = 0).getOrThrow()
        
        // Assert: Schedule debe haberse recalculado con nuevo orden
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        val newFirstTaskId = updatedBlocks.first().taskId
        assertEquals("t2", newFirstTaskId, "Task 2 debe ser la primera ahora (priority 0)")
    }
    
    @Test
    fun `tarea se elimina - limpia bloques huérfanos`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialBlocks = workspace.planning.scheduleBlocks
        val task1Blocks = initialBlocks.filter { it.taskId == "t1" }
        assertTrue(task1Blocks.isNotEmpty(), "Debe haber bloques de Task 1")
        
        // Act: Eliminar Task 1
        workspace = taskUseCases.deleteTask(workspace, "t1").getOrThrow()
        
        // Assert: No debe haber bloques huérfanos de Task 1
        val updatedBlocks = workspace.planning.scheduleBlocks
        val orphanBlocks = updatedBlocks.filter { it.taskId == "t1" }
        assertTrue(orphanBlocks.isEmpty(), "No debe haber bloques huérfanos de Task 1")
        
        // Verificar que Task 2 y Task 3 siguen teniendo bloques
        val task2Blocks = updatedBlocks.filter { it.taskId == "t2" }
        val task3Blocks = updatedBlocks.filter { it.taskId == "t3" }
        assertTrue(task2Blocks.isNotEmpty(), "Task 2 debe seguir teniendo bloques")
        assertTrue(task3Blocks.isNotEmpty(), "Task 3 debe seguir teniendo bloques")
    }
    
    @Test
    fun `tarea se reasigna - mueve bloques a nueva persona`() {
        // Arrange: Crear workspace con proyecto
        var workspace = createTestWorkspace()
        
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val timestamp = "${now.date}T${now.time}Z"
        
        // Crear proyecto con ambas personas como miembros
        val project = Project(
            id = "proj1",
            name = "Test Project",
            description = null,
            members = listOf("p1", "p2"),
            createdAt = timestamp,
            updatedAt = timestamp,
            tools = ProjectTools()
        )
        
        workspace = workspace.copy(projects = listOf(project))
        
        // Actualizar Task 2 para que pertenezca al proyecto
        val updatedTasks = workspace.tasks.map { task ->
            if (task.id == "t2") task.copy(projectId = "proj1") else task
        }
        workspace = workspace.copy(tasks = updatedTasks)
        
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialP1Blocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" && it.taskId == "t2" }
        val initialP2Blocks = workspace.planning.scheduleBlocks.filter { it.personId == "p2" && it.taskId == "t2" }
        assertTrue(initialP1Blocks.isNotEmpty(), "Task 2 debe estar asignada a Alice")
        assertTrue(initialP2Blocks.isEmpty(), "Task 2 no debe estar asignada a Bob")
        
        // Act: Reasignar Task 2 de Alice (p1) a Bob (p2)
        workspace = taskUseCases.unassignTask(workspace, "t2").getOrThrow()
        workspace = taskUseCases.assignTaskToPerson(workspace, "t2", "p2").getOrThrow()
        
        // Assert: Bloques de Task 2 deben estar en Bob, no en Alice
        val updatedP1Blocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" && it.taskId == "t2" }
        val updatedP2Blocks = workspace.planning.scheduleBlocks.filter { it.personId == "p2" && it.taskId == "t2" }
        assertTrue(updatedP1Blocks.isEmpty(), "Task 2 no debe estar en Alice")
        assertTrue(updatedP2Blocks.isNotEmpty(), "Task 2 debe estar en Bob")
    }
    
    @Test
    fun `costHours cambia - recalcula con nueva duración`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        val initialTotalHours = initialBlocks.sumOf { it.hoursPlanned }
        assertEquals(16.0, initialTotalHours, 0.01, "Task 1 debe tener 16h inicialmente")
        
        // Act: Cambiar costHours de Task 1 de 16 a 32
        workspace = taskUseCases.updateTask(workspace, "t1", costHours = 32.0).getOrThrow()
        
        // Assert: Schedule debe reflejar las nuevas 32h
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        val updatedTotalHours = updatedBlocks.sumOf { it.hoursPlanned }
        assertEquals(32.0, updatedTotalHours, 0.01, "Task 1 debe tener 32h ahora")
        assertTrue(updatedBlocks.size > initialBlocks.size, "Debe haber más bloques (más días)")
    }
    
    @Test
    fun `doneHours cambia - recalcula con horas pendientes`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        val initialTotalHours = initialBlocks.sumOf { it.hoursPlanned }
        assertEquals(16.0, initialTotalHours, 0.01, "Task 1 debe tener 16h inicialmente")
        
        // Act: Marcar 8h como completadas
        workspace = taskUseCases.updateTask(workspace, "t1", doneHours = 8.0).getOrThrow()
        
        // Assert: Schedule debe reflejar solo las 8h pendientes
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        val updatedTotalHours = updatedBlocks.sumOf { it.hoursPlanned }
        assertEquals(8.0, updatedTotalHours, 0.01, "Task 1 debe tener solo 8h pendientes")
        assertTrue(updatedBlocks.size < initialBlocks.size, "Debe haber menos bloques (menos días)")
    }
    
    @Test
    fun `no hay bloques huérfanos después de eliminar persona inactiva`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        assertTrue(initialBlocks.isNotEmpty(), "Debe haber bloques de Alice")
        
        // Act: Desactivar Alice
        workspace = personUseCases.updatePerson(workspace, "p1", active = false).getOrThrow()
        
        // Assert: No debe haber bloques de Alice (persona inactiva)
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p1" }
        assertTrue(updatedBlocks.isEmpty(), "No debe haber bloques de persona inactiva")
        
        // Verificar que Bob sigue teniendo bloques
        val bobBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p2" }
        assertTrue(bobBlocks.isNotEmpty(), "Bob debe seguir teniendo bloques")
    }
    
    @Test
    fun `validatePlanningIntegrity detecta bloques huérfanos`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        // Crear bloques huérfanos manualmente (simular inconsistencia)
        val orphanBlock = ScheduleBlock(
            id = "orphan",
            personId = "p_inexistente",
            taskId = "t_inexistente",
            projectId = "",
            date = "2026-02-17",
            hoursPlanned = 8.0
        )
        
        val corruptedPlanning = workspace.planning.copy(
            scheduleBlocks = workspace.planning.scheduleBlocks + orphanBlock
        )
        val corruptedWorkspace = workspace.copy(planning = corruptedPlanning)
        
        // Act
        val report = planningUseCases.validatePlanningIntegrity(corruptedWorkspace)
        
        // Assert
        assertFalse(report.isValid, "Planning debe ser inválido")
        assertTrue(report.issues.isNotEmpty(), "Debe haber issues detectados")
        assertTrue(report.issues.any { it.contains("p_inexistente") }, 
            "Debe detectar personId inexistente")
        assertTrue(report.issues.any { it.contains("t_inexistente") }, 
            "Debe detectar taskId inexistente")
    }
    
    @Test
    fun `cleanOrphanBlocks limpia bloques correctamente`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        // Crear bloques huérfanos manualmente
        val orphanBlock1 = ScheduleBlock(
            id = "orphan1",
            personId = "p_inexistente",
            taskId = "t1",
            projectId = "",
            date = "2026-02-17",
            hoursPlanned = 8.0
        )
        
        val orphanBlock2 = ScheduleBlock(
            id = "orphan2",
            personId = "p1",
            taskId = "t_inexistente",
            projectId = "",
            date = "2026-02-17",
            hoursPlanned = 8.0
        )
        
        val corruptedPlanning = workspace.planning.copy(
            scheduleBlocks = workspace.planning.scheduleBlocks + orphanBlock1 + orphanBlock2
        )
        val corruptedWorkspace = workspace.copy(planning = corruptedPlanning)
        
        val initialBlockCount = corruptedWorkspace.planning.scheduleBlocks.size
        
        // Act
        val cleanedWorkspace = planningUseCases.cleanOrphanBlocks(corruptedWorkspace).getOrThrow()
        
        // Assert
        val finalBlockCount = cleanedWorkspace.planning.scheduleBlocks.size
        assertEquals(initialBlockCount - 2, finalBlockCount, 
            "Debe haber eliminado 2 bloques huérfanos")
        
        // Verificar integridad
        val report = planningUseCases.validatePlanningIntegrity(cleanedWorkspace)
        assertTrue(report.isValid, "Planning debe ser válido después de limpiar")
    }
    
    @Test
    fun `distribución optimizada - aprovecha capacidad residual del día`() {
        // Arrange
        var workspace = createTestWorkspace()
        
        // Crear persona con 8h/día y 2 tareas de 4h cada una
        val person = Person(
            id = "p_test",
            displayName = "Test",
            hoursPerDay = 8.0,
            role = "Test",
            avatar = null,
            tags = emptyList(),
            active = true
        )
        
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val timestamp = "${now.date}T${now.time}Z"
        
        val task1 = Task(
            id = "t_test1",
            projectId = "",
            title = "Task Test 1",
            costHours = 4.0,
            doneHours = 0.0,
            description = null,
            status = "todo",
            priority = 1,
            assigneeId = "p_test",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        val task2 = Task(
            id = "t_test2",
            projectId = "",
            title = "Task Test 2",
            costHours = 4.0,
            doneHours = 0.0,
            description = null,
            status = "todo",
            priority = 2,
            assigneeId = "p_test",
            createdAt = timestamp,
            updatedAt = timestamp
        )
        
        workspace = workspace.copy(
            people = workspace.people + person,
            tasks = workspace.tasks + task1 + task2
        )
        
        // Act
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        // Assert: Ambas tareas deben estar en el mismo día
        val testBlocks = workspace.planning.scheduleBlocks.filter { it.personId == "p_test" }
        
        // Agrupar por fecha
        val blocksByDate = testBlocks.groupBy { it.date }
        
        // Debe haber solo 1 día (ambas tareas en el mismo día)
        assertEquals(1, blocksByDate.size, 
            "Ambas tareas de 4h deben estar en el mismo día (8h total)")
        
        // Verificar que el día tiene exactamente 8h
        val firstDay = blocksByDate.values.first()
        val totalHours = firstDay.sumOf { it.hoursPlanned }
        assertEquals(8.0, totalHours, 0.01, 
            "El día debe tener exactamente 8h (4h + 4h)")
    }
    
    @Test
    fun `status completed - limpia bloques de tareas completadas`() {
        // Arrange
        var workspace = createTestWorkspace()
        workspace = planningUseCases.generateSchedule(workspace).getOrThrow()
        
        val initialBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        assertTrue(initialBlocks.isNotEmpty(), "Debe haber bloques de Task 1")
        
        // Act: Marcar Task 1 como completada
        workspace = taskUseCases.updateTask(workspace, "t1", status = "completed").getOrThrow()
        
        // Assert: No debe haber bloques de Task 1 (completada)
        val updatedBlocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t1" }
        assertTrue(updatedBlocks.isEmpty(), 
            "No debe haber bloques de tarea completada")
        
        // Verificar que otras tareas siguen teniendo bloques
        val task2Blocks = workspace.planning.scheduleBlocks.filter { it.taskId == "t2" }
        assertTrue(task2Blocks.isNotEmpty(), "Task 2 debe seguir teniendo bloques")
    }
}

