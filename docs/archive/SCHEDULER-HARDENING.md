# KodeForge — Scheduler Hardening (Análisis y Mejoras)

**Fecha:** 2026-02-16  
**Objetivo:** Validar robustez del scheduler y proponer mejoras  
**Alcance:** Sin IA ni dependencias complejas

---

## 1. ANÁLISIS DE PROBLEMAS ACTUALES

### 🔴 PROBLEMA CRÍTICO 1: No se recalcula automáticamente

**Situación actual:**
- `generateSchedule()` se llama manualmente
- Los cambios en tareas NO disparan recalculo
- Los cambios en personas NO disparan recalculo

**Escenarios problemáticos:**

#### Escenario A: hoursPerDay cambia
```kotlin
// Usuario cambia hoursPerDay de 8 a 4
personUseCases.updatePerson(workspace, personId, hoursPerDay = 4.0)
// ❌ scheduleBlocks sigue usando 8 horas/día
// ❌ Persona aparece como "disponible" cuando está sobrecargada
```

**Impacto:** CRÍTICO
- Planning desactualizado
- Fechas de fin incorrectas
- Detección de overload errónea

---

#### Escenario B: priority cambia
```kotlin
// Usuario cambia prioridad de tarea de 5 a 1 (más urgente)
taskUseCases.updateTask(workspace, taskId, priority = 1)
// ❌ scheduleBlocks mantiene orden antiguo
// ❌ Tarea urgente sigue al final
```

**Impacto:** ALTO
- Orden de ejecución incorrecto
- Tareas urgentes no se priorizan

---

#### Escenario C: tarea se elimina
```kotlin
// Usuario elimina una tarea
taskUseCases.deleteTask(workspace, taskId)
// ❌ scheduleBlocks sigue teniendo bloques de esa tarea
// ❌ BLOQUES HUÉRFANOS en planning
```

**Impacto:** CRÍTICO
- Bloques huérfanos (taskId no existe)
- Timeline muestra tareas fantasma
- Cálculos de carga incorrectos

---

#### Escenario D: tarea se reasigna
```kotlin
// Usuario reasigna tarea de persona A a persona B
taskUseCases.assignTask(workspace, taskId, personIdB)
// ❌ scheduleBlocks de persona A siguen existiendo
// ❌ scheduleBlocks de persona B no se crean
// ❌ INCONSISTENCIA TOTAL
```

**Impacto:** CRÍTICO
- Persona A tiene bloques de tarea que ya no es suya
- Persona B no tiene bloques de su nueva tarea
- Ambos timelines incorrectos

---

#### Escenario E: costHours cambia
```kotlin
// Usuario cambia costHours de 10 a 20 (tarea más grande)
taskUseCases.updateTask(workspace, taskId, costHours = 20.0)
// ❌ scheduleBlocks sigue usando 10 horas
// ❌ Fecha de fin incorrecta
```

**Impacto:** ALTO
- Estimaciones incorrectas
- Persona parece tener más capacidad de la real

---

#### Escenario F: doneHours cambia
```kotlin
// Usuario marca progreso: doneHours de 0 a 5
taskUseCases.updateTask(workspace, taskId, doneHours = 5.0)
// ❌ scheduleBlocks sigue usando costHours completo
// ❌ No se liberan días
```

**Impacto:** MEDIO
- Planning no refleja progreso real
- Fechas de fin no se actualizan

---

### 🔴 PROBLEMA CRÍTICO 2: Bloques huérfanos no se limpian

**Código actual en `deleteTask`:**
```kotlin
fun deleteTask(workspace: Workspace, taskId: String): Result<Workspace> {
    val updatedTasks = workspace.tasks.filter { it.id != taskId }
    val updatedWorkspace = workspace.copy(tasks = updatedTasks)
    
    // ❌ NO limpia scheduleBlocks
    
    return Result.success(updatedWorkspace)
}
```

**Problema:**
- `scheduleBlocks` contiene bloques con `taskId` que ya no existe
- `detectOverloads()` cuenta horas de tareas eliminadas
- Timeline muestra bloques fantasma

**Evidencia en código:**
```kotlin
// PlanningUseCases.kt:253
val relevantBlocks = workspace.planning.scheduleBlocks.filter { it.projectId == projectId }

// ❌ No valida que taskId exista en workspace.tasks
// ❌ No valida que personId exista en workspace.people
```

---

### 🟠 PROBLEMA ALTO 3: No valida integridad referencial

**Código actual:**
```kotlin
fun generateSchedule(workspace: Workspace, ...): Result<Workspace> {
    // ...
    val person = workspace.people.find { it.id == personId }
    
    if (person == null || !person.active || person.hoursPerDay <= 0) {
        println("⚠️ Warning: Persona '$personId' no válida, saltando...")
        continue // ❌ Salta silenciosamente
    }
}
```

**Problemas:**
1. Si persona se desactiva (`active = false`), sus bloques quedan huérfanos
2. Si persona se elimina, sus bloques quedan huérfanos
3. No hay validación al leer `scheduleBlocks`

**Escenario:**
```kotlin
// 1. Generar schedule para persona A
planningUseCases.generateSchedule(workspace)

// 2. Desactivar persona A
personUseCases.updatePerson(workspace, personIdA, active = false)

// 3. Mostrar timeline
val blocks = planningUseCases.getScheduleForPerson(workspace, personIdA)
// ✅ Devuelve bloques
// ❌ Pero persona está inactiva
// ❌ UI muestra datos inconsistentes
```

---

### 🟠 PROBLEMA ALTO 4: Bug en distribución de días

**Código actual (líneas 94-127):**
```kotlin
var currentDate = start

for ((task, pendingHours) in pendingTasks) {
    var remainingHours = pendingHours
    
    while (remainingHours > 0) {
        currentDate = skipToWorkingDay(currentDate, workingDays)
        val hoursThisDay = minOf(remainingHours, person.hoursPerDay)
        // ... crear block
        
        if (remainingHours > 0) {
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }
    }
    
    // ❌ BUG: Siguiente tarea empieza en currentDate
    // ❌ Si tarea anterior usó 4h de 8h disponibles,
    // ❌ siguiente tarea NO usa las 4h restantes
}
```

**Problema:**
- Cada tarea empieza en un día nuevo
- No se aprovecha capacidad residual del día
- Desperdicio de horas disponibles

**Ejemplo:**
```
Persona: 8h/día
Tarea A: 4h (prioridad 1)
Tarea B: 4h (prioridad 2)

Resultado actual:
- Día 1: 4h (Tarea A) → 4h desperdiciadas
- Día 2: 4h (Tarea B) → 4h desperdiciadas

Resultado esperado:
- Día 1: 4h (Tarea A) + 4h (Tarea B) → 0h desperdiciadas
```

---

### 🟡 PROBLEMA MEDIO 5: No maneja tareas completadas

**Código actual:**
```kotlin
val assignedTasks = workspace.tasks.filter { task ->
    task.assigneeId != null && task.status != "completed"
}
```

**Problema:**
- Tareas completadas se excluyen del schedule
- ✅ Correcto para planning futuro
- ❌ Incorrecto para histórico/análisis

**Escenario:**
```kotlin
// Usuario marca tarea como completada
taskUseCases.updateTask(workspace, taskId, status = "completed")

// ❌ scheduleBlocks de esa tarea siguen existiendo
// ❌ Pero generateSchedule() no los regenera
// ❌ Inconsistencia: bloques de tarea "completed" en planning
```

---

### 🟡 PROBLEMA MEDIO 6: ID generation no es determinista

**Código actual:**
```kotlin
private fun generateScheduleBlockId(existingBlocks: List<ScheduleBlock>): String {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    val random = Random.nextInt(1000, 9999)
    val id = "sb_${timestamp}_$random"
    
    return if (existingBlocks.any { it.id == id }) {
        generateScheduleBlockId(existingBlocks) // ❌ Recursión
    } else {
        id
    }
}
```

**Problemas:**
1. IDs cambian en cada regeneración (dificulta tracking)
2. Recursión puede ser infinita (muy improbable pero posible)
3. No es reproducible en tests

---

## 2. PROPUESTAS DE MEJORA

### ✅ MEJORA 1: Auto-recalculo inteligente

**Objetivo:** Recalcular automáticamente cuando cambian datos relevantes.

**Implementación:**

```kotlin
// domain/usecases/TaskUseCases.kt
fun updateTask(...): Result<Workspace> {
    // ... actualizar tarea
    
    // ✅ Auto-recalculo si cambió algo que afecta planning
    val needsReschedule = costHours != null || 
                          priority != null || 
                          doneHours != null ||
                          status != null
    
    return if (needsReschedule) {
        val planningUseCases = PlanningUseCases()
        planningUseCases.generateSchedule(updatedWorkspace)
    } else {
        Result.success(updatedWorkspace)
    }
}

fun deleteTask(...): Result<Workspace> {
    // ... eliminar tarea
    
    // ✅ Auto-recalculo (limpia bloques huérfanos)
    val planningUseCases = PlanningUseCases()
    return planningUseCases.generateSchedule(updatedWorkspace)
}

fun assignTask(...): Result<Workspace> {
    // ... asignar tarea
    
    // ✅ Auto-recalculo (mueve bloques a nueva persona)
    val planningUseCases = PlanningUseCases()
    return planningUseCases.generateSchedule(updatedWorkspace)
}

// domain/usecases/PersonUseCases.kt
fun updatePerson(...): Result<Workspace> {
    // ... actualizar persona
    
    // ✅ Auto-recalculo si cambió hoursPerDay
    return if (hoursPerDay != null) {
        val planningUseCases = PlanningUseCases()
        planningUseCases.generateSchedule(updatedWorkspace)
    } else {
        Result.success(updatedWorkspace)
    }
}
```

**Beneficios:**
- ✅ Planning siempre actualizado
- ✅ No hay bloques huérfanos
- ✅ Fechas de fin correctas
- ✅ UX transparente (usuario no debe "recalcular")

**Riesgos:**
- ⚠️ Recalculo puede ser costoso (muchas tareas)
- ⚠️ Puede ser lento en UI

**Mitigación:**
- Hacer recalculo en background (coroutine)
- Mostrar indicador de "Recalculando..."
- Cachear resultados si workspace no cambió

---

### ✅ MEJORA 2: Limpieza de bloques huérfanos

**Objetivo:** Eliminar bloques que referencian tareas/personas inexistentes.

**Implementación:**

```kotlin
// domain/usecases/PlanningUseCases.kt

/**
 * Limpia bloques huérfanos (referencias a tareas/personas inexistentes).
 */
fun cleanOrphanBlocks(workspace: Workspace): Result<Workspace> {
    val validTaskIds = workspace.tasks.map { it.id }.toSet()
    val validPersonIds = workspace.people.filter { it.active }.map { it.id }.toSet()
    
    val cleanedBlocks = workspace.planning.scheduleBlocks.filter { block ->
        block.taskId in validTaskIds && block.personId in validPersonIds
    }
    
    val orphanCount = workspace.planning.scheduleBlocks.size - cleanedBlocks.size
    
    if (orphanCount > 0) {
        println("🗑️ Limpiados $orphanCount bloques huérfanos")
    }
    
    val updatedPlanning = workspace.planning.copy(
        scheduleBlocks = cleanedBlocks,
        generatedAt = generateTimestamp()
    )
    
    return Result.success(workspace.copy(planning = updatedPlanning))
}

/**
 * Genera schedule limpiando primero bloques huérfanos.
 */
fun generateSchedule(...): Result<Workspace> {
    // 1. Limpiar bloques huérfanos antes de regenerar
    val cleanedWorkspace = cleanOrphanBlocks(workspace).getOrThrow()
    
    // 2. Generar schedule nuevo
    // ... resto del código actual
}
```

**Beneficios:**
- ✅ No hay bloques huérfanos
- ✅ Cálculos de carga correctos
- ✅ Timeline limpio

---

### ✅ MEJORA 3: Validación de integridad referencial

**Objetivo:** Validar que scheduleBlocks sean consistentes con workspace.

**Implementación:**

```kotlin
// domain/usecases/PlanningUseCases.kt

/**
 * Valida integridad referencial del planning.
 */
fun validatePlanningIntegrity(workspace: Workspace): PlanningIntegrityReport {
    val issues = mutableListOf<String>()
    
    val validTaskIds = workspace.tasks.map { it.id }.toSet()
    val validPersonIds = workspace.people.map { it.id }.toSet()
    val activePersonIds = workspace.people.filter { it.active }.map { it.id }.toSet()
    
    workspace.planning.scheduleBlocks.forEach { block ->
        // Validar taskId existe
        if (block.taskId !in validTaskIds) {
            issues.add("Block ${block.id}: taskId '${block.taskId}' no existe")
        }
        
        // Validar personId existe
        if (block.personId !in validPersonIds) {
            issues.add("Block ${block.id}: personId '${block.personId}' no existe")
        }
        
        // Validar persona está activa
        if (block.personId !in activePersonIds) {
            issues.add("Block ${block.id}: personId '${block.personId}' está inactiva")
        }
        
        // Validar hoursPlanned > 0
        if (block.hoursPlanned <= 0) {
            issues.add("Block ${block.id}: hoursPlanned <= 0")
        }
        
        // Validar fecha válida
        try {
            LocalDate.parse(block.date)
        } catch (e: Exception) {
            issues.add("Block ${block.id}: fecha inválida '${block.date}'")
        }
    }
    
    return PlanningIntegrityReport(
        isValid = issues.isEmpty(),
        issues = issues,
        totalBlocks = workspace.planning.scheduleBlocks.size,
        validBlocks = workspace.planning.scheduleBlocks.size - issues.size
    )
}

data class PlanningIntegrityReport(
    val isValid: Boolean,
    val issues: List<String>,
    val totalBlocks: Int,
    val validBlocks: Int
)
```

**Uso:**
```kotlin
// En UI o tests
val report = planningUseCases.validatePlanningIntegrity(workspace)
if (!report.isValid) {
    println("⚠️ Planning tiene ${report.issues.size} problemas:")
    report.issues.forEach { println("  - $it") }
}
```

**Beneficios:**
- ✅ Detecta inconsistencias
- ✅ Útil para debugging
- ✅ Útil para tests

---

### ✅ MEJORA 4: Optimizar distribución de días

**Objetivo:** Aprovechar capacidad residual del día.

**Implementación:**

```kotlin
// domain/usecases/PlanningUseCases.kt

fun generateSchedule(...): Result<Workspace> {
    // ...
    
    for ((personId, tasks) in tasksPerPerson) {
        val person = workspace.people.find { it.id == personId } ?: continue
        val sortedTasks = tasks.sortedBy { it.priority }
        val pendingTasks = sortedTasks.mapNotNull { /* ... */ }
        
        var currentDate = start
        var remainingCapacityToday = 0.0 // ✅ Capacidad residual
        
        for ((task, pendingHours) in pendingTasks) {
            var remainingHours = pendingHours
            
            while (remainingHours > 0) {
                currentDate = skipToWorkingDay(currentDate, workingDays)
                
                // ✅ Usar capacidad residual si existe
                if (remainingCapacityToday <= 0) {
                    remainingCapacityToday = person.hoursPerDay
                }
                
                val hoursThisDay = minOf(remainingHours, remainingCapacityToday)
                
                val block = ScheduleBlock(
                    id = generateScheduleBlockId(scheduleBlocks),
                    personId = personId,
                    taskId = task.id,
                    projectId = task.projectId,
                    date = currentDate.toString(),
                    hoursPlanned = hoursThisDay
                )
                
                scheduleBlocks.add(block)
                remainingHours -= hoursThisDay
                remainingCapacityToday -= hoursThisDay
                
                // ✅ Si se agotó capacidad del día, pasar al siguiente
                if (remainingCapacityToday <= 0 && remainingHours > 0) {
                    currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                    remainingCapacityToday = 0.0
                }
            }
        }
    }
    
    // ...
}
```

**Beneficios:**
- ✅ Mejor aprovechamiento de tiempo
- ✅ Fechas de fin más tempranas
- ✅ Menos días de trabajo

**Ejemplo:**
```
Antes:
- Día 1: 4h (Tarea A) → 4h desperdiciadas
- Día 2: 4h (Tarea B) → 4h desperdiciadas

Después:
- Día 1: 4h (Tarea A) + 4h (Tarea B) → 0h desperdiciadas
```

---

### ✅ MEJORA 5: Manejo de tareas completadas

**Objetivo:** Limpiar bloques de tareas completadas.

**Implementación:**

```kotlin
// domain/usecases/PlanningUseCases.kt

fun generateSchedule(...): Result<Workspace> {
    // ...
    
    // 1. Filtrar tareas asignadas y no completadas
    val assignedTasks = workspace.tasks.filter { task ->
        task.assigneeId != null && task.status != "completed"
    }
    
    // ✅ 2. Limpiar bloques de tareas completadas
    val validTaskIds = assignedTasks.map { it.id }.toSet()
    val existingBlocks = workspace.planning.scheduleBlocks.filter { block ->
        block.taskId in validTaskIds
    }
    
    println("🗑️ Limpiados ${workspace.planning.scheduleBlocks.size - existingBlocks.size} bloques de tareas completadas")
    
    // 3. Generar schedule nuevo (solo para tareas activas)
    // ...
}
```

**Beneficios:**
- ✅ Planning solo muestra tareas activas
- ✅ No hay bloques de tareas completadas

---

### ✅ MEJORA 6: ID generation determinista

**Objetivo:** IDs reproducibles y sin recursión.

**Implementación:**

```kotlin
// domain/usecases/PlanningUseCases.kt

private var blockIdCounter = 0

private fun generateScheduleBlockId(scheduleBlocks: List<ScheduleBlock>): String {
    blockIdCounter++
    val timestamp = Clock.System.now().toEpochMilliseconds()
    return "sb_${timestamp}_${blockIdCounter}"
}

// ✅ Resetear contador al inicio de generateSchedule
fun generateSchedule(...): Result<Workspace> {
    blockIdCounter = 0 // ✅ Reset
    val scheduleBlocks = mutableListOf<ScheduleBlock>()
    // ...
}
```

**Beneficios:**
- ✅ Sin recursión
- ✅ IDs únicos garantizados
- ✅ Más fácil de testear

---

## 3. PLAN DE IMPLEMENTACIÓN

### Fase 1: Limpieza y validación (CRÍTICO)

1. ✅ Implementar `cleanOrphanBlocks()`
2. ✅ Implementar `validatePlanningIntegrity()`
3. ✅ Llamar `cleanOrphanBlocks()` al inicio de `generateSchedule()`
4. ✅ Añadir tests de validación

**Tiempo:** 1 hora  
**Riesgo:** BAJO

---

### Fase 2: Auto-recalculo (CRÍTICO)

5. ✅ Añadir auto-recalculo en `updateTask()`
6. ✅ Añadir auto-recalculo en `deleteTask()`
7. ✅ Añadir auto-recalculo en `assignTask()`
8. ✅ Añadir auto-recalculo en `updatePerson()` (si cambia hoursPerDay)
9. ✅ Añadir indicador de "Recalculando..." en UI

**Tiempo:** 2 horas  
**Riesgo:** MEDIO (puede ser lento)

---

### Fase 3: Optimizaciones (ALTO)

10. ✅ Implementar distribución optimizada (capacidad residual)
11. ✅ Implementar limpieza de tareas completadas
12. ✅ Mejorar ID generation (sin recursión)
13. ✅ Añadir tests de optimización

**Tiempo:** 1.5 horas  
**Riesgo:** BAJO

---

### Fase 4: Tests y validación (OBLIGATORIO)

14. ✅ Test: hoursPerDay cambia → recalcula
15. ✅ Test: priority cambia → recalcula
16. ✅ Test: tarea se elimina → limpia bloques
17. ✅ Test: tarea se reasigna → mueve bloques
18. ✅ Test: costHours cambia → recalcula
19. ✅ Test: doneHours cambia → recalcula
20. ✅ Test: no hay bloques huérfanos
21. ✅ Test: integridad referencial

**Tiempo:** 2 horas  
**Riesgo:** BAJO

---

## 4. RESUMEN DE MEJORAS

| Mejora | Prioridad | Impacto | Complejidad | Tiempo |
|--------|-----------|---------|-------------|--------|
| Auto-recalculo | 🔴 CRÍTICO | ALTO | MEDIA | 2h |
| Limpieza huérfanos | 🔴 CRÍTICO | ALTO | BAJA | 1h |
| Validación integridad | 🟠 ALTO | MEDIO | BAJA | 0.5h |
| Optimizar distribución | 🟠 ALTO | MEDIO | MEDIA | 1h |
| Manejo completadas | 🟡 MEDIO | BAJO | BAJA | 0.5h |
| ID determinista | 🟡 MEDIO | BAJO | BAJA | 0.5h |
| Tests | 🔴 CRÍTICO | ALTO | MEDIA | 2h |

**Total estimado:** 7.5 horas

---

## 5. CRITERIOS DE ÉXITO

1. ✅ **No hay bloques huérfanos** después de cualquier operación
2. ✅ **Planning se recalcula automáticamente** cuando cambian datos relevantes
3. ✅ **Integridad referencial** validada y garantizada
4. ✅ **Distribución optimizada** (capacidad residual aprovechada)
5. ✅ **Tests pasan** para todos los escenarios problemáticos
6. ✅ **Comportamiento externo** mejorado (más robusto)
7. ✅ **Compatibilidad** con data-schema.json mantenida

---

## 6. RIESGOS Y MITIGACIONES

### Riesgo 1: Recalculo lento en UI

**Mitigación:**
- Ejecutar en coroutine background
- Mostrar indicador de progreso
- Cachear si workspace no cambió

### Riesgo 2: Recalculo demasiado frecuente

**Mitigación:**
- Debounce (esperar 500ms antes de recalcular)
- Solo recalcular si cambió algo relevante
- Opción de desactivar auto-recalculo (avanzado)

### Riesgo 3: Romper funcionalidad existente

**Mitigación:**
- Tests exhaustivos
- Cambios incrementales
- Validar con tests actuales

---

**Estado:** ⏸️ Pendiente de implementación  
**Recomendación:** Implementar Fases 1-3, luego validar con Fase 4

