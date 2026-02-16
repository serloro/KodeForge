# T7B - Excedidos en Rojo (MVP) - Diseño

**Objetivo:** Resaltar en rojo personas excedidas en la vista proyecto.

**Alcance:** Detección de sobrecarga y resaltado visual en timeline.

---

## 📋 DEFINICIÓN DE "EXCEDIDO"

### **Criterio 1: Sobrecarga Diaria**

Una persona está **excedida en un día** si:

```
hoursPlannedInDay > person.hoursPerDay
```

**Ejemplo:**
- Persona: Alice, `hoursPerDay = 8.0`
- Día 2026-02-17: tiene 3 tareas planificadas
  - Tarea A: 4 horas
  - Tarea B: 3 horas
  - Tarea C: 2 horas
  - **Total: 9 horas > 8 horas → EXCEDIDA**

### **Criterio 2: Tareas Solapadas (Opcional)**

Dos tareas están **solapadas** si:
- Ambas tienen `assigneeId` igual
- Sus rangos de fechas se superponen
- Ambas están `in_progress` o `todo`

**Nota:** En el modelo actual, las tareas no tienen fechas explícitas de inicio/fin, solo `scheduleBlocks` por día. Por lo tanto, el criterio principal es **sobrecarga diaria**.

---

## 🎨 DISEÑO VISUAL

### **Resaltado en Timeline:**

```
Timeline del Proyecto
┌─────────────────────────────────────────────────────────┐
│        [Fechas]                                         │
│                    ↓                                    │
│                  Hoy                                    │
│                    │                                    │
│ 👤 Alice    ████████│████░░░░░░░░░░░░░░░░░░░░░░░░     │ ← Normal
│ 👤 Bob      ████████│🔴🔴🔴░░░░░░░░░░░░░░░░░░░░░░░     │ ← EXCEDIDO
│   (rojo)           │                                    │
│ 👤 Carol    ████████│████████░░░░░░░░░░░░░░░░░░░░░   │ ← Normal
└─────────────────────────────────────────────────────────┘
```

### **Elementos a Resaltar:**

1. **Nombre de la persona:** Color rojo si está excedida en algún día del rango visible
2. **Bloques de tareas en días excedidos:** Fondo rojo (`#F44336`)
3. **Indicador visual:** Borde rojo en la fila

---

## 🏗️ ARQUITECTURA

### **Función de Detección:**

```kotlin
data class OverloadInfo(
    val personId: String,
    val overloadedDates: Set<LocalDate>,
    val detailsByDate: Map<LocalDate, DayOverload>
)

data class DayOverload(
    val date: LocalDate,
    val hoursPlanned: Double,
    val hoursAvailable: Double,
    val excess: Double
)

fun detectOverloads(
    workspace: Workspace,
    project: Project,
    startDate: LocalDate,
    endDate: LocalDate
): Map<String, OverloadInfo> {
    val overloads = mutableMapOf<String, OverloadInfo>()
    
    project.members.forEach { personId ->
        val person = workspace.people.find { it.id == personId } ?: return@forEach
        val scheduleBlocks = workspace.planning.scheduleBlocks.filter {
            it.personId == personId && it.projectId == project.id
        }
        
        val overloadedDates = mutableSetOf<LocalDate>()
        val detailsByDate = mutableMapOf<LocalDate, DayOverload>()
        
        // Agrupar por fecha
        val blocksByDate = scheduleBlocks.groupBy { LocalDate.parse(it.date) }
        
        blocksByDate.forEach { (date, blocks) ->
            if (date in startDate..endDate) {
                val totalHours = blocks.sumOf { it.hoursPlanned }
                
                if (totalHours > person.hoursPerDay) {
                    overloadedDates.add(date)
                    detailsByDate[date] = DayOverload(
                        date = date,
                        hoursPlanned = totalHours,
                        hoursAvailable = person.hoursPerDay,
                        excess = totalHours - person.hoursPerDay
                    )
                }
            }
        }
        
        if (overloadedDates.isNotEmpty()) {
            overloads[personId] = OverloadInfo(
                personId = personId,
                overloadedDates = overloadedDates,
                detailsByDate = detailsByDate
            )
        }
    }
    
    return overloads
}
```

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/PlanningUseCases.kt`**
   - Añadir `detectOverloads()` y data classes

2. **`src/commonMain/kotlin/com/kodeforge/ui/components/ProjectTimeline.kt`**
   - Calcular overloads
   - Pasar info a `TimelineRow`

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/TimelineRow.kt`**
   - Recibir `isOverloaded: Boolean`
   - Resaltar nombre en rojo si excedido
   - Añadir borde rojo a la fila

4. **`src/commonMain/kotlin/com/kodeforge/ui/components/TaskBlock.kt`**
   - Recibir `isOverloaded: Boolean`
   - Cambiar color a rojo si excedido

---

## 🧪 CASO DE PRUEBA

### **Escenario: Alice Excedida**

```json
{
  "people": [
    {
      "id": "p_alice",
      "displayName": "Alice",
      "hoursPerDay": 8.0,
      "active": true
    }
  ],
  "projects": [
    {
      "id": "proj1",
      "name": "Project Alpha",
      "members": ["p_alice"]
    }
  ],
  "tasks": [
    {
      "id": "t1",
      "projectId": "proj1",
      "title": "Task A",
      "costHours": 4.0,
      "assigneeId": "p_alice",
      "status": "in_progress",
      "priority": 1
    },
    {
      "id": "t2",
      "projectId": "proj1",
      "title": "Task B",
      "costHours": 3.0,
      "assigneeId": "p_alice",
      "status": "in_progress",
      "priority": 2
    },
    {
      "id": "t3",
      "projectId": "proj1",
      "title": "Task C",
      "costHours": 2.0,
      "assigneeId": "p_alice",
      "status": "in_progress",
      "priority": 3
    }
  ],
  "planning": {
    "scheduleBlocks": [
      {
        "id": "sb1",
        "personId": "p_alice",
        "projectId": "proj1",
        "taskId": "t1",
        "date": "2026-02-17",
        "hoursPlanned": 4.0
      },
      {
        "id": "sb2",
        "personId": "p_alice",
        "projectId": "proj1",
        "taskId": "t2",
        "date": "2026-02-17",
        "hoursPlanned": 3.0
      },
      {
        "id": "sb3",
        "personId": "p_alice",
        "projectId": "proj1",
        "taskId": "t3",
        "date": "2026-02-17",
        "hoursPlanned": 2.0
      }
    ]
  }
}
```

**Resultado esperado:**
- Alice aparece con nombre en **rojo**
- Día 2026-02-17: bloques en **rojo**
- Total: 9 horas > 8 horas → **EXCEDIDA**

---

## 🎯 PROPUESTA PARA specs/spec.md

### **Sección 3.3 Proyectos - Añadir:**

```markdown
#### Detección de Sobrecarga

Una persona está **excedida** en un día si:

```
Σ hoursPlanned (en ese día) > person.hoursPerDay
```

**Resaltado visual:**
- Nombre de la persona en rojo
- Bloques de tareas en días excedidos en rojo
- Borde rojo en la fila del timeline

**Cálculo:**
- Se suman todas las `hoursPlanned` de los `scheduleBlocks` del día
- Se compara con `person.hoursPerDay`
- Si excede, se marca como sobrecarga
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Detectar sobrecarga diaria | `detectOverloads()` |
| Resaltar nombre en rojo | `TimelineRow` con color condicional |
| Resaltar bloques en rojo | `TaskBlock` con color condicional |
| Borde rojo en fila | `TimelineRow` con border condicional |
| Caso de prueba | data-schema.json actualizado |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Añadir `detectOverloads()` en `PlanningUseCases`
2. ✅ Modificar `ProjectTimeline` para calcular overloads
3. ✅ Modificar `TimelineRow` para resaltar nombre y fila
4. ✅ Modificar `TaskBlock` para color rojo en excedidos
5. ✅ Crear caso de prueba en data-schema.json
6. ✅ Actualizar specs/spec.md con definición
7. ✅ Compilar y validar

---

**Tiempo estimado:** 1-2 horas  
**Complejidad:** Media  
**Dependencias:** PlanningUseCases, ProjectTimeline, TimelineRow, TaskBlock

---

*Diseño completado - Listo para implementación*

