# T6B - Tareas por Proyecto + Asignación - Diseño

**Objetivo:** Permitir crear tareas asociadas a un proyecto y asignarlas solo a miembros del proyecto.

**Alcance:** SOLO gestión de tareas por proyecto con validaciones. NO timeline visual.

---

## 📋 REQUISITOS

### **Crear Tareas por Proyecto:**
- Crear tarea asociada a `projectId`
- Campos: title, description, costHours, priority, status, assigneeId
- Validaciones específicas del proyecto

### **Asignación Restringida:**
- Solo se puede asignar a miembros del proyecto
- `costHours > 0` obligatorio si se asigna a persona
- Validar en create y update

### **Persistencia:**
- Guardar en `workspace.tasks`
- Mantener relación `task.projectId` → `project.id`
- Mantener relación `task.assigneeId` → `person.id` (miembro del proyecto)

---

## 🏗️ ARQUITECTURA

### **Validador Actualizado:**
```
TaskValidator
├── validate(task: Task, project: Project?): List<String>
│   ├── Validaciones existentes (title, costHours, etc.)
│   ├── NUEVA: Si assigneeId != null → costHours > 0
│   └── NUEVA: Si project != null → assigneeId debe estar en project.members
```

### **Use Cases Actualizados:**
```
TaskUseCases
├── createTask(..., projectId) - MODIFICAR validación
├── updateTask(...) - MODIFICAR validación
├── assignTask(..., projectId) - MODIFICAR validación
└── getTasksByProject(workspace, projectId) - YA EXISTE
```

### **UI - Pantalla Proyecto:**
```
ManageTasksScreen (YA EXISTE)
├── MODIFICAR: Filtrar personas → solo miembros del proyecto
├── MODIFICAR: Validación al asignar
└── MODIFICAR: Mensajes de error específicos
```

---

## 📊 VALIDACIONES

### **Regla 1: costHours obligatorio si hay assignee**

| Caso | costHours | assigneeId | Resultado |
|------|-----------|------------|-----------|
| Tarea sin asignar | 0 | null | ✅ OK |
| Tarea sin asignar | 10 | null | ✅ OK |
| Tarea asignada | 0 | "p_123" | ❌ ERROR |
| Tarea asignada | 10 | "p_123" | ✅ OK |

**Mensaje de error:**
```
"El costo en horas debe ser mayor que cero si la tarea está asignada."
```

### **Regla 2: assignee debe ser miembro del proyecto**

| Caso | assigneeId | project.members | Resultado |
|------|------------|-----------------|-----------|
| Sin asignar | null | [...] | ✅ OK |
| Miembro válido | "p_123" | ["p_123", "p_456"] | ✅ OK |
| No miembro | "p_789" | ["p_123", "p_456"] | ❌ ERROR |

**Mensaje de error:**
```
"La persona asignada debe ser miembro del proyecto."
```

---

## 🔄 FLUJO DE DATOS

### **Crear Tarea en Proyecto:**
```
ManageTasksScreen (proyecto seleccionado)
       ↓
TaskForm (projectId fijo)
       ↓
Seleccionar assignee → SOLO miembros del proyecto
       ↓
TaskUseCases.createTask(workspace, projectId, ...)
       ↓
TaskValidator.validate(task, project) ✅
       ↓
Validar: assigneeId en project.members ✅
       ↓
Validar: costHours > 0 si assigneeId != null ✅
       ↓
workspace.copy(tasks = tasks + newTask)
       ↓
WorkspaceRepository.save()
```

### **Asignar Tarea Existente:**
```
ManageTasksScreen
       ↓
AssignTaskDialog (solo miembros del proyecto)
       ↓
TaskUseCases.assignTask(workspace, taskId, assigneeId, costHours, projectId)
       ↓
Validar: assigneeId en project.members ✅
       ↓
Validar: costHours > 0 ✅
       ↓
workspace.copy(tasks = tasksUpdated)
```

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/validation/TaskValidator.kt`**
   - Añadir parámetro `project: Project?` a `validate()`
   - Validar `assigneeId` en `project.members`
   - Validar `costHours > 0` si `assigneeId != null`

2. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/TaskUseCases.kt`**
   - Modificar `createTask()` para validar con proyecto
   - Modificar `updateTask()` para validar con proyecto
   - Modificar `assignTask()` para validar con proyecto

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/TaskForm.kt`**
   - Filtrar personas → solo `project.members`
   - Mostrar solo miembros del proyecto en dropdown

4. **`src/commonMain/kotlin/com/kodeforge/ui/components/AssignTaskDialog.kt`**
   - Filtrar personas → solo `project.members`
   - Validar `costHours > 0`

5. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ManageTasksScreen.kt`**
   - Pasar `project` a formularios
   - Filtrar personas disponibles

---

## 🧪 TESTS

### **Test 1: costHours obligatorio si hay assignee**
```kotlin
@Test
fun `assignTask - costHours must be greater than 0`() = runBlocking {
    val project = Project("proj1", "Project 1", members = listOf("p1"), ...)
    val person = Person("p1", "Alice", hoursPerDay = 8.0)
    val task = Task("t1", "proj1", "Task A", costHours = 0.0, ...)
    val workspace = createTestWorkspace(
        projects = listOf(project),
        people = listOf(person),
        tasks = listOf(task)
    )
    
    val (_, errors) = taskUseCases.assignTask(
        workspace, "t1", "p1", costHours = 0.0, projectId = "proj1"
    )
    
    assertTrue(errors.isNotEmpty())
    assertTrue(errors.any { it.contains("costo en horas") })
}
```

### **Test 2: assignee debe ser miembro del proyecto**
```kotlin
@Test
fun `assignTask - assignee must be project member`() = runBlocking {
    val project = Project("proj1", "Project 1", members = listOf("p1"), ...)
    val person1 = Person("p1", "Alice", hoursPerDay = 8.0)
    val person2 = Person("p2", "Bob", hoursPerDay = 8.0) // NO miembro
    val task = Task("t1", "proj1", "Task A", costHours = 10.0, ...)
    val workspace = createTestWorkspace(
        projects = listOf(project),
        people = listOf(person1, person2),
        tasks = listOf(task)
    )
    
    val (_, errors) = taskUseCases.assignTask(
        workspace, "t1", "p2", costHours = 10.0, projectId = "proj1"
    )
    
    assertTrue(errors.isNotEmpty())
    assertTrue(errors.any { it.contains("miembro del proyecto") })
}
```

### **Test 3: asignación válida**
```kotlin
@Test
fun `assignTask - valid assignment to project member`() = runBlocking {
    val project = Project("proj1", "Project 1", members = listOf("p1"), ...)
    val person = Person("p1", "Alice", hoursPerDay = 8.0)
    val task = Task("t1", "proj1", "Task A", costHours = 10.0, ...)
    val workspace = createTestWorkspace(
        projects = listOf(project),
        people = listOf(person),
        tasks = listOf(task)
    )
    
    val (updatedWorkspace, errors) = taskUseCases.assignTask(
        workspace, "t1", "p1", costHours = 10.0, projectId = "proj1"
    )
    
    assertTrue(errors.isEmpty())
    assertEquals("p1", updatedWorkspace.tasks.first().assigneeId)
}
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Crear tarea asociada a proyecto | `createTask(projectId)` |
| Asignar solo a miembros | Validación en `TaskValidator` |
| costHours > 0 si asignada | Validación en `TaskValidator` |
| Filtrar personas en UI | Solo `project.members` en dropdowns |
| Persistencia JSON | `workspace.tasks` |
| Tests de validación | 3 tests mínimos |
| NO timeline visual | Correcto, no implementar |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Modificar `TaskValidator.validate()` - añadir parámetro `project`
2. ✅ Modificar `TaskUseCases` - validar con proyecto
3. ✅ Modificar `TaskForm` - filtrar personas
4. ✅ Modificar `AssignTaskDialog` - filtrar personas
5. ✅ Modificar `ManageTasksScreen` - pasar proyecto
6. ✅ Crear tests de validación
7. ✅ Compilar y validar

---

**Tiempo estimado:** 1-2 horas  
**Complejidad:** Media  
**Dependencias:** TaskValidator, TaskUseCases, UI existente

---

*Diseño completado - Listo para implementación*

