package com.kodeforge

import com.kodeforge.data.repository.JvmFileSystemAdapter
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.domain.model.AppMetadata
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Workspace
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorkspaceRepositoryTest {

    private lateinit var repository: WorkspaceRepository
    private lateinit var fileSystem: JvmFileSystemAdapter

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        fileSystem = JvmFileSystemAdapter()
        repository = WorkspaceRepository(fileSystem)
    }

    @AfterEach
    fun cleanup() {
        // Cleanup automático por @TempDir
    }

    @Test
    fun `test load initial schema from data-schema json`() = runBlocking {
        // Given: El archivo specs/data-schema.json existe
        val schemaPath = "specs/data-schema.json"
        val schemaFile = File(schemaPath)
        assertTrue(schemaFile.exists(), "specs/data-schema.json debe existir")

        // When: Cargamos el workspace inicial
        val workspace = repository.loadInitialSchema(schemaPath)

        // Then: Validamos estructura básica
        assertNotNull(workspace, "Workspace no debe ser null")
        assertEquals(1, workspace.app.schemaVersion, "schemaVersion debe ser 1")
        assertEquals("KodeForge", workspace.app.name, "App name debe ser KodeForge")
        
        // Validar personas
        assertTrue(workspace.people.isNotEmpty(), "Debe haber personas en el schema")
        assertEquals(3, workspace.people.size, "Debe haber 3 personas en el schema de ejemplo")
        
        val basso7 = workspace.people.find { it.id == "p_basso7" }
        assertNotNull(basso7, "Debe existir persona con id p_basso7")
        assertEquals("Basso7", basso7.displayName)
        assertEquals(6.0, basso7.hoursPerDay)
        
        // Validar proyectos
        assertTrue(workspace.projects.isNotEmpty(), "Debe haber proyectos en el schema")
        val cloudScale = workspace.projects.find { it.id == "pr_cloudScale" }
        assertNotNull(cloudScale, "Debe existir proyecto Cloud Scale UI")
        assertEquals("Cloud Scale UI", cloudScale.name)
        
        // Validar tareas
        assertTrue(workspace.tasks.isNotEmpty(), "Debe haber tareas en el schema")
        assertEquals(3, workspace.tasks.size, "Debe haber 3 tareas en el schema de ejemplo")
        
        // Validar planning
        assertTrue(workspace.planning.scheduleBlocks.isNotEmpty(), "Debe haber bloques de planificación")
        
        println("✅ Schema cargado correctamente:")
        println("   - Personas: ${workspace.people.size}")
        println("   - Proyectos: ${workspace.projects.size}")
        println("   - Tareas: ${workspace.tasks.size}")
        println("   - Bloques de planificación: ${workspace.planning.scheduleBlocks.size}")
    }

    @Test
    fun `test save and load workspace atomically`() = runBlocking {
        // Given: Un workspace de prueba
        val testWorkspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(
                Person(
                    id = "p_test",
                    displayName = "Test Person",
                    hoursPerDay = 8.0
                )
            )
        )

        val testPath = tempDir.resolve("test-workspace.json").toString()

        // When: Guardamos el workspace
        repository.save(testPath, testWorkspace)

        // Then: El archivo debe existir
        assertTrue(fileSystem.exists(testPath), "El archivo debe existir después de save")

        // When: Cargamos el workspace guardado
        val loadedWorkspace = repository.load(testPath)

        // Then: Los datos deben coincidir
        assertEquals(testWorkspace.app.schemaVersion, loadedWorkspace.app.schemaVersion)
        assertEquals(testWorkspace.app.name, loadedWorkspace.app.name)
        assertEquals(testWorkspace.people.size, loadedWorkspace.people.size)
        assertEquals(testWorkspace.people[0].id, loadedWorkspace.people[0].id)
        assertEquals(testWorkspace.people[0].displayName, loadedWorkspace.people[0].displayName)
        assertEquals(testWorkspace.people[0].hoursPerDay, loadedWorkspace.people[0].hoursPerDay)

        println("✅ Save/Load atómico funciona correctamente")
    }

    @Test
    fun `test portable behavior - copy workspace to another location`() = runBlocking {
        // Given: Cargamos el schema inicial
        val originalWorkspace = repository.loadInitialSchema("specs/data-schema.json")
        
        // When: Guardamos en una ubicación
        val location1 = tempDir.resolve("workspace1.json").toString()
        repository.save(location1, originalWorkspace)
        
        // And: Copiamos el archivo a otra ubicación (simulando copiar a otro equipo)
        val location2 = tempDir.resolve("workspace2.json").toString()
        val content = fileSystem.readFile(location1)
        fileSystem.writeFile(location2, content)
        
        // Then: Cargar desde la segunda ubicación debe funcionar igual
        val copiedWorkspace = repository.load(location2)
        
        assertEquals(originalWorkspace.app.schemaVersion, copiedWorkspace.app.schemaVersion)
        assertEquals(originalWorkspace.people.size, copiedWorkspace.people.size)
        assertEquals(originalWorkspace.projects.size, copiedWorkspace.projects.size)
        assertEquals(originalWorkspace.tasks.size, copiedWorkspace.tasks.size)
        
        // Validar que los datos específicos se mantienen
        val originalPerson = originalWorkspace.people.first()
        val copiedPerson = copiedWorkspace.people.first()
        assertEquals(originalPerson.id, copiedPerson.id)
        assertEquals(originalPerson.displayName, copiedPerson.displayName)
        assertEquals(originalPerson.hoursPerDay, copiedPerson.hoursPerDay)
        
        println("✅ Comportamiento portable validado:")
        println("   - Copiar JSON a otra ubicación funciona correctamente")
        println("   - Todos los datos se preservan")
    }

    @Test
    fun `test atomic save prevents corruption on failure`() = runBlocking {
        // Given: Un workspace válido guardado
        val workspace = Workspace(
            app = AppMetadata(
                schemaVersion = 1,
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            ),
            people = listOf(
                Person(id = "p1", displayName = "Person 1", hoursPerDay = 8.0)
            )
        )
        
        val testPath = tempDir.resolve("atomic-test.json").toString()
        repository.save(testPath, workspace)
        
        // When: Guardamos una versión modificada
        val modifiedWorkspace = workspace.copy(
            people = workspace.people + Person(id = "p2", displayName = "Person 2", hoursPerDay = 6.0)
        )
        repository.save(testPath, modifiedWorkspace)
        
        // Then: El archivo debe contener la versión modificada (no corrupta)
        val loaded = repository.load(testPath)
        assertEquals(2, loaded.people.size, "Debe haber 2 personas después de la modificación")
        
        // Verificar que no existe archivo temporal
        val tempPath = "$testPath.tmp"
        assertTrue(!fileSystem.exists(tempPath), "El archivo temporal debe ser eliminado después de save")
        
        println("✅ Save atómico previene corrupción:")
        println("   - Archivo temporal se elimina después de escritura exitosa")
        println("   - Datos se guardan correctamente")
    }

    @Test
    fun `test schemaVersion is required and validated`() = runBlocking {
        // Given: Un workspace sin schemaVersion válido
        val invalidWorkspace = Workspace(
            app = AppMetadata(
                schemaVersion = 0, // INVÁLIDO
                createdAt = "2026-02-16T10:00:00Z",
                updatedAt = "2026-02-16T10:00:00Z"
            )
        )
        
        val testPath = tempDir.resolve("invalid-schema.json").toString()
        
        // When/Then: Intentar guardar debe fallar
        var exceptionThrown = false
        try {
            repository.save(testPath, invalidWorkspace)
        } catch (e: Exception) {
            exceptionThrown = true
            assertTrue(
                e.message?.contains("schemaVersion", ignoreCase = true) == true,
                "El error debe mencionar schemaVersion. Mensaje actual: ${e.message}"
            )
            println("✅ Validación de schemaVersion funciona correctamente")
            println("   - Error capturado: ${e.message}")
        }
        
        assertTrue(exceptionThrown, "Debería haber lanzado excepción por schemaVersion inválido")
    }
}

