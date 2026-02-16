# T0 - Workspace Portable JSON Layer

## ✅ Implementación Completada

**Fecha:** 16 de febrero de 2026  
**Tarea:** T0 - KMP + JSON Workspace  
**Estado:** ✅ COMPLETADO

---

## 📋 Requisitos Cumplidos

### ✅ 1. schemaVersion Obligatorio
- Campo `schemaVersion` requerido en `AppMetadata`
- Validación en load/save (debe ser > 0)
- Permite migraciones futuras del schema

### ✅ 2. Load/Save Atómico
- Estrategia: escribir a archivo temporal → atomic rename
- Previene corrupción de datos en caso de fallo
- Implementado con `Files.move()` + `ATOMIC_MOVE` en JVM

### ✅ 3. Carga de specs/data-schema.json
- Método `loadInitialSchema()` carga el schema de ejemplo
- Workspace inicial con:
  - 3 personas (Basso7, Blanco J, Bocera J)
  - 1 proyecto (Cloud Scale UI)
  - 3 tareas
  - 5 bloques de planificación
  - Herramientas configuradas (SMTP, REST/SOAP, Info)

### ✅ 4. Comportamiento Portable
- Copiar JSON a otra ubicación funciona correctamente
- Todos los datos se preservan
- No hay dependencias externas para funcionar
- Validado con tests automatizados

---

## 🏗️ Arquitectura Implementada

### Estructura del Proyecto

```
kodeforge/
├── src/
│   ├── commonMain/kotlin/com/kodeforge/
│   │   ├── domain/model/
│   │   │   ├── Workspace.kt          # Modelo raíz + AppMetadata
│   │   │   ├── Person.kt             # Modelo de personas
│   │   │   ├── Project.kt            # Modelo de proyectos + tools
│   │   │   ├── Task.kt               # Modelo de tareas
│   │   │   ├── Planning.kt           # Planificación + ScheduleBlocks
│   │   │   ├── UiState.kt            # Estado de UI
│   │   │   └── Secrets.kt            # Gestión de secretos
│   │   └── data/repository/
│   │       └── WorkspaceRepository.kt # Repositorio con load/save
│   ├── jvmMain/kotlin/com/kodeforge/
│   │   ├── Main.kt                    # Demo de T0
│   │   └── data/repository/
│   │       └── JvmFileSystemAdapter.kt # Implementación JVM
│   └── jvmTest/kotlin/com/kodeforge/
│       └── WorkspaceRepositoryTest.kt  # Tests unitarios
├── specs/
│   └── data-schema.json               # Schema inicial
├── build.gradle.kts                   # Configuración Gradle
├── settings.gradle.kts
└── gradle.properties
```

### Modelo de Datos Completo

#### Workspace (Raíz)
```kotlin
data class Workspace(
    val app: AppMetadata,              // Metadata + schemaVersion
    val people: List<Person>,          // Personas
    val projects: List<Project>,       // Proyectos
    val tasks: List<Task>,             // Tareas
    val planning: Planning,            // Planificación
    val uiState: UiState,              // Estado UI
    val secrets: Secrets               // Secretos
)
```

#### AppMetadata
```kotlin
data class AppMetadata(
    val name: String = "KodeForge",
    val schemaVersion: Int,            // ⚠️ REQUIRED
    val createdAt: String,
    val updatedAt: String,
    val defaultLocale: String = "es",
    val supportedLocales: List<String>,
    val settings: AppSettings
)
```

#### Person
```kotlin
data class Person(
    val id: String,
    val displayName: String,
    val avatar: String?,               // Path relativo o null
    val role: String?,
    val hoursPerDay: Double,           // ⚠️ REQUIRED
    val active: Boolean = true,
    val tags: List<String>,            // Tags libres
    val meta: PersonMeta
)
```

#### Project + ProjectTools
```kotlin
data class Project(
    val id: String,
    val name: String,
    val description: String?,
    val status: String,                // active, paused, completed, cancelled
    val members: List<String>,         // IDs de personas
    val createdAt: String,
    val updatedAt: String,
    val tools: ProjectTools            // Herramientas del proyecto
)

data class ProjectTools(
    val smtpFake: SmtpFakeTool?,
    val restSoap: RestSoapTool?,
    val sftp: SftpTool?,
    val dbTools: DbTool?,
    val taskManager: TaskManagerTool?,
    val info: InfoTool?                // WYSIWYG HTML multiidioma
)
```

#### Task
```kotlin
data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val status: String,                // todo, in_progress, completed
    val priority: Int,                 // Menor = más prioritario
    val costHours: Double,             // ⚠️ REQUIRED si hay assigneeId
    val doneHours: Double = 0.0,
    val assigneeId: String?,
    val createdAt: String,
    val updatedAt: String
)
```

#### Planning
```kotlin
data class Planning(
    val generatedAt: String?,
    val strategy: PlanningStrategy,
    val scheduleBlocks: List<ScheduleBlock>
)

data class ScheduleBlock(
    val id: String,
    val personId: String,
    val taskId: String,
    val projectId: String,
    val date: String,                  // YYYY-MM-DD
    val hoursPlanned: Double
)
```

### WorkspaceRepository

```kotlin
class WorkspaceRepository(private val fileSystem: FileSystemAdapter) {
    
    suspend fun load(path: String): Workspace
    
    suspend fun save(path: String, workspace: Workspace)
    
    suspend fun loadInitialSchema(schemaPath: String = "specs/data-schema.json"): Workspace
}
```

**Características:**
- ✅ Validación de `schemaVersion` en load/save
- ✅ Escritura atómica (temp file + atomic rename)
- ✅ Pretty-print JSON para legibilidad
- ✅ Manejo de errores con excepciones específicas

### FileSystemAdapter (Multiplataforma)

```kotlin
interface FileSystemAdapter {
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String)
    suspend fun atomicMove(sourcePath: String, destPath: String)
    suspend fun exists(path: String): Boolean
    suspend fun delete(path: String)
}
```

**Implementación JVM:**
- Usa `java.io.File` y `java.nio.file.Files`
- `Files.move()` con `ATOMIC_MOVE` + `REPLACE_EXISTING`
- Coroutines con `Dispatchers.IO`

---

## 🧪 Tests Implementados

### WorkspaceRepositoryTest (5 tests, 100% pasados)

1. **`test load initial schema from data-schema json`**
   - Carga `specs/data-schema.json`
   - Valida estructura básica (personas, proyectos, tareas)
   - Verifica `schemaVersion = 1`

2. **`test save and load workspace atomically`**
   - Crea workspace de prueba
   - Guarda y recarga
   - Verifica que los datos coinciden

3. **`test portable behavior - copy workspace to another location`**
   - Carga schema inicial
   - Guarda en ubicación 1
   - Copia a ubicación 2 (simula copiar a otro equipo)
   - Verifica que ambos workspaces son idénticos

4. **`test atomic save prevents corruption on failure`**
   - Guarda workspace
   - Modifica y guarda nuevamente
   - Verifica que no existe archivo temporal después de save
   - Confirma que los datos se guardaron correctamente

5. **`test schemaVersion is required and validated`**
   - Intenta guardar workspace con `schemaVersion = 0`
   - Verifica que lanza excepción
   - Confirma que el mensaje menciona "schemaVersion"

### Ejecutar Tests

```bash
./gradlew jvmTest
```

**Resultado:** ✅ 5/5 tests pasados (100%)

---

## 🚀 Demostración

### Ejecutar Demo

```bash
./gradlew runDemo
```

### Salida de la Demo

```
═══════════════════════════════════════════════════════════
  KodeForge - T0 Workspace Portable JSON Layer Demo
═══════════════════════════════════════════════════════════

📂 Cargando workspace inicial desde specs/data-schema.json...
✅ Workspace cargado correctamente:
   • App: KodeForge
   • Schema Version: 1
   • Personas: 3
     - Basso7 (6.0h/día)
     - Blanco J (8.0h/día)
     - Bocera J (4.0h/día)
   • Proyectos: 1
     - Cloud Scale UI (active)
   • Tareas: 3
   • Bloques de planificación: 5

💾 Guardando workspace en workspace.json...
✅ Workspace guardado correctamente (escritura atómica)

🔄 Validando comportamiento portable...
   Copiando workspace.json → workspace-copy.json
   Cargando workspace desde copia...
✅ Comportamiento portable validado:
   • Copiar JSON a otra ubicación funciona correctamente
   • Todos los datos se preservan

═══════════════════════════════════════════════════════════
  T0 - Características implementadas:
═══════════════════════════════════════════════════════════
✅ Workspace portable JSON layer
✅ schemaVersion obligatorio (validado)
✅ Load/Save atómico (previene corrupción)
✅ Carga specs/data-schema.json como workspace inicial
✅ Comportamiento portable (copiar JSON funciona)
✅ Modelo de datos completo
✅ Tests unitarios (100% pasados)
```

### Archivos Generados

- `workspace.json` - Workspace principal (12 KB)
- `workspace-copy.json` - Copia para validación (12 KB)

---

## 📊 Validación de Comportamiento Portable

### Prueba Manual

1. **Ejecutar demo:**
   ```bash
   ./gradlew runDemo
   ```

2. **Copiar workspace.json a otra ubicación:**
   ```bash
   cp workspace.json /tmp/workspace-test.json
   ```

3. **Verificar que se puede cargar desde la nueva ubicación:**
   ```kotlin
   val repository = WorkspaceRepository(JvmFileSystemAdapter())
   val workspace = repository.load("/tmp/workspace-test.json")
   // ✅ Funciona correctamente
   ```

### Validación Automatizada

Los tests validan automáticamente:
- ✅ Copiar JSON preserva todos los datos
- ✅ No hay dependencias de rutas absolutas
- ✅ El workspace es completamente portable

---

## 🔧 Tecnologías Utilizadas

- **Kotlin Multiplatform:** 1.9.22
- **kotlinx.serialization:** 1.6.2 (JSON)
- **kotlinx.coroutines:** 1.7.3 (Async I/O)
- **kotlinx.datetime:** 0.5.0 (Fechas)
- **JUnit 5:** 5.10.1 (Tests)
- **Gradle:** 8.5

---

## 📝 Notas de Implementación

### Decisiones de Diseño

1. **schemaVersion obligatorio:**
   - Permite migraciones futuras del formato JSON
   - Se valida en load/save para prevenir errores

2. **Save atómico:**
   - Estrategia: temp file + atomic rename
   - Previene corrupción si falla durante escritura
   - En JVM usa `Files.move()` con `ATOMIC_MOVE`

3. **Modelo de datos completo:**
   - Incluye todas las entidades del sistema
   - ProjectTools con 6 herramientas (SMTP, REST/SOAP, SFTP, DB, Tasks, Info)
   - Info tool con soporte multiidioma (páginas HTML por idioma)

4. **Secrets en texto plano (MVP):**
   - ⚠️ En producción migrar a Keychain/Credential Manager
   - Campo `value` opcional para almacenar secretos
   - Por ahora solo referencias (placeholders)

5. **FileSystemAdapter multiplataforma:**
   - Interface común para todas las plataformas
   - Implementación JVM con `java.io.File`
   - Futuro: implementaciones para Native (macOS, Windows, Linux) y JS

### Limitaciones Conocidas

1. **Sin UI:** T0 solo implementa la capa de datos (según requisitos)
2. **Secrets en texto plano:** Migrar a keychain en producción
3. **Solo target JVM:** Otros targets (Native, JS) se agregarán según necesidad
4. **Sin validación de IDs:** No se valida que los IDs referenciados existan

### Próximos Pasos (T1)

- UI base con Compose Multiplatform
- Sidebar con Projects y Personas
- Botones "Gestionar" junto a títulos
- Pantalla Home con resumen global
- Scroll independiente en sidebar

---

## ✅ Checklist de Requisitos T0

- [x] Proyecto Kotlin Multiplatform creado
- [x] schemaVersion obligatorio y validado
- [x] Load/Save atómico implementado
- [x] Carga specs/data-schema.json como workspace inicial
- [x] Modelo de datos completo (Workspace, Person, Project, Task, Planning, etc.)
- [x] WorkspaceRepository con load/save
- [x] FileSystemAdapter JVM implementado
- [x] Tests unitarios (5 tests, 100% pasados)
- [x] Validación de comportamiento portable
- [x] Demo funcional
- [x] Documentación completa

---

## 📚 Referencias

- **Spec:** `/specs/spec.md`
- **Tasks:** `/specs/tasks.md`
- **Data Schema:** `/specs/data-schema.json`
- **Tests:** `/src/jvmTest/kotlin/com/kodeforge/WorkspaceRepositoryTest.kt`
- **Demo:** `/src/jvmMain/kotlin/com/kodeforge/Main.kt`

---

**Implementado por:** Claude Sonnet 4.5  
**Fecha:** 16 de febrero de 2026  
**Estado:** ✅ COMPLETADO Y VALIDADO

