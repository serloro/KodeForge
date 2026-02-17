# T5 (Parcial) - Base de Tareas y Asignación - Diseño

**Alcance de esta implementación:**
- ✅ CRUD de tareas (parte inicial de T5)
- ✅ Asignación de tarea a persona
- ✅ Validación costHours obligatorio si hay asignación
- ❌ Scheduler (siguiente fase)
- ❌ Calendario visual (siguiente fase)
- ❌ Detalle persona con calendario (siguiente fase)

---

## 📋 ANÁLISIS DE ESPECIFICACIONES

### **spec.md - Reglas:**
```
"al asignar tarea → se indica costHours"
"el sistema calcula duración y planifica en calendario"
```

### **tasks.md - T5:**
```
"CRUD tareas (title, costHours, status, priority)"
"Asignar tarea a persona exige costHours"
"Scheduler secuencial por persona (consume hoursPerDay por día)" ← Siguiente fase
"Detalle persona: resumen + calendario" ← Siguiente fase
```

### **Modelo Task existente:**
```kotlin
data class Task(
    val id: String,              // REQUIRED
    val projectId: String,       // REQUIRED - tarea pertenece a un proyecto
    val title: String,           // REQUIRED
    val description: String? = null, // OPTIONAL
    val status: String = "todo", // DEFAULT "todo" (todo, in_progress, completed)
    val priority: Int = 0,       // DEFAULT 0 (menor = más prioritario)
    val costHours: Double,       // REQUIRED (especialmente si assigneeId != null)
    val doneHours: Double = 0.0, // DEFAULT 0.0
    val assigneeId: String? = null, // OPTIONAL - ID de persona asignada
    val createdAt: String,       // REQUIRED - ISO 8601
    val updatedAt: String        // REQUIRED - ISO 8601
)
```

---

## 🏗️ ARQUITECTURA DE IMPLEMENTACIÓN

### **1. Validador**
```
TaskValidator.kt
├─ validateCreate(title, costHours, projectId): Result<Unit>
│  ├─ title: no vacío, trim, max 200 chars
│  ├─ costHours: > 0, max 1000 (razonable)
│  └─ projectId: no vacío, existe en workspace
│
├─ validateUpdate(fields...): Result<Unit>
│  └─ Similar a create pero campos opcionales
│
└─ validateAssignment(task, personId, costHours): Result<Unit>
   ├─ personId: existe en workspace
   ├─ costHours: > 0 (OBLIGATORIO si se asigna)
   └─ persona: active = true
```

### **2. Use Cases**
```
TaskUseCases.kt
├─ createTask(projectId, title, costHours, description?, status?, priority?)
│  ├─ Valida datos
│  ├─ Genera ID único (task_{timestamp}_{random})
│  ├─ Genera createdAt/updatedAt (ISO 8601)
│  ├─ Crea Task (sin asignar)
│  └─ Actualiza workspace.tasks
│
├─ updateTask(taskId, title?, costHours?, description?, status?, priority?)
│  ├─ Valida datos
│  ├─ Busca tarea existente
│  ├─ Actualiza campos modificados + updatedAt
│  └─ Actualiza workspace.tasks
│
├─ deleteTask(taskId)
│  ├─ Busca tarea existente
│  ├─ Elimina de workspace.tasks
│  └─ (Futuro: recalcular scheduler si estaba asignada)
│
├─ assignTaskToPerson(taskId, personId, costHours)
│  ├─ Valida persona existe y está activa
│  ├─ Valida costHours > 0 (OBLIGATORIO)
│  ├─ Actualiza task.assigneeId + task.costHours
│  ├─ Actualiza updatedAt
│  └─ (Futuro: recalcular scheduler)
│
├─ unassignTask(taskId)
│  ├─ Busca tarea existente
│  ├─ task.assigneeId = null
│  └─ (Futuro: recalcular scheduler)
│
└─ getTasksByProject(projectId): List<Task>
   └─ Filtrar workspace.tasks por projectId
```

### **3. UI - Gestión de Tareas por Proyecto**
```
ManageTasksScreen.kt (similar a ManagePeopleScreen)
├─ Lista de tareas del proyecto
│  ├─ Header con projectName
│  ├─ Botón "+ Crear Tarea"
│  ├─ Cada item:
│  │  ├─ Título + descripción
│  │  ├─ Status badge (todo/in_progress/completed)
│  │  ├─ Priority badge
│  │  ├─ Cost hours badge
│  │  ├─ Assignee (si hay)
│  │  ├─ Botón "Asignar/Reasignar"
│  │  ├─ Botón "Editar"
│  │  └─ Botón "Eliminar"
│  └─ Empty state si no hay tareas
│
├─ Modal Create/Edit Task
│  ├─ Formulario:
│  │  ├─ title (REQUIRED)
│  │  ├─ description (opcional, textarea)
│  │  ├─ costHours (REQUIRED, > 0)
│  │  ├─ status (select: todo, in_progress, completed)
│  │  ├─ priority (number input, ej: 1, 2, 3...)
│  │  └─ assigneeId (select personas, opcional)
│  └─ Validación en tiempo real
│
└─ Modal Assign Task
   ├─ Select persona (obligatorio)
   ├─ Input costHours (obligatorio si no lo tiene)
   └─ Botón "Asignar"
```

---

## 📁 ARCHIVOS A CREAR/MODIFICAR

### **Nuevos:**
1. `src/commonMain/kotlin/com/kodeforge/domain/validation/TaskValidator.kt`
2. `src/commonMain/kotlin/com/kodeforge/domain/usecases/TaskUseCases.kt`
3. `src/commonMain/kotlin/com/kodeforge/ui/components/TaskForm.kt`
4. `src/commonMain/kotlin/com/kodeforge/ui/components/TaskListItem.kt`
5. `src/commonMain/kotlin/com/kodeforge/ui/components/AssignTaskDialog.kt`
6. `src/commonMain/kotlin/com/kodeforge/ui/screens/ManageTasksScreen.kt`

### **Modificados:**
1. `src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`
   - Añadir navegación a ManageTasksScreen al seleccionar proyecto
   - Por ahora: clic en proyecto → ManageTasksScreen (en T6 será modo proyecto completo)

---

## 🎨 UI LAYOUT

### **ManageTasksScreen:**
```
┌────────────────────────────────────────────┐
│ ← Proyecto: Cloud Scale UI  [+ Nueva Tarea]│
├────────────────────────────────────────────┤
│                                            │
│ ┌────────────────────────────────────────┐ │
│ │ [1] Implementar login                  │ │
│ │     8h · 🟡 In Progress                │ │
│ │     👤 Basso7          [↻] [✏️] [🗑️]   │ │
│ └────────────────────────────────────────┘ │
│                                            │
│ ┌────────────────────────────────────────┐ │
│ │ [2] Diseñar UI dashboard               │ │
│ │     12h · ⚪ Todo                      │ │
│ │     Sin asignar        [👤] [✏️] [🗑️]  │ │
│ └────────────────────────────────────────┘ │
│                                            │
└────────────────────────────────────────────┘
```

### **Modal Create/Edit Task:**
```
┌─────────────────────────────────┐
│ Crear Tarea               [×]   │
├─────────────────────────────────┤
│ Título *                        │
│ [___________________________]   │
│                                 │
│ Descripción (opcional)          │
│ [___________________________]   │
│ [___________________________]   │
│                                 │
│ Costo en horas *                │
│ [_______] (> 0)                 │
│                                 │
│ Estado                          │
│ [Todo ▼]                        │
│                                 │
│ Prioridad                       │
│ [_______] (menor = prioritario) │
│                                 │
│ Asignar a (opcional)            │
│ [Sin asignar ▼]                 │
│                                 │
│     [Cancelar]  [Crear]         │
└─────────────────────────────────┘
```

### **Modal Assign Task:**
```
┌─────────────────────────────────┐
│ Asignar Tarea             [×]   │
├─────────────────────────────────┤
│ Tarea: Implementar login        │
│ Costo: 8 horas                  │
│                                 │
│ Asignar a: *                    │
│ [Seleccionar persona... ▼]      │
│                                 │
│ • Basso7 (8h/día disponibles)   │
│ • Blanco J (6h/día disponibles) │
│ • Bocera J (7h/día disponibles) │
│                                 │
│     [Cancelar]  [Asignar]       │
└─────────────────────────────────┘
```

---

## ✅ VALIDACIONES

| Campo | Validación | Mensaje Error |
|-------|------------|---------------|
| title | No vacío | "El título es obligatorio" |
| | Max 200 chars | "Título muy largo (max 200)" |
| costHours | > 0 | "Debe ser mayor a 0" |
| | Max 1000 | "Valor excesivo (max 1000h)" |
| | Numérico válido | "Valor numérico inválido" |
| projectId | Existe en workspace | "Proyecto no encontrado" |
| assigneeId | Existe en workspace | "Persona no encontrada" |
| | Persona activa | "Persona inactiva" |
| | costHours > 0 si asignada | "costHours obligatorio si hay asignación" |
| status | Valor válido | "Estado inválido" |
| priority | >= 0 | "Prioridad debe ser >= 0" |

---

## 🔄 FLUJO DE DATOS

### **Crear Tarea:**
```
UI Form → TaskUseCases.createTask()
       → TaskValidator.validateCreate()
       → Genera ID (task_1708100234567_8901)
       → Genera createdAt/updatedAt (ISO 8601)
       → Crea Task
       → workspace.copy(tasks = tasks + newTask)
       → onWorkspaceUpdate(newWorkspace)
```

### **Asignar Tarea a Persona:**
```
UI Dialog → TaskUseCases.assignTaskToPerson()
         → TaskValidator.validateAssignment()
         → Verifica persona existe y active = true
         → Verifica costHours > 0
         → task.copy(assigneeId = personId, costHours = costHours)
         → workspace.copy(tasks = tasksUpdated)
         → onWorkspaceUpdate(newWorkspace)
         → (Futuro: recalcular scheduler)
```

### **Editar Tarea:**
```
UI Form → TaskUseCases.updateTask()
       → TaskValidator.validateUpdate()
       → Actualiza campos modificados
       → Actualiza updatedAt
       → workspace.copy(tasks = tasksUpdated)
       → (Futuro: recalcular scheduler si cambió costHours o assignee)
```

---

## 🚫 FUERA DE ALCANCE (Esta Fase)

- ❌ Scheduler (cálculo de distribución en días) → Siguiente fase
- ❌ Calendario visual de persona → Siguiente fase
- ❌ Detalle persona con resumen de tareas → Siguiente fase
- ❌ Timeline de proyecto → T6
- ❌ Modo proyecto completo → T6
- ❌ Reordenar prioridades drag & drop → Futuro

---

## 📊 CRITERIOS DE VALIDACIÓN

| Criterio | Estado |
|----------|--------|
| CRUD tareas completo | ✅ Implementar |
| Campo title obligatorio | ✅ Implementar |
| Campo costHours obligatorio | ✅ Implementar |
| Campo status (todo/in_progress/completed) | ✅ Implementar |
| Campo priority | ✅ Implementar |
| Asignar tarea a persona | ✅ Implementar |
| costHours obligatorio si hay asignación | ✅ Implementar |
| Persistencia en workspace JSON | ✅ Implementar |
| Sin scheduler (siguiente fase) | ✅ Correcto |
| Sin calendario visual (siguiente fase) | ✅ Correcto |

---

## ⏭️ PREPARACIÓN PARA SCHEDULER (Siguiente Fase)

Esta implementación deja preparado para el scheduler:
- ✅ `Task.costHours` ya definido
- ✅ `Task.assigneeId` ya definido
- ✅ `Task.priority` ya definido (para orden secuencial)
- ✅ `Person.hoursPerDay` ya disponible
- ✅ Solo faltará implementar el algoritmo de distribución

---

**Siguiente paso:** Implementación del código.

