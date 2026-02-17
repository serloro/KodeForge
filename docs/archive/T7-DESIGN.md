# T7 - Vista Proyecto (Timeline por Filas) - Diseño

**Objetivo:** Implementar la UI del modo proyecto con timeline por filas según `p2.png`.

**Alcance:** SOLO UI. NO lógica de tools (SMTP, REST, etc.).

---

## 📋 ANÁLISIS DE p2.png

### **Estructura Visual:**

```
┌─────────────────────────────────────────────────────────────┐
│ Header: ProjectFlow > Cloud Scale UI                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Utilidades del Proyecto                                    │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐             │
│ │Tempo │ │Tempo │ │SMTP  │ │REST  │ │Ajustes│             │
│ │ 📊  │ │ ⏱️  │ │ 📧  │ │ 🔌  │ │ ⚙️   │             │
│ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘             │
│                                                             │
│ Timeline del Proyecto                                       │
│ ┌─────────────────────────────────────────────────────────┐│
│ │ Hoy: 24 Abr          [Columnas de fechas]              ││
│ │                                                         ││
│ │ Bassor   👤  ████████████░░░░░░░░░░░░░░░░░░░░░░░░     ││
│ │ Blonna   👤  ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░     ││
│ │ Basslizun 👤  ████████████████░░░░░░░░░░░░░░░░░░░░░   ││
│ │ Blodka   👤  ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░     ││
│ │ Devar    👤  ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░     ││
│ │ Ferdersen 👤  ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   ││
│ │              ↑                                          ││
│ │            Hoy (línea vertical)                         ││
│ └─────────────────────────────────────────────────────────┘│
│                                                             │
│ Total tas'a los                                             │
│ 125 tareas | 118 completadas | 92% de abierta incumplides │
│                                                             │
│ MÉTRICAS CLAVE                                              │
│ GUDD: 735h | SSS Prioridades: 735h | RETRITO AUTO: 92%    │
└─────────────────────────────────────────────────────────────┘
```

### **Elementos Clave:**

1. **Utilidades del Proyecto (Tiles):**
   - 5-6 tiles horizontales
   - Cada tile: icono + título + subtítulo
   - Colores pastel (azul, verde, naranja, morado, rojo)
   - Click → placeholder (sin lógica)

2. **Timeline:**
   - **Filas:** Una por persona miembro
   - **Columnas:** Fechas (días/semanas)
   - **Bloques:** Tareas asignadas (colores según estado)
   - **Línea "Hoy":** Vertical, muy visible (azul)
   - **Avatar:** Circular a la izquierda de cada fila

3. **Estadísticas:**
   - Total tareas
   - Completadas
   - Porcentaje
   - Métricas clave

---

## 🏗️ ARQUITECTURA

### **Pantalla Principal:**
```
ProjectViewScreen
├── Header (breadcrumb: ProjectFlow > [Nombre Proyecto])
├── Utilidades del Proyecto
│   └── UtilityTilesGrid (5-6 tiles)
├── Timeline del Proyecto
│   ├── TimelineHeader (fechas, "Hoy")
│   └── TimelineRows (una por persona)
│       └── TimelineRow (persona + bloques de tareas)
└── ProjectStats (estadísticas)
```

### **Componentes UI:**

1. **`ProjectViewScreen.kt`**
   - Pantalla principal del modo proyecto
   - Layout vertical con secciones

2. **`UtilityTile.kt`**
   - Tile individual para cada utilidad
   - Icono, título, subtítulo
   - Click → placeholder

3. **`UtilityTilesGrid.kt`**
   - Grid horizontal de tiles
   - 5-6 utilidades

4. **`ProjectTimeline.kt`**
   - Timeline completo
   - Header con fechas + línea "Hoy"
   - Filas de personas

5. **`TimelineRow.kt`**
   - Fila individual por persona
   - Avatar + nombre + bloques de tareas

6. **`TaskBlock.kt`**
   - Bloque visual de tarea en timeline
   - Color según estado
   - Ancho según duración

7. **`ProjectStats.kt`**
   - Estadísticas del proyecto
   - Total, completadas, porcentaje

---

## 🎨 DISEÑO VISUAL

### **Colores de Tiles (según p2.png):**

| Utilidad | Color Fondo | Color Icono |
|----------|-------------|-------------|
| Tempo 1 | Azul claro (`#E3F2FD`) | Azul (`#2196F3`) |
| Tempo 2 | Verde claro (`#E8F5E9`) | Verde (`#4CAF50`) |
| SMTP Fake | Naranja claro (`#FFF3E0`) | Naranja (`#FF9800`) |
| REST API | Morado claro (`#F3E5F5`) | Morado (`#9C27B0`) |
| Ajustes | Rojo claro (`#FFEBEE`) | Rojo (`#F44336`) |

### **Colores de Bloques de Tareas:**

| Estado | Color |
|--------|-------|
| todo | Gris claro (`#E0E0E0`) |
| in_progress | Naranja (`#FF9800`) |
| completed | Verde (`#4CAF50`) |
| overdue | Rojo (`#F44336`) |

### **Línea "Hoy":**
- Color: Azul primario (`#2196F3`)
- Ancho: 2-3dp
- Estilo: Sólido
- Label: "Hoy" + fecha

---

## 📊 LÓGICA DE DATOS

### **Calcular Bloques de Timeline:**

```kotlin
data class TimelineBlock(
    val taskId: String,
    val taskTitle: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val hoursPlanned: Double
)

fun calculateTimelineBlocks(
    workspace: Workspace,
    project: Project
): Map<String, List<TimelineBlock>> {
    // Agrupar scheduleBlocks por persona
    val blocksByPerson = workspace.planning.scheduleBlocks
        .filter { it.projectId == project.id }
        .groupBy { it.personId }
    
    // Para cada persona, consolidar bloques consecutivos de la misma tarea
    return blocksByPerson.mapValues { (_, blocks) ->
        consolidateBlocks(blocks, workspace.tasks)
    }
}
```

### **Calcular Rango de Fechas:**

```kotlin
fun calculateDateRange(
    scheduleBlocks: List<ScheduleBlock>,
    daysBuffer: Int = 7
): Pair<LocalDate, LocalDate> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    if (scheduleBlocks.isEmpty()) {
        return Pair(today.minus(daysBuffer, DateTimeUnit.DAY), today.plus(daysBuffer * 2, DateTimeUnit.DAY))
    }
    
    val minDate = scheduleBlocks.minOf { LocalDate.parse(it.date) }
    val maxDate = scheduleBlocks.maxOf { LocalDate.parse(it.date) }
    
    return Pair(
        minOf(minDate, today).minus(daysBuffer, DateTimeUnit.DAY),
        maxOf(maxDate, today).plus(daysBuffer, DateTimeUnit.DAY)
    )
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ProjectViewScreen.kt`**
   - Pantalla principal del modo proyecto

2. **`src/commonMain/kotlin/com/kodeforge/ui/components/UtilityTile.kt`**
   - Tile individual para utilidades

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/UtilityTilesGrid.kt`**
   - Grid de tiles

4. **`src/commonMain/kotlin/com/kodeforge/ui/components/ProjectTimeline.kt`**
   - Timeline completo

5. **`src/commonMain/kotlin/com/kodeforge/ui/components/TimelineRow.kt`**
   - Fila de persona en timeline

6. **`src/commonMain/kotlin/com/kodeforge/ui/components/TaskBlock.kt`**
   - Bloque de tarea en timeline

7. **`src/commonMain/kotlin/com/kodeforge/ui/components/ProjectStats.kt`**
   - Estadísticas del proyecto

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`**
   - Añadir navegación a `ProjectViewScreen`
   - Modificar `onProjectClick` para ir a vista proyecto

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Modo proyecto al seleccionar | `ProjectViewScreen` |
| Tiles de utilidades (UI) | `UtilityTilesGrid` |
| Timeline por filas (personas) | `ProjectTimeline` |
| Bloques de tareas | `TaskBlock` |
| Línea "Hoy" vertical | En `ProjectTimeline` |
| Estadísticas del proyecto | `ProjectStats` |
| NO lógica de tools | Correcto, solo UI |
| Basado en p2.png | Colores, layout, spacing |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `UtilityTile.kt` - Tile individual
2. ✅ Crear `UtilityTilesGrid.kt` - Grid de tiles
3. ✅ Crear `TaskBlock.kt` - Bloque de tarea
4. ✅ Crear `TimelineRow.kt` - Fila de persona
5. ✅ Crear `ProjectTimeline.kt` - Timeline completo
6. ✅ Crear `ProjectStats.kt` - Estadísticas
7. ✅ Crear `ProjectViewScreen.kt` - Pantalla principal
8. ✅ Modificar `HomeScreen.kt` - Navegación
9. ✅ Compilar y validar
10. ✅ Comparar con p2.png

---

**Tiempo estimado:** 3-4 horas  
**Complejidad:** Alta  
**Dependencias:** Workspace, Project, ScheduleBlocks

---

*Diseño completado - Listo para implementación*

