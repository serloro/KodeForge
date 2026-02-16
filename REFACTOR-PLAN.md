# KodeForge — Plan de Refactor Estructural

**Fecha:** 2026-02-16  
**Objetivo:** Mejorar la arquitectura sin cambiar comportamiento externo  
**Principio:** Mantener compatibilidad con `data-schema.json`

---

## Análisis de Estructura Actual

### ✅ Aspectos Bien Implementados

1. **Separación dominio/UI:**
   - ✅ `domain/model/` - Modelos de datos
   - ✅ `domain/usecases/` - Lógica de negocio
   - ✅ `domain/validation/` - Validaciones
   - ✅ `ui/` - Componentes y pantallas

2. **Repositorio aislado:**
   - ✅ `data/repository/WorkspaceRepository.kt`
   - ✅ `data/repository/JvmFileSystemAdapter.kt`

3. **Expect/Actual para multiplataforma:**
   - ✅ `sftp/SftpClient.kt`
   - ✅ `database/QueryExecutor.kt`
   - ✅ `smtp/SmtpServer.kt`

### ⚠️ Aspectos a Mejorar

1. **Services por tool mezclados:**
   - ⚠️ `smtp/` tiene `EmailSender`, `SmtpServer`, `SmtpServerManager`
   - ⚠️ `sftp/` tiene `SftpClient` y `SftpModels`
   - ⚠️ No hay estructura consistente entre tools

2. **Scheduler acoplado:**
   - ⚠️ `PlanningUseCases.kt` contiene lógica de scheduler
   - ⚠️ No hay abstracción de estrategias de scheduling
   - ⚠️ Difícil cambiar algoritmo de planificación

3. **UseCases muy grandes:**
   - ⚠️ Algunos UseCases tienen 400+ líneas
   - ⚠️ Mezclan validación, transformación y persistencia

4. **Falta capa de servicios:**
   - ⚠️ No hay `domain/services/` para lógica compleja
   - ⚠️ UseCases hacen demasiado

---

## Propuesta de Nueva Estructura

```
src/commonMain/kotlin/com/kodeforge/
├── domain/
│   ├── model/                    # Modelos de datos (sin cambios)
│   │   ├── Person.kt
│   │   ├── Project.kt
│   │   ├── Task.kt
│   │   ├── Planning.kt
│   │   ├── Workspace.kt
│   │   ├── UiState.kt
│   │   └── Secrets.kt
│   │
│   ├── validation/               # Validadores (sin cambios)
│   │   ├── PersonValidator.kt
│   │   ├── ProjectValidator.kt
│   │   ├── TaskValidator.kt
│   │   └── ...
│   │
│   ├── services/                 # ⭐ NUEVO: Servicios de dominio
│   │   ├── scheduling/
│   │   │   ├── SchedulingStrategy.kt      # Interface
│   │   │   ├── SequentialScheduler.kt     # Implementación actual
│   │   │   └── SchedulingService.kt       # Orquestador
│   │   │
│   │   ├── tools/
│   │   │   ├── SmtpService.kt             # Lógica SMTP
│   │   │   ├── RestSoapService.kt         # Lógica REST/SOAP
│   │   │   ├── SftpService.kt             # Lógica SFTP
│   │   │   ├── DbService.kt               # Lógica DB
│   │   │   └── InfoService.kt             # Lógica Info
│   │   │
│   │   └── WorkspaceService.kt            # Operaciones transversales
│   │
│   └── usecases/                 # ⭐ REFACTOR: UseCases más pequeños
│       ├── person/
│       │   ├── CreatePersonUseCase.kt
│       │   ├── UpdatePersonUseCase.kt
│       │   ├── DeletePersonUseCase.kt
│       │   └── GetPeopleUseCase.kt
│       │
│       ├── project/
│       │   ├── CreateProjectUseCase.kt
│       │   ├── UpdateProjectUseCase.kt
│       │   ├── DeleteProjectUseCase.kt
│       │   └── ManageProjectMembersUseCase.kt
│       │
│       ├── task/
│       │   ├── CreateTaskUseCase.kt
│       │   ├── AssignTaskUseCase.kt
│       │   └── UpdateTaskPriorityUseCase.kt
│       │
│       └── planning/
│           ├── RecalculatePlanningUseCase.kt
│           └── GetScheduleForPersonUseCase.kt
│
├── infrastructure/               # ⭐ NUEVO: Infraestructura
│   ├── persistence/
│   │   ├── WorkspaceRepository.kt         # Movido desde data/
│   │   └── FileSystemAdapter.kt           # Interface común
│   │
│   ├── tools/                    # ⭐ REORGANIZADO: Implementaciones de tools
│   │   ├── smtp/
│   │   │   ├── SmtpClient.kt              # expect/actual
│   │   │   ├── SmtpServer.kt              # expect/actual
│   │   │   └── SmtpServerManager.kt
│   │   │
│   │   ├── sftp/
│   │   │   ├── SftpClient.kt              # expect/actual
│   │   │   └── SftpModels.kt
│   │   │
│   │   ├── database/
│   │   │   └── QueryExecutor.kt           # expect/actual
│   │   │
│   │   └── email/
│   │       └── EmailSender.kt             # expect/actual
│   │
│   └── scheduling/
│       └── SchedulerEngine.kt             # Motor de scheduling
│
└── ui/                           # UI (sin cambios estructurales)
    ├── components/
    ├── screens/
    └── theme/
```

---

## Cambios Propuestos

### 1. Crear `domain/services/`

#### 1.1 SchedulingService

**Objetivo:** Desacoplar el scheduler de PlanningUseCases.

**Antes:**
```kotlin
// PlanningUseCases.kt (400+ líneas)
class PlanningUseCases {
    fun recalculatePlanning(workspace: Workspace): Result<Workspace> {
        // Lógica de scheduling mezclada con persistencia
    }
}
```

**Después:**
```kotlin
// domain/services/scheduling/SchedulingStrategy.kt
interface SchedulingStrategy {
    fun schedule(tasks: List<Task>, people: List<Person>): List<ScheduleBlock>
}

// domain/services/scheduling/SequentialScheduler.kt
class SequentialScheduler : SchedulingStrategy {
    override fun schedule(tasks: List<Task>, people: List<Person>): List<ScheduleBlock> {
        // Lógica de scheduling pura (sin persistencia)
    }
}

// domain/services/scheduling/SchedulingService.kt
class SchedulingService(private val strategy: SchedulingStrategy) {
    fun calculateSchedule(workspace: Workspace): Planning {
        val tasks = workspace.tasks.filter { it.assigneeId != null }
        val people = workspace.people
        val blocks = strategy.schedule(tasks, people)
        return Planning(scheduleBlocks = blocks)
    }
}

// domain/usecases/planning/RecalculatePlanningUseCase.kt
class RecalculatePlanningUseCase(private val schedulingService: SchedulingService) {
    fun execute(workspace: Workspace): Result<Workspace> {
        val newPlanning = schedulingService.calculateSchedule(workspace)
        return Result.success(workspace.copy(planning = newPlanning))
    }
}
```

**Beneficios:**
- ✅ Scheduler desacoplado
- ✅ Fácil cambiar estrategia de scheduling
- ✅ Testeable independientemente
- ✅ UseCases más pequeños

---

#### 1.2 Tool Services

**Objetivo:** Extraer lógica de negocio de UseCases a Services.

**Antes:**
```kotlin
// SmtpFakeUseCases.kt (300+ líneas)
class SmtpFakeUseCases {
    fun addRecipient(...) { /* validación + transformación + actualización */ }
    fun captureEmail(...) { /* lógica compleja */ }
    fun startServer(...) { /* mezcla lógica de negocio con infraestructura */ }
}
```

**Después:**
```kotlin
// domain/services/tools/SmtpService.kt
class SmtpService {
    fun validateRecipient(email: String): Result<Unit> { /* validación */ }
    fun captureEmail(tool: SmtpFakeTool, email: Email): SmtpFakeTool { /* lógica pura */ }
    fun filterInbox(tool: SmtpFakeTool, recipient: String): List<Email> { /* lógica pura */ }
}

// SmtpFakeUseCases.kt (más pequeño)
class SmtpFakeUseCases(private val smtpService: SmtpService) {
    fun addRecipient(workspace: Workspace, projectId: String, email: String): Result<Workspace> {
        smtpService.validateRecipient(email).getOrElse { return Result.failure(it) }
        return updateSmtpTool(workspace, projectId) { tool ->
            tool.copy(allowedRecipients = tool.allowedRecipients + email)
        }
    }
}
```

**Beneficios:**
- ✅ Lógica de negocio separada de persistencia
- ✅ Services reutilizables
- ✅ UseCases más simples (orquestación)
- ✅ Mejor testabilidad

---

### 2. Reorganizar `infrastructure/`

#### 2.1 Mover repositorio

**Antes:**
```
data/repository/WorkspaceRepository.kt
data/repository/JvmFileSystemAdapter.kt
```

**Después:**
```
infrastructure/persistence/WorkspaceRepository.kt
infrastructure/persistence/FileSystemAdapter.kt (interface)
jvmMain/.../JvmFileSystemAdapter.kt (actual)
```

**Beneficios:**
- ✅ Nombre más claro (infrastructure vs data)
- ✅ Consistente con DDD

---

#### 2.2 Consolidar tools en infrastructure

**Antes:**
```
smtp/EmailSender.kt
smtp/SmtpServer.kt
smtp/SmtpServerManager.kt
sftp/SftpClient.kt
sftp/SftpModels.kt
database/QueryExecutor.kt
```

**Después:**
```
infrastructure/tools/smtp/...
infrastructure/tools/sftp/...
infrastructure/tools/database/...
infrastructure/tools/email/...
```

**Beneficios:**
- ✅ Estructura consistente
- ✅ Fácil encontrar implementaciones
- ✅ Clara separación dominio/infraestructura

---

### 3. Dividir UseCases grandes

**Antes:**
```kotlin
// PersonUseCases.kt (300 líneas)
class PersonUseCases {
    fun createPerson(...) { }
    fun updatePerson(...) { }
    fun deletePerson(...) { }
    fun getPeople(...) { }
    fun getIdlePeople(...) { }
    fun getPersonWorkload(...) { }
    // ... más métodos
}
```

**Después:**
```kotlin
// usecases/person/CreatePersonUseCase.kt
class CreatePersonUseCase(private val validator: PersonValidator) {
    fun execute(workspace: Workspace, person: Person): Result<Workspace> {
        validator.validatePerson(person).getOrElse { return Result.failure(it) }
        // ... lógica específica
    }
}

// usecases/person/GetPeopleUseCase.kt
class GetPeopleUseCase {
    fun execute(workspace: Workspace, idleFirst: Boolean = true): List<Person> {
        // ... lógica específica
    }
}
```

**Beneficios:**
- ✅ Single Responsibility Principle
- ✅ Más fácil de mantener
- ✅ Más fácil de testear
- ✅ Más fácil de reutilizar

---

## Plan de Implementación

### Fase 1: Crear estructura nueva (sin romper nada)

1. ✅ Crear carpetas:
   - `domain/services/scheduling/`
   - `domain/services/tools/`
   - `infrastructure/persistence/`
   - `infrastructure/tools/`

2. ✅ Crear interfaces y abstracciones:
   - `SchedulingStrategy.kt`
   - `SchedulingService.kt`
   - Tool services (SmtpService, SftpService, etc.)

3. ✅ Implementar servicios (copiando lógica de UseCases)

### Fase 2: Migrar UseCases a usar Services

4. ✅ Refactorizar `PlanningUseCases` para usar `SchedulingService`
5. ✅ Refactorizar tool UseCases para usar tool Services
6. ✅ Ejecutar tests para validar que no se rompió nada

### Fase 3: Reorganizar infraestructura

7. ✅ Mover `data/repository/` a `infrastructure/persistence/`
8. ✅ Mover `smtp/`, `sftp/`, `database/` a `infrastructure/tools/`
9. ✅ Actualizar imports en todo el proyecto
10. ✅ Ejecutar tests

### Fase 4: Dividir UseCases grandes (opcional)

11. ⏸️ Dividir `PersonUseCases` en casos de uso individuales
12. ⏸️ Dividir `ProjectUseCases` en casos de uso individuales
13. ⏸️ Dividir `TaskUseCases` en casos de uso individuales

**Nota:** Fase 4 es opcional y puede hacerse incrementalmente.

---

## Criterios de Éxito

1. ✅ **Todos los tests pasan** (sin cambiar tests)
2. ✅ **Comportamiento externo idéntico**
3. ✅ **Compatibilidad con data-schema.json** mantenida
4. ✅ **Imports actualizados** correctamente
5. ✅ **Build exitoso** en todas las plataformas
6. ✅ **Scheduler desacoplado** (puede cambiar estrategia)
7. ✅ **Services reutilizables** (lógica pura)
8. ✅ **UseCases más simples** (orquestación)

---

## Riesgos y Mitigaciones

### Riesgo 1: Romper funcionalidad existente

**Mitigación:**
- Ejecutar tests después de cada cambio
- Hacer cambios incrementales
- Mantener código viejo hasta validar el nuevo

### Riesgo 2: Imports rotos en muchos archivos

**Mitigación:**
- Usar herramientas de refactor del IDE
- Hacer cambios de carpetas al final
- Validar compilación frecuentemente

### Riesgo 3: Complejidad innecesaria

**Mitigación:**
- No sobre-abstraer
- Mantener services simples
- Solo crear services donde haya lógica compleja

---

## Decisión: Alcance del Refactor

Dado el tamaño del proyecto y el riesgo de romper funcionalidad, propongo hacer **solo las Fases 1-3**:

✅ **Fase 1:** Crear `domain/services/` con SchedulingService y tool services  
✅ **Fase 2:** Refactorizar UseCases para usar Services  
✅ **Fase 3:** Reorganizar `infrastructure/`  
⏸️ **Fase 4:** Dividir UseCases (futuro, incremental)

**Tiempo estimado:** 2-3 horas  
**Archivos afectados:** ~30-40 archivos  
**Tests a ejecutar:** Todos (100+)

---

## Próximos Pasos

1. Confirmar plan con el usuario
2. Comenzar Fase 1: Crear estructura nueva
3. Implementar SchedulingService
4. Implementar tool Services
5. Migrar UseCases
6. Reorganizar carpetas
7. Validar con tests
8. Documentar cambios

---

**Estado:** ⏸️ Pendiente de aprobación

