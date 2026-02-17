# Vista Detalle Persona - Diseño

**Objetivo:** Mostrar resumen de tareas y calendario/timeline horizontal para una persona.

---

## 📋 REQUISITOS (spec.md)

- ✅ "resumen de tareas + trabajo realizado"
- ✅ "calendario con distribución automática de tareas (se ve carga y fecha fin)"

---

## 🎨 DISEÑO UI

### **Layout:**
```
┌────────────────────────────────────────────────────────────────┐
│ ← Basso7 (Dev)                                                 │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│ ┌─────────────────────────────────────────────────────────┐   │
│ │ RESUMEN                                                  │   │
│ │                                                          │   │
│ │ Tareas Activas: 2                                       │   │
│ │ Horas Planificadas: 16h                                 │   │
│ │ Horas Realizadas: 4h                                    │   │
│ │ Progreso: 25%                                           │   │
│ │ Fecha Fin Estimada: 2026-02-20                          │   │
│ └─────────────────────────────────────────────────────────┘   │
│                                                                │
│ ┌─────────────────────────────────────────────────────────┐   │
│ │ CALENDARIO                                               │   │
│ │                                                          │   │
│ │ Lun 17  Mar 18  Mié 19  Jue 20  Vie 21  Lun 24  ...    │   │
│ │   │       │       │       │       │       │             │   │
│ │   ├───────┤       │       │       │       │             │   │
│ │   │ Login │       │       │       │       │             │   │
│ │   │  6h   │       │       │       │       │             │   │
│ │   └───────┘       │       │       │       │             │   │
│ │           ├───────┴───────┴───────┤       │             │   │
│ │           │    Dashboard UI       │       │             │   │
│ │           │        10h            │       │             │   │
│ │           └───────────────────────┘       │             │   │
│ │                                   ▼ HOY                  │   │
│ └─────────────────────────────────────────────────────────┘   │
│                                                                │
│ ┌─────────────────────────────────────────────────────────┐   │
│ │ TAREAS ACTIVAS                                           │   │
│ │                                                          │   │
│ │ [1] Implement login screen                              │   │
│ │     🟡 En Progreso · 10h (4h hechas)                    │   │
│ │                                                          │   │
│ │ [2] Design dashboard UI                                 │   │
│ │     ⚪ Por Hacer · 10h (0h hechas)                      │   │
│ └─────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ COMPONENTES

### **1. PersonDetailScreen.kt**
- Header con nombre + rol + botón volver
- Card Resumen (KPIs)
- Card Calendario (timeline horizontal)
- Card Lista de tareas activas

### **2. PersonSummaryCard.kt**
- Tareas activas (count)
- Horas planificadas (sum scheduleBlocks)
- Horas realizadas (sum doneHours)
- Progreso (%)
- Fecha fin estimada

### **3. PersonCalendar.kt**
- Timeline horizontal por días
- Bloques de tareas (basados en scheduleBlocks)
- Línea vertical "Hoy"
- Scroll horizontal
- Colores por tarea/proyecto
- Tooltip con info al hover

### **4. TaskListCard.kt**
- Lista de tareas activas de la persona
- Badge status + horas
- Sin botones de acción (solo lectura)

---

## 📊 DATOS NECESARIOS

### **Entrada:**
- `workspace` - Workspace completo
- `personId` - ID de la persona

### **Cálculos:**
```kotlin
// Tareas activas
val activeTasks = workspace.tasks.filter { 
    it.assigneeId == personId && it.status != "completed" 
}

// Horas planificadas
val plannedHours = workspace.planning.scheduleBlocks
    .filter { it.personId == personId }
    .sumOf { it.hoursPlanned }

// Horas realizadas
val doneHours = activeTasks.sumOf { it.doneHours }

// Progreso
val totalHours = activeTasks.sumOf { it.costHours }
val progress = if (totalHours > 0) (doneHours / totalHours * 100).toInt() else 0

// Fecha fin estimada
val endDate = planningUseCases.getEstimatedEndDate(workspace, personId)

// Bloques por fecha
val blocksByDate = workspace.planning.scheduleBlocks
    .filter { it.personId == personId }
    .groupBy { it.date }
    .toSortedMap()
```

---

## 🎨 CALENDARIO - DISEÑO DETALLADO

### **Timeline Horizontal:**
```
┌─────────────────────────────────────────────────────────────┐
│ Lun 17    Mar 18    Mié 19    Jue 20    Vie 21    Lun 24   │
│   │         │         │         │         │         │       │
│   ├─────────┤         │         │         │         │       │
│   │ Login   │         │         │         │         │       │
│   │  6h     │         │         │         │         │       │
│   └─────────┘         │         │         │         │       │
│             ├─────────┴─────────┴─────────┤         │       │
│             │    Dashboard UI             │         │       │
│             │        10h                  │         │       │
│             └─────────────────────────────┘         │       │
│                                     ▼ HOY           │       │
└─────────────────────────────────────────────────────────────┘
```

### **Características:**
- Ancho por día: 120dp
- Alto de bloque: 60dp
- Padding entre bloques: 8dp
- Scroll horizontal si hay muchos días
- Línea vertical "Hoy" en rojo/azul
- Bloques con color según proyecto
- Texto: título tarea + horas

### **Colores por proyecto:**
```kotlin
val projectColors = listOf(
    Color(0xFF2196F3), // Azul
    Color(0xFF4CAF50), // Verde
    Color(0xFFFF9800), // Naranja
    Color(0xFF9C27B0), // Púrpura
    Color(0xFF00BCD4), // Cian
    Color(0xFFE91E63)  // Rosa
)

fun getProjectColor(projectId: String): Color {
    val index = projectId.hashCode() % projectColors.size
    return projectColors[index.absoluteValue]
}
```

---

## 🔄 NAVEGACIÓN

### **Desde HomeScreen:**
```kotlin
// En Sidebar, onClick persona
onPersonClick = { person ->
    currentScreen = Screen.PersonDetail(person)
}
```

### **Desde PersonDetailScreen:**
```kotlin
// Botón "Volver"
onBack = {
    currentScreen = Screen.Home
}
```

---

## 📁 ARCHIVOS A CREAR

1. `src/commonMain/kotlin/com/kodeforge/ui/screens/PersonDetailScreen.kt`
2. `src/commonMain/kotlin/com/kodeforge/ui/components/PersonSummaryCard.kt`
3. `src/commonMain/kotlin/com/kodeforge/ui/components/PersonCalendar.kt`
4. `src/commonMain/kotlin/com/kodeforge/ui/components/TaskListCard.kt`

---

## 📁 ARCHIVOS A MODIFICAR

1. `src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`
   - Añadir Screen.PersonDetail(person)
   - onClick persona → navegar a detalle

---

## ✅ VALIDACIONES

| Validación | Implementación |
|------------|----------------|
| Persona existe | Buscar en workspace.people |
| Tiene tareas asignadas | Filter tasks by assigneeId |
| Tiene schedule generado | Filter scheduleBlocks by personId |
| Fecha "Hoy" correcta | LocalDate.now() |
| Scroll horizontal funciona | LazyRow |

---

**Siguiente paso:** Implementación del código.

