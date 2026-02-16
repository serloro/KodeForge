package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.PlanningUseCases
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para PlanningUseCases (Scheduler Secuencial MVP).
 * 
 * Casos de prueba:
 * 1. Tarea simple que cabe en un día
 * 2. Tarea que se divide en múltiples días
 * 3. Múltiples tareas para una persona
 * 4. Múltiples personas con tareas
 * 5. Saltar fines de semana
 * 6. Tareas sin asignar (no se schedulean)
 * 7. Tareas completadas (no se schedulean)
 * 8. Persona inactiva (no se schedules)
 */
class PlanningUseCasesTest {
    
    private val planningUseCases = PlanningUseCases()
    
    @Test
    fun `test tarea simple que cabe en un dia`() {
        // Persona con 8h/día, tarea de 6h
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Simple Task",
            costHours = 6.0,
            doneHours = 0.0,
            assigneeId = "p_001",
            status = "todo",
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val startDate = LocalDate(2026, 2, 17) // Lunes
        
        val result = planningUseCases.generateSchedule(workspace, startDate)
        
        assertTrue(result.isSuccess, "El schedule debería generarse exitosamente")
        
        val updatedWorkspace = result.getOrThrow()
        val blocks = updatedWorkspace.planning.scheduleBlocks
        
        assertEquals(1, blocks.size, "Debería haber 1 bloque")
        assertEquals("p_001", blocks[0].personId)
        assertEquals("t_001", blocks[0].taskId)
        assertEquals("2026-02-17", blocks[0].date)
        assertEquals(6.0, blocks[0].hoursPlanned)
        
        println("✅ Test 1 passed: Tarea simple en 1 día")
    }
    
    @Test
    fun `test tarea que se divide en multiples dias`() {
        // Persona con 6h/día, tarea de 20h
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 6.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Large Task",
            costHours = 20.0,
            doneHours = 0.0,
            assigneeId = "p_001",
            status = "todo",
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val startDate = LocalDate(2026, 2, 17) // Lunes
        
        val result = planningUseCases.generateSchedule(workspace, startDate)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        // 20h / 6h por día = 3.33 días → 4 bloques (6+6+6+2)
        assertEquals(4, blocks.size, "Debería haber 4 bloques")
        
        assertEquals(6.0, blocks[0].hoursPlanned, "Día 1: 6h")
        assertEquals("2026-02-17", blocks[0].date)
        
        assertEquals(6.0, blocks[1].hoursPlanned, "Día 2: 6h")
        assertEquals("2026-02-18", blocks[1].date)
        
        assertEquals(6.0, blocks[2].hoursPlanned, "Día 3: 6h")
        assertEquals("2026-02-19", blocks[2].date)
        
        assertEquals(2.0, blocks[3].hoursPlanned, "Día 4: 2h")
        assertEquals("2026-02-20", blocks[3].date)
        
        println("✅ Test 2 passed: Tarea dividida en 4 días")
    }
    
    @Test
    fun `test multiples tareas para una persona ordenadas por prioridad`() {
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val tasks = listOf(
            Task(
                id = "t_001",
                projectId = "pr_001",
                title = "Task Priority 2",
                costHours = 5.0,
                doneHours = 0.0,
                assigneeId = "p_001",
                status = "todo",
                priority = 2, // Segunda prioridad
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            Task(
                id = "t_002",
                projectId = "pr_001",
                title = "Task Priority 1",
                costHours = 10.0,
                doneHours = 0.0,
                assigneeId = "p_001",
                status = "todo",
                priority = 1, // Primera prioridad (menor = más prioritario)
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            Task(
                id = "t_003",
                projectId = "pr_001",
                title = "Task Priority 3",
                costHours = 4.0,
                doneHours = 0.0,
                assigneeId = "p_001",
                status = "todo",
                priority = 3, // Tercera prioridad
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            )
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = tasks
        )
        
        val startDate = LocalDate(2026, 2, 17) // Lunes
        
        val result = planningUseCases.generateSchedule(workspace, startDate)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        // Total: 10h + 5h + 4h = 19h
        // Día 1 (8h): t_002 (priority 1) → 8h
        // Día 2 (8h): t_002 (2h restantes) + t_001 (priority 2) → 5h + t_003 (1h)
        // Día 3 (8h): t_003 (3h restantes)
        
        // Verificar que las tareas se ejecutan en orden de prioridad
        val taskOrder = blocks.map { it.taskId }.distinct()
        assertEquals(listOf("t_002", "t_001", "t_003"), taskOrder, "Tareas deben ejecutarse por prioridad")
        
        println("✅ Test 3 passed: Múltiples tareas ordenadas por prioridad")
    }
    
    @Test
    fun `test saltar fines de semana`() {
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Task",
            costHours = 24.0, // 3 días de 8h
            doneHours = 0.0,
            assigneeId = "p_001",
            status = "todo",
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val startDate = LocalDate(2026, 2, 20) // Jueves
        
        val result = planningUseCases.generateSchedule(workspace, startDate)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        assertEquals(3, blocks.size)
        assertEquals("2026-02-20", blocks[0].date, "Jueves")
        assertEquals("2026-02-23", blocks[1].date, "Lunes (salta fin de semana)")
        assertEquals("2026-02-24", blocks[2].date, "Martes")
        
        println("✅ Test 4 passed: Salta fines de semana correctamente")
    }
    
    @Test
    fun `test tareas sin asignar no se schedulean`() {
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Unassigned Task",
            costHours = 10.0,
            doneHours = 0.0,
            assigneeId = null, // Sin asignar
            status = "todo",
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val result = planningUseCases.generateSchedule(workspace)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        assertEquals(0, blocks.size, "No debería haber bloques para tareas sin asignar")
        
        println("✅ Test 5 passed: Tareas sin asignar no se schedulean")
    }
    
    @Test
    fun `test tareas completadas no se schedulean`() {
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Completed Task",
            costHours = 10.0,
            doneHours = 10.0,
            assigneeId = "p_001",
            status = "completed", // Completada
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val result = planningUseCases.generateSchedule(workspace)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        assertEquals(0, blocks.size, "No debería haber bloques para tareas completadas")
        
        println("✅ Test 6 passed: Tareas completadas no se schedulean")
    }
    
    @Test
    fun `test tarea con horas parcialmente hechas`() {
        val person = Person(
            id = "p_001",
            displayName = "Test Person",
            hoursPerDay = 8.0,
            active = true,
            meta = PersonMeta(createdAt = "2026-02-16T10:00:00Z")
        )
        
        val project = Project(
            id = "pr_001",
            name = "Test Project",
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val task = Task(
            id = "t_001",
            projectId = "pr_001",
            title = "Partial Task",
            costHours = 20.0,
            doneHours = 12.0, // Ya hizo 12h, quedan 8h
            assigneeId = "p_001",
            status = "in_progress",
            priority = 1,
            createdAt = "2026-02-16T10:00:00Z",
            updatedAt = "2026-02-16T10:00:00Z"
        )
        
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(person),
            projects = listOf(project),
            tasks = listOf(task)
        )
        
        val startDate = LocalDate(2026, 2, 17)
        
        val result = planningUseCases.generateSchedule(workspace, startDate)
        
        assertTrue(result.isSuccess)
        
        val blocks = result.getOrThrow().planning.scheduleBlocks
        
        // Solo quedan 8h pendientes → 1 día de 8h
        assertEquals(1, blocks.size)
        assertEquals(8.0, blocks[0].hoursPlanned)
        
        println("✅ Test 7 passed: Tarea con horas parcialmente hechas")
    }
}

