# T10 - Tool Info (UI Lista Páginas + Idioma) - Diseño

**Objetivo:** Implementar UI para gestión y visualización de páginas Info.

**Alcance:** Lista de páginas + visor HTML. NO editor WYSIWYG.

---

## 📋 ANÁLISIS

### **Requisitos:**

1. **Panel izquierdo:**
   - Lista de páginas con scroll
   - Botón "Crear página"
   - Acciones por página: Renombrar, Eliminar, Reordenar

2. **Panel derecho:**
   - Visor HTML (solo lectura)
   - Selector de idioma (es/en)
   - Fallback si no existe traducción

3. **Coherencia visual:**
   - Basado en p2.png
   - Cards, spacing, colores consistentes

---

## 🎨 DISEÑO VISUAL

### **Layout:**

```
┌─────────────────────────────────────────────────────────────┐
│ ← Info - Documentación                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ ┌──────────────┐  ┌──────────────────────────────────────┐ │
│ │ Páginas      │  │ [ES] [EN]                            │ │
│ │              │  │                                      │ │
│ │ [+ Nueva]    │  │ ┌──────────────────────────────────┐ │ │
│ │              │  │ │                                  │ │ │
│ │ ┌──────────┐ │  │ │  <h1>Introducción</h1>          │ │ │
│ │ │Intro  ✏️🗑️│ │  │ │  <p>Documentación...</p>        │ │ │
│ │ │↑ ↓       │ │  │ │                                  │ │ │
│ │ └──────────┘ │  │ │                                  │ │ │
│ │              │  │ │                                  │ │ │
│ │ ┌──────────┐ │  │ │                                  │ │ │
│ │ │API    ✏️🗑️│ │  │ │                                  │ │ │
│ │ │↑ ↓       │ │  │ │                                  │ │ │
│ │ └──────────┘ │  │ └──────────────────────────────────┘ │ │
│ │              │  │                                      │ │
│ │ ┌──────────┐ │  │ [No hay traducción en inglés]       │ │
│ │ │FAQ    ✏️🗑️│ │  │ [Copiar desde español]              │ │
│ │ │↑ ↓       │ │  │                                      │ │
│ │ └──────────┘ │  │                                      │ │
│ └──────────────┘  └──────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ ARQUITECTURA

### **Componentes:**

1. **`InfoToolScreen.kt`** - Pantalla principal
   - Layout de 2 columnas
   - Estado: selectedPageId, selectedLocale

2. **`InfoPageList.kt`** - Lista de páginas
   - Scroll vertical
   - Botón "Nueva página"
   - Items de página con acciones

3. **`InfoPageListItem.kt`** - Item de página
   - Título
   - Botones: Editar, Eliminar, Arriba, Abajo

4. **`InfoPageViewer.kt`** - Visor HTML
   - Selector de idioma
   - Renderizado HTML (AndroidView/WebView)
   - Mensaje de fallback

5. **`CreatePageDialog.kt`** - Diálogo crear página
   - Slug
   - Título ES
   - Título EN

6. **`RenamePageDialog.kt`** - Diálogo renombrar
   - Slug
   - Título ES
   - Título EN

7. **`DeletePageDialog.kt`** - Confirmación eliminar
   - Mensaje de confirmación
   - Botones Cancelar/Eliminar

---

## 📊 ESTADO Y LÓGICA

### **Estado de InfoToolScreen:**

```kotlin
var selectedPageId by remember { mutableStateOf<String?>(null) }
var selectedLocale by remember { mutableStateOf("es") }
var showCreateDialog by remember { mutableStateOf(false) }
var pageToRename by remember { mutableStateOf<InfoPage?>(null) }
var pageToDelete by remember { mutableStateOf<InfoPage?>(null) }
```

### **Acciones:**

```kotlin
// Crear página
onCreatePage(slug, titleEs, titleEn)

// Renombrar página
onRenamePage(pageId, slug, titleEs, titleEn)

// Eliminar página
onDeletePage(pageId)

// Reordenar
onMovePageUp(pageId)
onMovePageDown(pageId)

// Cambiar idioma
onLocaleChange(locale)

// Seleccionar página
onPageSelect(pageId)
```

---

## 🎨 COMPONENTES DETALLADOS

### **1. InfoToolScreen.kt:**

```kotlin
@Composable
fun InfoToolScreen(
    workspace: Workspace,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit
) {
    val infoUseCases = remember { InfoUseCases() }
    val pages = remember(workspace, project.id) {
        infoUseCases.getPages(workspace, project.id)
    }
    
    var selectedPageId by remember { mutableStateOf(pages.firstOrNull()?.id) }
    var selectedLocale by remember { mutableStateOf("es") }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Panel izquierdo: Lista de páginas
        InfoPageList(
            pages = pages,
            selectedPageId = selectedPageId,
            onPageSelect = { selectedPageId = it },
            onCreatePage = { ... },
            onRenamePage = { ... },
            onDeletePage = { ... },
            onMoveUp = { ... },
            onMoveDown = { ... }
        )
        
        // Panel derecho: Visor HTML
        InfoPageViewer(
            page = pages.find { it.id == selectedPageId },
            selectedLocale = selectedLocale,
            onLocaleChange = { selectedLocale = it },
            onCopyTranslation = { ... }
        )
    }
}
```

### **2. InfoPageList.kt:**

```kotlin
@Composable
fun InfoPageList(
    pages: List<InfoPage>,
    selectedPageId: String?,
    onPageSelect: (String) -> Unit,
    onCreatePage: () -> Unit,
    onRenamePage: (InfoPage) -> Unit,
    onDeletePage: (InfoPage) -> Unit,
    onMoveUp: (InfoPage) -> Unit,
    onMoveDown: (InfoPage) -> Unit
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Título
        Text("Páginas", style = MaterialTheme.typography.titleLarge)
        
        Spacer(Modifier.height(16.dp))
        
        // Botón crear
        Button(onClick = onCreatePage) {
            Icon(Icons.Default.Add, null)
            Text("Nueva página")
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Lista con scroll
        LazyColumn {
            items(pages) { page ->
                InfoPageListItem(
                    page = page,
                    isSelected = page.id == selectedPageId,
                    canMoveUp = pages.indexOf(page) > 0,
                    canMoveDown = pages.indexOf(page) < pages.size - 1,
                    onSelect = { onPageSelect(page.id) },
                    onRename = { onRenamePage(page) },
                    onDelete = { onDeletePage(page) },
                    onMoveUp = { onMoveUp(page) },
                    onMoveDown = { onMoveDown(page) }
                )
            }
        }
    }
}
```

### **3. InfoPageViewer.kt:**

```kotlin
@Composable
fun InfoPageViewer(
    page: InfoPage?,
    selectedLocale: String,
    onLocaleChange: (String) -> Unit,
    onCopyTranslation: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        // Selector de idioma
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedLocale == "es",
                onClick = { onLocaleChange("es") },
                label = { Text("Español") }
            )
            FilterChip(
                selected = selectedLocale == "en",
                onClick = { onLocaleChange("en") },
                label = { Text("English") }
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (page == null) {
            // Sin página seleccionada
            Text("Selecciona una página para ver su contenido")
        } else {
            val translation = page.translations[selectedLocale]
            
            if (translation != null) {
                // Visor HTML
                HtmlViewer(html = translation.html)
            } else {
                // Fallback
                val otherLocale = if (selectedLocale == "es") "en" else "es"
                val otherTranslation = page.translations[otherLocale]
                
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No hay traducción en ${if (selectedLocale == "es") "español" else "inglés"}")
                        
                        if (otherTranslation != null) {
                            Button(onClick = { onCopyTranslation(page.id, otherLocale) }) {
                                Text("Copiar desde ${if (otherLocale == "es") "español" else "inglés"}")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### **4. HtmlViewer.kt:**

```kotlin
@Composable
fun HtmlViewer(html: String) {
    // Opción 1: Renderizar HTML simple con Text (limitado)
    // Opción 2: Usar AndroidView con WebView (desktop no soportado)
    // Opción 3: Usar librería externa (ej: Compose HTML)
    
    // Por ahora: mostrar HTML como texto con formato básico
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            // Renderizado básico de HTML
            // TODO: Mejorar con librería de renderizado HTML
            Text(
                text = html.replace("<[^>]*>".toRegex(), ""),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/InfoToolScreen.kt`**
2. **`src/commonMain/kotlin/com/kodeforge/ui/components/InfoPageList.kt`**
3. **`src/commonMain/kotlin/com/kodeforge/ui/components/InfoPageListItem.kt`**
4. **`src/commonMain/kotlin/com/kodeforge/ui/components/InfoPageViewer.kt`**
5. **`src/commonMain/kotlin/com/kodeforge/ui/components/HtmlViewer.kt`**
6. **`src/commonMain/kotlin/com/kodeforge/ui/components/CreatePageDialog.kt`**
7. **`src/commonMain/kotlin/com/kodeforge/ui/components/RenamePageDialog.kt`**
8. **`src/commonMain/kotlin/com/kodeforge/ui/components/DeletePageDialog.kt`**

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`**
   - Modificar navegación de `Screen.Tool` para Info

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Panel izquierdo con lista | `InfoPageList` |
| Crear página | `CreatePageDialog` |
| Renombrar página | `RenamePageDialog` |
| Eliminar con confirmación | `DeletePageDialog` |
| Reordenar (↑↓) | Botones en `InfoPageListItem` |
| Panel derecho visor HTML | `InfoPageViewer` |
| Selector de idioma | FilterChips en `InfoPageViewer` |
| Fallback traducción | Mensaje + botón copiar |
| Coherencia visual p2.png | Cards, spacing, colores |
| NO editor WYSIWYG | Correcto |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `HtmlViewer.kt`
2. ✅ Crear `InfoPageListItem.kt`
3. ✅ Crear `InfoPageList.kt`
4. ✅ Crear `InfoPageViewer.kt`
5. ✅ Crear `CreatePageDialog.kt`
6. ✅ Crear `RenamePageDialog.kt`
7. ✅ Crear `DeletePageDialog.kt`
8. ✅ Crear `InfoToolScreen.kt`
9. ✅ Modificar `HomeScreen.kt` para navegar a Info
10. ✅ Compilar y validar

---

**Tiempo estimado:** 3-4 horas  
**Complejidad:** Alta  
**Dependencias:** InfoUseCases, Project, Workspace

---

*Diseño completado - Listo para implementación*

