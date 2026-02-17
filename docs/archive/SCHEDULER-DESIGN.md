# Scheduler Secuencial MVP - Diseño

**Objetivo:** Distribuir tareas asignadas a personas en un calendario, consumiendo `hoursPerDay` por día.

---

## 📋 ALGORITMO SECUENCIAL

### **Entrada:**
- `workspace.tasks` - Tareas con `assigneeId` != null
- `workspace.people` - Personas con `hoursPerDay`
- `startDate` - Fecha de inicio (default: hoy)
- `workingDays` - Días laborables (default: [1,2,3,4,5] = Lun-Vie)

### **Salida:**
- `workspace.planning.scheduleBlocks` - Bloques de planificación por persona/día

---

## 🔄 ALGORITMO PASO A PASO

### **1. Agrupar tareas por persona:**
```
tasksPerPerson = workspace.tasks
    .filter { it.assigneeId != null && it.status != "completed" }
    .groupBy { it.assigneeId }
```

### **2. Para cada persona:**
```
for (personId, tasks) in tasksPerPerson:
    person = workspace.people.find { it.id == personId }
    
    # Ordenar tareas por prioridad (menor = más prioritario)
    sortedTasks = tasks.sortedBy { it.priority }
    
    # Calcular horas pendientes por tarea
    pendingTasks = sortedTasks.map { task ->
        (task, pendingHours = task.costHours - task.doneHours)
    }.filter { it.pendingHours > 0 }
    
    # Distribuir en días
    currentDate = startDate
    
    for (task, pendingHours) in pendingTasks:
        remainingHours = pendingHours
        
        while (remainingHours > 0):
            # Saltar fines de semana (si no es día laborable)
            while (currentDate.dayOfWeek not in workingDays):
                currentDate = currentDate + 1 day
            
            # Calcular horas a asignar este día
            hoursThisDay = min(remainingHours, person.hoursPerDay)
            
            # Crear ScheduleBlock
            scheduleBlocks.add(
                ScheduleBlock(
                    id = "sb_{timestamp}_{random}",
                    personId = personId,
                    taskId = task.id,
                    projectId = task.projectId,
                    date = currentDate (YYYY-MM-DD),
                    hoursPlanned = hoursThisDay
                )
            )
            
            remainingHours -= hoursThisDay
            
            # Si quedan horas, pasar al siguiente día
            if (remainingHours > 0):
                currentDate = currentDate + 1 day
        
        # Siguiente tarea empieza en el día actual (sin saltar)
```

### **3. Generar Planning:**
```
planning = Planning(
    generatedAt = now (ISO 8601),
    strategy = PlanningStrategy(
        type = "sequential",
        splitAcrossDays = true
    ),
    scheduleBlocks = scheduleBlocks
)

workspace = workspace.copy(planning = planning)
```

---

## 📊 EJEMPLO CON data-schema.json

### **Datos de entrada:**

**Personas:**
- `p_basso7`: 6h/día
- `p_blancoJ`: 8h/día
- `p_boceraJ`: 4h/día

**Tareas:**
1. `t_001` (priority=1): Basso7, 10h costo - 4h hechas = **6h pendientes**
2. `t_002` (priority=2): BlancoJ, 6h costo - 0h hechas = **6h pendientes**
3. `t_003` (priority=3): BoceraJ, 4h costo - 0h hechas = **4h pendientes**

**Fecha inicio:** 2026-02-17 (Lunes)

### **Cálculo para p_basso7 (6h/día):**
```
Tarea t_001: 6h pendientes
- 2026-02-17 (Lun): 6h → Completa en 1 día
```

**ScheduleBlocks generados:**
```json
{
  "id": "sb_001",
  "personId": "p_basso7",
  "taskId": "t_001",
  "projectId": "pr_cloudScale",
  "date": "2026-02-17",
  "hoursPlanned": 6
}
```

### **Cálculo para p_blancoJ (8h/día):**
```
Tarea t_002: 6h pendientes
- 2026-02-17 (Lun): 6h → Completa en 1 día
```

**ScheduleBlocks generados:**
```json
{
  "id": "sb_002",
  "personId": "p_blancoJ",
  "taskId": "t_002",
  "projectId": "pr_cloudScale",
  "date": "2026-02-17",
  "hoursPlanned": 6
}
```

### **Cálculo para p_boceraJ (4h/día):**
```
Tarea t_003: 4h pendientes
- 2026-02-17 (Lun): 4h → Completa en 1 día
```

**ScheduleBlocks generados:**
```json
{
  "id": "sb_003",
  "personId": "p_boceraJ",
  "taskId": "t_003",
  "projectId": "pr_cloudScale",
  "date": "2026-02-17",
  "hoursPlanned": 4
}
```

---

## 📊 EJEMPLO CON TAREA QUE SE DIVIDE

**Escenario:** Persona con 6h/día, tarea de 20h pendientes

```
Tarea t_004: 20h pendientes, priority=1
Persona: 6h/día
Fecha inicio: 2026-02-17 (Lunes)

Distribución:
- 2026-02-17 (Lun): 6h → Quedan 14h
- 2026-02-18 (Mar): 6h → Quedan 8h
- 2026-02-19 (Mié): 6h → Quedan 2h
- 2026-02-20 (Jue): 2h → Completa

ScheduleBlocks:
[
  { date: "2026-02-17", hoursPlanned: 6 },
  { date: "2026-02-18", hoursPlanned: 6 },
  { date: "2026-02-19", hoursPlanned: 6 },
  { date: "2026-02-20", hoursPlanned: 2 }
]
```

---

## 📊 EJEMPLO CON MÚLTIPLES TAREAS

**Escenario:** Persona con 8h/día, 3 tareas

```
Persona: 8h/día
Tareas (ordenadas por priority):
1. t_001 (priority=1): 10h pendientes
2. t_002 (priority=2): 5h pendientes
3. t_003 (priority=3): 12h pendientes

Fecha inicio: 2026-02-17 (Lunes)

Distribución:
Tarea t_001 (10h):
- 2026-02-17 (Lun): 8h → Quedan 2h
- 2026-02-18 (Mar): 2h → Completa

Tarea t_002 (5h):
- 2026-02-18 (Mar): 5h (ya usó 2h, quedan 6h disponibles) → Completa

Tarea t_003 (12h):
- 2026-02-18 (Mar): 1h (ya usó 7h) → Quedan 11h
- 2026-02-19 (Mié): 8h → Quedan 3h
- 2026-02-20 (Jue): 3h → Completa

ScheduleBlocks:
[
  { taskId: "t_001", date: "2026-02-17", hoursPlanned: 8 },
  { taskId: "t_001", date: "2026-02-18", hoursPlanned: 2 },
  { taskId: "t_002", date: "2026-02-18", hoursPlanned: 5 },
  { taskId: "t_003", date: "2026-02-18", hoursPlanned: 1 },
  { taskId: "t_003", date: "2026-02-19", hoursPlanned: 8 },
  { taskId: "t_003", date: "2026-02-20", hoursPlanned: 3 }
]
```

---

## 🚫 FUERA DE ALCANCE (MVP)

- ❌ Dependencias entre tareas (ej: tarea B empieza cuando A termina)
- ❌ Paralelización (múltiples tareas el mismo día)
- ❌ Priorización dinámica (cambios en tiempo real)
- ❌ Optimización por IA/ML
- ❌ Balanceo de carga entre personas
- ❌ Festivos/vacaciones (solo salta fines de semana)
- ❌ Horas parciales por día (ej: 0.5h)

---

## ✅ VALIDACIONES

| Validación | Implementación |
|------------|----------------|
| Solo tareas asignadas (assigneeId != null) | ✅ Filter |
| Solo tareas no completadas (status != "completed") | ✅ Filter |
| Persona existe y está activa | ✅ Verificar |
| hoursPerDay > 0 | ✅ Verificar |
| costHours - doneHours > 0 | ✅ Calcular pendientes |
| Saltar fines de semana | ✅ workingDays |
| Ordenar por priority | ✅ sortedBy |

---

## 📁 ARCHIVOS A CREAR

1. `src/commonMain/kotlin/com/kodeforge/domain/usecases/PlanningUseCases.kt`
   - `generateSchedule(workspace, startDate?): Result<Workspace>`
   - `clearSchedule(workspace): Result<Workspace>`

2. `src/jvmTest/kotlin/com/kodeforge/PlanningUseCasesTest.kt`
   - Test con data-schema.json
   - Test con tarea dividida
   - Test con múltiples tareas

---

**Siguiente paso:** Implementación del código.

