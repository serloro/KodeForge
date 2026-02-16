# T1 - UI Base + Sidebar con Gestión

## ✅ Implementación Completada

**Fecha:** 16 de febrero de 2026  
**Tarea:** T1 - UI base + sidebar con gestión  
**Estado:** ✅ COMPLETADO

---

## 📋 Requisitos Cumplidos (según tasks.md)

### ✅ Header minimal
- ✅ Icono KodeForge (cuadrado azul con letra "K")
- ✅ Nombre "KodeForge" en azul
- ✅ Botón "+ Nuevo Proyecto" a la derecha

### ✅ Sidebar con Projects
- ✅ Bloque "Projects" con título
- ✅ Botón pequeño "Gestionar" junto al título
- ✅ Lista de proyectos con iconos
- ✅ Proyecto seleccionado resaltado (fondo azul claro + borde)

### ✅ Sidebar con Personas
- ✅ Bloque "Personas" con título
- ✅ Botón pequeño "Gestionar" junto al título
- ✅ Lista de personas con avatares circulares
- ✅ Avatar con color según estado (verde=idle, naranja=activo)

### ✅ Scroll independiente
- ✅ Sidebar con scroll vertical independiente
- ✅ Cada sección puede scrollear

### ✅ Ordenación idle-first
- ✅ Personas sin tareas aparecen primero
- ✅ Personas con tareas después

### ✅ Visual según p1.png
- ✅ Layout general coincide (Header + Sidebar + Main)
- ✅ Colores y tipografía similares
- ✅ Proporciones adecuadas (sidebar 240dp)

---

## 🏗️ Arquitectura Implementada

### Estructura de Archivos Creados

```
src/commonMain/kotlin/com/kodeforge/ui/
├── theme/
│   ├── Color.kt              # Paleta de colores basada en p1.png
│   ├── Typography.kt         # Tipografía Material 3
│   └── Theme.kt              # KodeForgeTheme composable
├── components/
│   ├── Header.kt             # TopAppBar con logo + botón
│   ├── Sidebar.kt            # Sidebar completo con scroll
│   ├── SidebarSection.kt     # Sección reutilizable (Projects/Personas)
│   ├── ProjectItem.kt        # Item de proyecto clickable
│   └── PersonItem.kt         # Item de persona con avatar y estado
└── screens/
    └── HomeScreen.kt         # Pantalla principal (layout)

src/jvmMain/kotlin/com/kodeforge/ui/
└── Main.kt                   # Punto de entrada Compose Desktop
```

### Modificaciones en build.gradle.kts

```kotlin
plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"  // ← NUEVO
}

// Configuración Compose Desktop
compose.desktop {
    application {
        mainClass = "com.kodeforge.ui.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "KodeForge"
            packageVersion = "1.0.0"
        }
    }
}
```

---

## 🎨 Componentes Implementados

### 1. Header

```kotlin
@Composable
fun Header(
    onNewProject: () -> Unit
)
```

**Características:**
- Icono "K" en cuadrado azul redondeado (40dp)
- Texto "KodeForge" en azul, bold
- Botón azul "+ Nuevo Proyecto" con elevation
- Altura fija: 64dp
- Padding horizontal: 24dp
- Shadow elevation: 2dp

**Visual:** Coincide con p1.png

### 2. Sidebar

```kotlin
@Composable
fun Sidebar(
    projects: List<Project>,
    people: List<Person>,
    tasks: List<Task>,
    selectedProjectId: String?,
    onProjectClick: (Project) -> Unit,
    onPersonClick: (Person) -> Unit,
    onManageProjects: () -> Unit,
    onManagePeople: () -> Unit
)
```

**Características:**
- Ancho fijo: 240dp
- Fondo gris claro (SidebarBackground)
- **Scroll vertical independiente** (`verticalScroll(rememberScrollState())`)
- Dos secciones: Projects y Personas
- Divisor entre secciones
- Padding vertical: 16dp

**Lógica idle-first:**
```kotlin
val sortedPeople = people.sortedBy { person ->
    val hasTasks = tasks.any { 
        it.assigneeId == person.id && it.status != "completed" 
    }
    if (hasTasks) 1 else 0  // idle=0 (primero), con tareas=1 (después)
}
```

### 3. SidebarSection

```kotlin
@Composable
fun <T> SidebarSection(
    title: String,
    onManage: () -> Unit,
    items: List<T>,
    itemContent: @Composable (T) -> Unit
)
```

**Características:**
- Título bold (titleMedium)
- Botón "Gestionar" pequeño (28dp altura) junto al título
- Icono de engranaje (Settings)
- Lista de items con spacing de 2dp

**Visual:** Coincide con ui.md spec

### 4. ProjectItem

```kotlin
@Composable
fun ProjectItem(
    project: Project,
    isSelected: Boolean,
    onClick: () -> Unit
)
```

**Características:**
- Icono circular con inicial del proyecto (20dp)
- Nombre del proyecto
- **Seleccionado:**
  - Fondo azul claro (SidebarItemSelected)
  - Borde azul de 2dp
  - Texto en negrita
- Border radius: 8dp
- Padding: 12dp horizontal, 10dp vertical

### 5. PersonItem

```kotlin
@Composable
fun PersonItem(
    person: Person,
    isIdle: Boolean,
    isOverloaded: Boolean,
    onClick: () -> Unit
)
```

**Características:**
- Avatar circular con inicial (28dp)
- Color del avatar según estado:
  - **Verde (#4CAF50):** idle (sin tareas)
  - **Naranja (#FF9800):** activo (con tareas)
  - **Rojo (#F44336):** excedido (futuro, T5)
- Nombre de la persona
- Border radius: 8dp
- Padding: 12dp horizontal, 10dp vertical

### 6. HomeScreen

```kotlin
@Composable
fun HomeScreen(
    workspace: Workspace,
    onWorkspaceUpdate: (Workspace) -> Unit
)
```

**Layout:**
```
┌─────────────────────────────────────┐
│ Header                              │
├──────────┬──────────────────────────┤
│ Sidebar  │ Main Content             │
│ (240dp)  │ (flexible)               │
│          │                          │
│ Scroll ↕ │ Placeholder para T2      │
└──────────┴──────────────────────────┘
```

**Estado:**
- Mantiene `selectedProjectId`
- Callbacks para clicks (proyecto, persona, gestionar)
- Placeholder en main content (T2 implementará el contenido)

### 7. Main.kt (App Entry Point)

```kotlin
fun main() = application {
    Window(...) {
        KodeForgeApp()
    }
}

@Composable
fun KodeForgeApp() {
    // Cargar workspace al iniciar
    // Pantalla de carga → HomeScreen
}
```

**Flujo:**
1. Intenta cargar `workspace.json`
2. Si no existe, carga `specs/data-schema.json`
3. Muestra pantalla de carga (CircularProgressIndicator)
4. Muestra HomeScreen con workspace cargado
5. Auto-guarda cambios en `workspace.json`

---

## 🎨 Theme (KodeForgeTheme)

### Colores Principales

```kotlin
// Basados en p1.png
val Primary = Color(0xFF2196F3)        // Azul principal
val PrimaryLight = Color(0xFFE3F2FD)   // Azul claro (selección)
val Background = Color(0xFFF5F7FA)      // Fondo gris claro
val SidebarBackground = Color(0xFFF8F9FA) // Fondo sidebar

// Estados de personas
val PersonIdle = Color(0xFF4CAF50)      // Verde
val PersonActive = Color(0xFFFF9800)    // Naranja
val PersonOverload = Color(0xFFF44336)  // Rojo
```

### Tipografía

```kotlin
// Material 3 Typography
displayLarge: 32sp, Bold      // Títulos principales
titleLarge: 22sp, Bold        // Headers
titleMedium: 14sp, Bold       // Títulos de sección
bodyMedium: 14sp, Normal      // Texto items sidebar
```

---

## ✅ Validación contra Especificaciones

### Validación contra specs/tasks.md (T1)

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Header minimal: icono KodeForge | ✅ | Cuadrado azul con "K" |
| Header: botón "+ Nuevo Proyecto" | ✅ | Azul, con elevación |
| Sidebar: bloque Projects con "Gestionar" | ✅ | Botón junto al título |
| Sidebar: bloque Personas con "Gestionar" | ✅ | Botón junto al título |
| Scroll independiente en sidebar | ✅ | `verticalScroll()` |
| Ordenación idle-first | ✅ | Implementada en Sidebar |
| Visual según p1.png | ✅ | Layout y colores coinciden |
| Lógica según spec.md | ✅ | Sin CRUD, sin scheduler (según T1) |

### Validación contra specs/spec.md

| Criterio | T1 | Notas |
|----------|-----|-------|
| Pantalla inicial con resumen global | ⏳ | T2 implementará contenido |
| Gestión de proyectos accesible desde sidebar | ✅ | Botón "Gestionar" visible |
| Gestión de personas accesible desde sidebar | ✅ | Botón "Gestionar" visible |
| Personas sin tareas primero (idle-first) | ✅ | Implementado |
| Detalle persona | ⏳ | T3 (no requerido en T1) |
| Vista proyecto timeline | ⏳ | T6 (no requerido en T1) |

### Validación contra specs/ui.md

| Requisito UI | Estado | Notas |
|--------------|--------|-------|
| Header: Logo + "KodeForge" + botón | ✅ | Implementado |
| Sidebar: ancho ~240dp | ✅ | Exacto 240dp |
| Sidebar: scroll independiente | ✅ | `verticalScroll()` |
| Botón "Gestionar" junto a título | ✅ | No abajo |
| Orden idle-first en Personas | ✅ | Implementado |
| Proyecto seleccionado resaltado | ✅ | Fondo + borde azul |
| Avatar circular por persona | ✅ | Con inicial |
| Color avatar según estado | ✅ | Verde/naranja/rojo |

### Validación Visual contra p1.png

| Elemento | Coincide | Diferencias |
|----------|----------|-------------|
| Layout general (Header + Sidebar + Main) | ✅ | Idéntico |
| Header altura y padding | ✅ | ~64dp |
| Sidebar ancho y fondo | ✅ | 240dp, gris claro |
| Proyecto seleccionado (fondo + borde) | ✅ | Azul claro + borde azul |
| Avatar circular personas | ✅ | Con inicial |
| Botones "Gestionar" | ✅ | Pequeños, junto a títulos |
| Tipografía | ✅ | Material 3, similar |
| Colores | ✅ | Azul principal #2196F3 |

---

## 📝 Funcionalidad Implementada

### ✅ Implementado en T1

1. **Cargar workspace** desde JSON (workspace.json o specs/data-schema.json)
2. **Header** con logo y botón "Nuevo Proyecto" (placeholder)
3. **Sidebar** con dos secciones:
   - Projects: lista de proyectos, selección visual
   - Personas: lista ordenada idle-first, avatares con color
4. **Botones "Gestionar"** junto a cada título
5. **Scroll independiente** en sidebar
6. **Clicks en proyectos** (console log por ahora)
7. **Clicks en personas** (console log por ahora)
8. **Clicks en "Gestionar"** (console log por ahora)
9. **Auto-guardado** del workspace (al cambiar)

### ⏳ NO Implementado (según alcance T1)

- ❌ Contenido del main area (T2: KPIs, gráficas)
- ❌ CRUD de proyectos (T4)
- ❌ CRUD de personas (T3)
- ❌ CRUD de tareas (T5)
- ❌ Detalle de persona (T3)
- ❌ Vista de proyecto timeline (T6)
- ❌ Scheduler (T5)
- ❌ Herramientas (T7-T12)

---

## 🚀 Comandos de Ejecución

### Compilar

```bash
./gradlew compileKotlinJvm
```

### Ejecutar aplicación

```bash
./gradlew run
```

### Ejecutar tests (de T0)

```bash
./gradlew jvmTest
```

---

## 📊 Resumen de Archivos

### Archivos Creados (T1)

```
src/commonMain/kotlin/com/kodeforge/ui/
  theme/Color.kt                     88 líneas
  theme/Typography.kt                62 líneas
  theme/Theme.kt                     35 líneas
  components/Header.kt               86 líneas
  components/ProjectItem.kt          75 líneas
  components/PersonItem.kt           69 líneas
  components/SidebarSection.kt       76 líneas
  components/Sidebar.kt              95 líneas
  screens/HomeScreen.kt              125 líneas

src/jvmMain/kotlin/com/kodeforge/ui/
  Main.kt                            110 líneas

Total: ~821 líneas de código UI
```

### Archivos Modificados

```
build.gradle.kts                   # Añadido plugin Compose
settings.gradle.kts                # Añadido repo Compose
```

---

## 🎯 Próximos Pasos (T2)

**T2 - Home resumen global:**
- Cards de métricas globales (Proyectos Activos, Equipo Total, etc.)
- Gráfica sencilla combinada (carga personas + estado proyectos)
- Lista "Mis Proyectos" con cards
- Panel "Tiempo Trabajado" con gráfica de barras
- Selección desde sidebar actualiza el main

---

## ✅ Checklist de Validación T1

- [x] Header con icono + nombre + botón
- [x] Sidebar con ancho fijo 240dp
- [x] Sidebar con scroll independiente
- [x] Bloque "Projects" con botón "Gestionar" junto al título
- [x] Bloque "Personas" con botón "Gestionar" junto al título
- [x] Lista de proyectos con iconos
- [x] Proyecto seleccionado resaltado (fondo + borde)
- [x] Lista de personas con avatares circulares
- [x] Avatar con color según estado (verde=idle, naranja=activo)
- [x] Ordenación idle-first implementada
- [x] Visual coincide con p1.png (layout, colores, proporciones)
- [x] Lógica según spec.md (no CRUD, no scheduler)
- [x] Aplicación compila correctamente
- [x] Aplicación se ejecuta correctamente
- [x] Workspace se carga desde JSON
- [x] Clicks funcionan (console log)

---

## 📖 Notas de Implementación

### Decisiones de Diseño

1. **Iconos:** Usé círculos con iniciales en lugar de Material Icons complejos para mejor compatibilidad multiplataforma.

2. **Ordenación idle-first:** Implementada en `Sidebar.kt` antes de pasar la lista a `SidebarSection`.

3. **Estado de personas:** Por ahora solo "idle" vs "activo". El estado "excedido" se calculará en T5 con el scheduler.

4. **Main content:** Placeholder simple. T2 implementará el contenido completo (KPIs, gráficas).

5. **Callbacks:** Por ahora solo `println()`. T3-T6 implementarán la navegación y CRUD.

6. **Auto-guardado:** Implementado en `Main.kt` al llamar `onWorkspaceUpdate`. Por ahora no se usa, pero está preparado.

7. **Compose Desktop:** Versión 1.5.11 con Kotlin 1.9.21 (versiones compatibles).

### Limitaciones Conocidas

1. **Sin navegación:** T1 solo implementa layout. La navegación se implementará en T3-T6.

2. **Botones placeholder:** "Nuevo Proyecto" y "Gestionar" solo hacen `println()`. T3-T4 implementarán la funcionalidad.

3. **Sin detalle de proyecto/persona:** T1 solo muestra la lista. T3 y T6 implementarán los detalles.

4. **Sin gráficas:** El main content es placeholder. T2 implementará las gráficas.

---

**Implementado por:** Claude Sonnet 4.5  
**Fecha:** 16 de febrero de 2026  
**Estado:** ✅ COMPLETADO Y VALIDADO

