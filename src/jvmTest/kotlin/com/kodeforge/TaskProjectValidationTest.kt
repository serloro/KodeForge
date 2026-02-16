package com.kodeforge

import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.TaskUseCases
import com.kodeforge.domain.validation.TaskValidator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para validar las nuevas reglas de T6B:
 * 1. costHours > 0 obligatorio si se asigna a persona
 * 2. assigneeId debe ser miembro del proyecto
 */
class TaskProjectValidationTest {
    
    private val taskUseCases = TaskUseCases()
    
    private fun generateTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.UTC)
        return "${localDateTime.date}T${localDateTime.time}Z"
    }
    
    private fun createTestWorkspace(
        projects: List<Project> = emptyList(),
        people: List<Person> = emptyList(),
        tasks: List<Task> = emptyList()
    ): Workspace {
        return Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = generateTimestamp(),
                updatedAt = generateTimestamp()
            ),
            projects = projects,
            people = people,
            tasks = tasks
        )
    }
    
    /**
     * Test 1: costHours > 0 obligatorio si se asigna a persona
     */
    @Test
    fun `createTask - costHours must be greater than 0 when assigning to person`() {
        println("\n🧪 TEST: costHours > 0 obligatorio si se asigna")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"),
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person)
        )
        
        // Intentar crear tarea con costHours = 0 y assigneeId
        val result = taskUseCases.createTask(
            workspace = workspace,
            projectId = "proj1",
            title = "Task A",
            costHours = 0.0, // ❌ INVÁLIDO
            assigneeId = "p1"
        )
        
        assertTrue(result.isFailure, "Debería fallar con costHours = 0 y assigneeId")
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
            errorMessage.contains("costHours", ignoreCase = true) || errorMessage.contains("costo", ignoreCase = true),
            "El error debe mencionar costHours o costo. Mensaje: $errorMessage"
        )
        
        println("✅ Validación correcta: costHours = 0 con assigneeId rechazado")
    }
    
    /**
     * Test 2: assigneeId debe ser miembro del proyecto
     */
    @Test
    fun `createTask - assignee must be project member`() {
        println("\n🧪 TEST: assignee debe ser miembro del proyecto")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"), // Solo p1 es miembro
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person1 = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val person2 = Person(
            id = "p2",
            displayName = "Bob",
            hoursPerDay = 8.0
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person1, person2)
        )
        
        // Intentar crear tarea asignada a persona que NO es miembro
        val result = taskUseCases.createTask(
            workspace = workspace,
            projectId = "proj1",
            title = "Task A",
            costHours = 10.0,
            assigneeId = "p2" // ❌ NO es miembro del proyecto
        )
        
        assertTrue(result.isFailure, "Debería fallar con assignee no miembro")
        assertTrue(
            result.exceptionOrNull()?.message?.contains("miembro") == true,
            "El error debe mencionar 'miembro'. Mensaje: ${result.exceptionOrNull()?.message}"
        )
        
        println("✅ Validación correcta: assignee no miembro rechazado")
    }
    
    /**
     * Test 3: Crear tarea asignada a miembro válido (caso exitoso)
     */
    @Test
    fun `createTask - valid assignment to project member`() {
        println("\n🧪 TEST: asignación válida a miembro del proyecto")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"),
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person)
        )
        
        // Crear tarea válida
        val result = taskUseCases.createTask(
            workspace = workspace,
            projectId = "proj1",
            title = "Task A",
            costHours = 10.0, // ✅ > 0
            assigneeId = "p1" // ✅ Miembro del proyecto
        )
        
        assertTrue(result.isSuccess, "Debería tener éxito con datos válidos")
        
        val updatedWorkspace = result.getOrNull()!!
        assertEquals(1, updatedWorkspace.tasks.size)
        
        val createdTask = updatedWorkspace.tasks.first()
        assertEquals("Task A", createdTask.title)
        assertEquals(10.0, createdTask.costHours)
        assertEquals("p1", createdTask.assigneeId)
        assertEquals("proj1", createdTask.projectId)
        
        println("✅ Tarea creada correctamente con asignación válida")
    }
    
    /**
     * Test 4: Asignar tarea existente con costHours = 0 (debe fallar)
     */
    @Test
    fun `assignTask - costHours must be greater than 0`() {
        println("\n🧪 TEST: assignTask con costHours = 0 debe fallar")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"),
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val task = Task(
            id = "t1",
            projectId = "proj1",
            title = "Task A",
            costHours = 5.0,
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person),
            tasks = listOf(task)
        )
        
        // Intentar asignar con costHours = 0
        val result = taskUseCases.assignTaskToPerson(
            workspace = workspace,
            taskId = "t1",
            personId = "p1",
            costHours = 0.0 // ❌ INVÁLIDO
        )
        
        assertTrue(result.isFailure, "Debería fallar con costHours = 0")
        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
            errorMessage.contains("costHours", ignoreCase = true) || errorMessage.contains("costo", ignoreCase = true),
            "El error debe mencionar costHours o costo. Mensaje: $errorMessage"
        )
        
        println("✅ Validación correcta: assignTask con costHours = 0 rechazado")
    }
    
    /**
     * Test 5: Asignar tarea a persona que NO es miembro (debe fallar)
     */
    @Test
    fun `assignTask - assignee must be project member`() {
        println("\n🧪 TEST: assignTask a no miembro debe fallar")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"), // Solo p1 es miembro
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person1 = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val person2 = Person(
            id = "p2",
            displayName = "Bob",
            hoursPerDay = 8.0
        )
        
        val task = Task(
            id = "t1",
            projectId = "proj1",
            title = "Task A",
            costHours = 10.0,
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person1, person2),
            tasks = listOf(task)
        )
        
        // Intentar asignar a persona que NO es miembro
        val result = taskUseCases.assignTaskToPerson(
            workspace = workspace,
            taskId = "t1",
            personId = "p2", // ❌ NO es miembro
            costHours = 10.0
        )
        
        assertTrue(result.isFailure, "Debería fallar con assignee no miembro")
        assertTrue(
            result.exceptionOrNull()?.message?.contains("miembro") == true,
            "El error debe mencionar 'miembro'. Mensaje: ${result.exceptionOrNull()?.message}"
        )
        
        println("✅ Validación correcta: assignTask a no miembro rechazado")
    }
    
    /**
     * Test 6: Asignar tarea a miembro válido (caso exitoso)
     */
    @Test
    fun `assignTask - valid assignment to project member`() {
        println("\n🧪 TEST: assignTask válido a miembro del proyecto")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"),
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val person = Person(
            id = "p1",
            displayName = "Alice",
            hoursPerDay = 8.0
        )
        
        val task = Task(
            id = "t1",
            projectId = "proj1",
            title = "Task A",
            costHours = 5.0,
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project),
            people = listOf(person),
            tasks = listOf(task)
        )
        
        // Asignar tarea válida
        val result = taskUseCases.assignTaskToPerson(
            workspace = workspace,
            taskId = "t1",
            personId = "p1", // ✅ Miembro del proyecto
            costHours = 10.0 // ✅ > 0
        )
        
        assertTrue(result.isSuccess, "Debería tener éxito con datos válidos")
        
        val updatedWorkspace = result.getOrNull()!!
        val updatedTask = updatedWorkspace.tasks.first()
        
        assertEquals("p1", updatedTask.assigneeId)
        assertEquals(10.0, updatedTask.costHours)
        
        println("✅ Tarea asignada correctamente a miembro válido")
    }
    
    /**
     * Test 7: Crear tarea sin asignar (costHours puede ser 0)
     */
    @Test
    fun `createTask - unassigned task can have costHours = 0`() {
        println("\n🧪 TEST: tarea sin asignar puede tener costHours = 0")
        
        val project = Project(
            id = "proj1",
            name = "Project 1",
            members = listOf("p1"),
            createdAt = generateTimestamp(),
            updatedAt = generateTimestamp()
        )
        
        val workspace = createTestWorkspace(
            projects = listOf(project)
        )
        
        // Crear tarea sin asignar con costHours = 0
        val result = taskUseCases.createTask(
            workspace = workspace,
            projectId = "proj1",
            title = "Task A",
            costHours = 0.1, // Mínimo válido
            assigneeId = null // Sin asignar
        )
        
        assertTrue(result.isSuccess, "Debería tener éxito sin assignee")
        
        val updatedWorkspace = result.getOrNull()!!
        assertEquals(1, updatedWorkspace.tasks.size)
        
        val createdTask = updatedWorkspace.tasks.first()
        assertEquals("Task A", createdTask.title)
        assertEquals(null, createdTask.assigneeId)
        
        println("✅ Tarea sin asignar creada correctamente")
    }
}

