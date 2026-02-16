# T8 - Navegación a Tools (Placeholders) - Diseño

**Objetivo:** Implementar navegación desde tiles de Utilities a pantallas placeholder de tools.

**Alcance:** SOLO navegación y UI placeholder. NO lógica de tools.

---

## 📋 ANÁLISIS

### **Tools a Implementar:**

1. **SMTP Fake** - Servidor SMTP falso para testing
2. **REST/SOAP** - Cliente HTTP + Mock Server
3. **SFTP** - Conexión SFTP/SSH
4. **BBDD** - Conexiones a bases de datos
5. **Gestión Tareas** - Sync con GitHub Issues, etc.
6. **Info** - Editor WYSIWYG multiidioma

---

## 🏗️ ARQUITECTURA

### **Navegación:**

```
ProjectViewScreen
       ↓
Usuario click en tile "SMTP Fake"
       ↓
onUtilityClick("smtp")
       ↓
currentScreen = Screen.Tool(toolType = "smtp")
       ↓
ToolScreen(toolType = "smtp")
       ↓
Muestra placeholder de SMTP Fake
```

### **Componentes:**

1. **`ToolScreen.kt`** - Pantalla genérica para tools
   - Recibe `toolType: String`
   - Muestra título, descripción, "En construcción"

2. **Modificar `HomeScreen.kt`:**
   - Añadir `Screen.Tool(toolType: String)`
   - Navegar desde `ProjectViewScreen`

3. **Modificar `ProjectViewScreen.kt`:**
   - Pasar callback `onToolClick: (String) -> Unit`

---

## 🎨 DISEÑO VISUAL

### **Pantalla Placeholder:**

```
┌─────────────────────────────────────────────────────────┐
│ ← [Título del Tool]                                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                    🔧                                   │
│                                                         │
│              [Título del Tool]                          │
│                                                         │
│         [Descripción breve del tool]                    │
│                                                         │
│              🚧 En construcción 🚧                      │
│                                                         │
│     Esta funcionalidad estará disponible próximamente   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 CONFIGURACIÓN DE TOOLS

```kotlin
data class ToolConfig(
    val id: String,
    val title: String,
    val description: String,
    val icon: String // emoji
)

val toolConfigs = mapOf(
    "tempo1" to ToolConfig(
        id = "tempo1",
        title = "Tempo - Gestión Tarea 1",
        description = "Herramienta de gestión de tiempo y tareas",
        icon = "📅"
    ),
    "tempo2" to ToolConfig(
        id = "tempo2",
        title = "Tempo - Hory Franquimonos",
        description = "Herramienta de seguimiento de horas",
        icon = "⏱️"
    ),
    "smtp" to ToolConfig(
        id = "smtp",
        title = "SMTP Fake",
        description = "Servidor SMTP falso para testing de correos electrónicos",
        icon = "📧"
    ),
    "rest" to ToolConfig(
        id = "rest",
        title = "REST/SOAP API",
        description = "Cliente HTTP y Mock Server para APIs REST y SOAP",
        icon = "🔌"
    ),
    "ajustes" to ToolConfig(
        id = "ajustes",
        title = "Ajustes",
        description = "Configuración general de la aplicación",
        icon = "⚙️"
    ),
    "info" to ToolConfig(
        id = "info",
        title = "Info - Documentación",
        description = "Editor WYSIWYG multiidioma para páginas de información",
        icon = "ℹ️"
    ),
    "sftp" to ToolConfig(
        id = "sftp",
        title = "SFTP/SSH",
        description = "Conexión SFTP y explorador de archivos remoto",
        icon = "📁"
    ),
    "bbdd" to ToolConfig(
        id = "bbdd",
        title = "Base de Datos",
        description = "Conexiones a bases de datos y editor de consultas",
        icon = "🗄️"
    ),
    "tasks" to ToolConfig(
        id = "tasks",
        title = "Gestión de Tareas",
        description = "Sincronización con GitHub Issues y otros sistemas",
        icon = "✅"
    )
)
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ToolScreen.kt`**
   - Pantalla placeholder genérica para tools

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`**
   - Añadir `Screen.Tool(toolType: String, project: Project)`
   - Manejar navegación a `ToolScreen`

2. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ProjectViewScreen.kt`**
   - Añadir parámetro `onToolClick: (String) -> Unit`
   - Pasar a `UtilityTilesGrid`

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/UtilityTilesGrid.kt`**
   - Ya tiene `onUtilityClick`, solo verificar

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Click en tile → navegar | `onUtilityClick` → `Screen.Tool` |
| Pantalla placeholder | `ToolScreen` |
| Título + descripción | `ToolConfig` |
| "En construcción" | Texto placeholder |
| Botón volver | TopAppBar con back |
| NO lógica de tools | Correcto |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `ToolScreen.kt` con placeholder
2. ✅ Añadir `Screen.Tool` en `HomeScreen.kt`
3. ✅ Modificar `ProjectViewScreen.kt` para pasar callback
4. ✅ Conectar navegación
5. ✅ Compilar y validar

---

**Tiempo estimado:** 30-45 minutos  
**Complejidad:** Baja  
**Dependencias:** HomeScreen, ProjectViewScreen

---

*Diseño completado - Listo para implementación*

