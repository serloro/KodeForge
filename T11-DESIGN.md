# T11 - Tool Info (WYSIWYG MVP) - Diseño

**Objetivo:** Implementar edición WYSIWYG para páginas Info.

**Alcance:** Editor MVP con toolbar + preview en vivo.

---

## 📋 ANÁLISIS DE OPCIONES

### **Opción 1: WYSIWYG Nativo (Compose Desktop)**
- ❌ No existe librería WYSIWYG nativa para Compose Desktop
- ❌ WebView no está disponible en Desktop

### **Opción 2: Librerías Multiplataforma**
- ❌ TinyMCE/CKEditor: requieren WebView (no disponible)
- ❌ Quill: requiere WebView
- ❌ No hay librerías Compose-native disponibles

### **Opción 3: MVP con Editor + Preview (SELECCIONADA)**
- ✅ Editor de texto plano con HTML
- ✅ Toolbar que inserta tags HTML
- ✅ Preview en vivo del HTML renderizado
- ✅ Viable con Compose Desktop
- ✅ Fácil evolución a WYSIWYG real

---

## 🏗️ ARQUITECTURA MVP

### **Componentes:**

1. **`HtmlEditor.kt`** - Editor principal
   - Modo "Leer" / "Editar"
   - Botón "Editar" / "Guardar" / "Cancelar"
   - Toolbar con acciones
   - TextField para HTML
   - Preview en vivo

2. **`HtmlEditorToolbar.kt`** - Barra de herramientas
   - Botones: Bold, Italic, H1, H2, List, Link
   - Inserta tags HTML en posición del cursor

3. **`HtmlPreview.kt`** - Preview en vivo
   - Renderiza HTML mientras se edita
   - Actualización en tiempo real

---

## 🎨 DISEÑO VISUAL

### **Modo Leer:**

```
┌──────────────────────────────────────────────────────────┐
│ [ES] [EN]                                    [Editar]    │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Introducción                                            │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │                                                    │ │
│  │  Introducción                                      │ │
│  │  Documentación del proyecto...                     │ │
│  │                                                    │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### **Modo Editar:**

```
┌──────────────────────────────────────────────────────────┐
│ [ES] [EN]                        [Guardar] [Cancelar]    │
├──────────────────────────────────────────────────────────┤
│ [B] [I] [H1] [H2] [•] [🔗]                               │
├──────────────────────────────────────────────────────────┤
│ Editor HTML                    │ Preview                 │
│ ┌────────────────────────────┐ │ ┌─────────────────────┐ │
│ │ <h1>Introducción</h1>      │ │ │ Introducción        │ │
│ │ <p>Documentación...</p>    │ │ │ Documentación...    │ │
│ │                            │ │ │                     │ │
│ │                            │ │ │                     │ │
│ └────────────────────────────┘ │ └─────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## 🔧 FUNCIONALIDADES

### **Toolbar:**

| Botón | Acción | HTML Insertado |
|-------|--------|----------------|
| **B** | Bold | `<strong>texto</strong>` |
| **I** | Italic | `<em>texto</em>` |
| **H1** | Heading 1 | `<h1>texto</h1>` |
| **H2** | Heading 2 | `<h2>texto</h2>` |
| **•** | Bullet List | `<ul><li>item</li></ul>` |
| **🔗** | Link | `<a href="url">texto</a>` |

### **Inserción de Tags:**

```kotlin
fun insertTag(
    currentText: String,
    cursorPosition: Int,
    openTag: String,
    closeTag: String,
    defaultContent: String = ""
): Pair<String, Int> {
    // Si hay selección, envolver
    // Si no hay selección, insertar con contenido por defecto
    // Retornar nuevo texto + nueva posición del cursor
}
```

### **Persistencia:**

- **Inmediata:** Al hacer click en "Guardar"
- **Actualiza:** `translations[lang].html` y `translations[lang].updatedAt`
- **Cancela:** Descarta cambios y vuelve al contenido original

---

## 📊 ESTADO

### **HtmlEditor State:**

```kotlin
var isEditMode by remember { mutableStateOf(false) }
var editedHtml by remember { mutableStateOf("") }
var cursorPosition by remember { mutableStateOf(0) }
var showLinkDialog by remember { mutableStateOf(false) }
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/components/HtmlEditor.kt`**
   - Componente principal del editor
   - Modo leer/editar
   - Botones Guardar/Cancelar

2. **`src/commonMain/kotlin/com/kodeforge/ui/components/HtmlEditorToolbar.kt`**
   - Barra de herramientas
   - Botones de formato

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/HtmlPreview.kt`**
   - Preview en vivo del HTML

4. **`src/commonMain/kotlin/com/kodeforge/ui/components/InsertLinkDialog.kt`**
   - Diálogo para insertar enlaces

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/components/InfoPageViewer.kt`**
   - Reemplazar `HtmlViewer` con `HtmlEditor`
   - Pasar callbacks de guardado

2. **`src/commonMain/kotlin/com/kodeforge/ui/screens/InfoToolScreen.kt`**
   - Añadir callback para guardar HTML editado

---

## 🧪 TESTS

### **InfoUseCasesTest.kt (añadir):**

```kotlin
@Test
fun `updatePage - updates html and updatedAt`() {
    // Crear página
    val createResult = infoUseCases.createPage(...)
    val workspace1 = createResult.getOrNull()!!
    val page = workspace1.projects[0].tools.info?.pages?.get(0)!!
    val originalUpdatedAt = page.translations["es"]?.updatedAt
    
    // Esperar 1ms para asegurar timestamp diferente
    Thread.sleep(1)
    
    // Actualizar HTML
    val updateResult = infoUseCases.updatePage(
        workspace = workspace1,
        projectId = project.id,
        pageId = page.id,
        htmlEs = "<h1>Nuevo contenido</h1>"
    )
    
    val workspace2 = updateResult.getOrNull()!!
    val updatedPage = workspace2.projects[0].tools.info?.pages?.get(0)!!
    
    // Verificar HTML cambió
    assertEquals("<h1>Nuevo contenido</h1>", updatedPage.translations["es"]?.html)
    
    // Verificar updatedAt cambió
    assertNotEquals(originalUpdatedAt, updatedPage.translations["es"]?.updatedAt)
}
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Modo Leer/Editar | Botón "Editar" cambia modo |
| Toolbar Bold/Italic | Inserta `<strong>` / `<em>` |
| Toolbar H1/H2 | Inserta `<h1>` / `<h2>` |
| Toolbar List | Inserta `<ul><li>` |
| Toolbar Link | Diálogo + inserta `<a href>` |
| Guardar | Actualiza HTML + updatedAt |
| Cancelar | Descarta cambios |
| Persistencia | Inmediata al guardar |
| Preview en vivo | Renderiza mientras edita |
| Tests | HTML + updatedAt cambian |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `HtmlPreview.kt` (mejorar HtmlViewer)
2. ✅ Crear `InsertLinkDialog.kt`
3. ✅ Crear `HtmlEditorToolbar.kt`
4. ✅ Crear `HtmlEditor.kt`
5. ✅ Modificar `InfoPageViewer.kt` para usar `HtmlEditor`
6. ✅ Modificar `InfoToolScreen.kt` para callback de guardado
7. ✅ Añadir tests de guardado
8. ✅ Compilar y validar

---

## 🚀 EVOLUCIÓN A WYSIWYG REAL

### **Opción A: Integrar WebView (cuando esté disponible)**

```kotlin
// Futuro: cuando Compose Desktop soporte WebView
@Composable
fun RichTextEditor(html: String, onHtmlChange: (String) -> Unit) {
    WebView(
        url = "file:///editor.html", // TinyMCE/CKEditor embebido
        onMessageReceived = { message ->
            if (message.type == "htmlChanged") {
                onHtmlChange(message.html)
            }
        }
    )
}
```

### **Opción B: Librería Compose-native (cuando exista)**

```kotlin
// Futuro: si aparece librería WYSIWYG para Compose
@Composable
fun RichTextEditor(html: String, onHtmlChange: (String) -> Unit) {
    ComposeRichTextEditor(
        initialHtml = html,
        onHtmlChange = onHtmlChange,
        toolbar = RichTextToolbar.Default
    )
}
```

### **Opción C: Implementación Custom (avanzada)**

```kotlin
// Futuro: editor WYSIWYG custom con Compose
@Composable
fun RichTextEditor(html: String, onHtmlChange: (String) -> Unit) {
    val document = remember(html) { parseHtmlToDocument(html) }
    
    RichTextCanvas(
        document = document,
        onDocumentChange = { doc ->
            onHtmlChange(doc.toHtml())
        }
    )
}
```

---

**Tiempo estimado:** 3-4 horas  
**Complejidad:** Alta  
**Dependencias:** InfoUseCases, InfoPageViewer

---

*Diseño completado - Listo para implementación MVP*

