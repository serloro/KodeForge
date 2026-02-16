# T9 - Tool Info (Modelo + Persistencia) - Diseño

**Objetivo:** Implementar modelo y persistencia para la herramienta Info (páginas WYSIWYG multiidioma).

**Alcance:** SOLO modelo de datos y CRUD. NO UI de editor.

---

## 📋 ANÁLISIS DE ESPECIFICACIONES

### **specs/spec.md:**
- Páginas Info WYSIWYG multiidioma (en JSON)
- Persistencia en workspace

### **specs/data-schema.json:**

```json
{
  "projects": [
    {
      "id": "pr_cloudScale",
      "tools": {
        "info": {
          "enabled": true,
          "pages": [
            {
              "id": "info_intro",
              "slug": "intro",
              "title": {
                "es": "Introducción",
                "en": "Introduction"
              },
              "order": 1,
              "translations": {
                "es": {
                  "html": "<h1>Introducción</h1><p>Documentación del proyecto.</p>",
                  "updatedAt": "2026-02-15T09:00:00Z"
                },
                "en": {
                  "html": "<h1>Introduction</h1><p>Project documentation.</p>",
                  "updatedAt": "2026-02-15T09:10:00Z"
                }
              }
            }
          ]
        }
      }
    }
  ]
}
```

---

## 🏗️ MODELO DE DATOS

### **InfoPage:**

```kotlin
@Serializable
data class InfoPage(
    val id: String,
    val slug: String,
    val title: Map<String, String>, // locale → title
    val order: Int,
    val translations: Map<String, InfoTranslation> // locale → translation
)

@Serializable
data class InfoTranslation(
    val html: String,
    val updatedAt: String // ISO 8601
)
```

### **InfoTool:**

```kotlin
@Serializable
data class InfoTool(
    val enabled: Boolean = true,
    val pages: List<InfoPage> = emptyList()
)
```

### **ProjectTools (modificar existente):**

```kotlin
@Serializable
data class ProjectTools(
    val smtpFake: SmtpFakeTool? = null,
    val restSoap: RestSoapTool? = null,
    val sftp: SftpTool? = null,
    val dbTools: DbTool? = null,
    val taskManager: TaskManagerTool? = null,
    val info: InfoTool? = null // ← Nuevo
)
```

---

## 📊 CASOS DE USO

### **InfoUseCases:**

```kotlin
class InfoUseCases {
    
    /**
     * Crea una nueva página Info.
     */
    fun createPage(
        workspace: Workspace,
        projectId: String,
        slug: String,
        titleEs: String,
        titleEn: String,
        htmlEs: String = "",
        htmlEn: String = ""
    ): Result<Workspace>
    
    /**
     * Actualiza una página existente.
     */
    fun updatePage(
        workspace: Workspace,
        projectId: String,
        pageId: String,
        slug: String? = null,
        titleEs: String? = null,
        titleEn: String? = null,
        htmlEs: String? = null,
        htmlEn: String? = null
    ): Result<Workspace>
    
    /**
     * Elimina una página.
     */
    fun deletePage(
        workspace: Workspace,
        projectId: String,
        pageId: String
    ): Result<Workspace>
    
    /**
     * Reordena páginas.
     */
    fun reorderPages(
        workspace: Workspace,
        projectId: String,
        pageIds: List<String>
    ): Result<Workspace>
    
    /**
     * Obtiene una página por slug.
     */
    fun getPageBySlug(
        workspace: Workspace,
        projectId: String,
        slug: String
    ): InfoPage?
    
    /**
     * Obtiene todas las páginas ordenadas.
     */
    fun getPages(
        workspace: Workspace,
        projectId: String
    ): List<InfoPage>
}
```

---

## 🎯 VALIDACIONES

### **InfoValidator:**

```kotlin
object InfoValidator {
    
    sealed class ValidationError(val message: String) {
        object SlugEmpty : ValidationError("El slug no puede estar vacío")
        object SlugInvalid : ValidationError("El slug solo puede contener letras, números y guiones")
        object SlugDuplicate : ValidationError("Ya existe una página con ese slug")
        object TitleEmpty : ValidationError("El título no puede estar vacío")
        object PageNotFound : ValidationError("Página no encontrada")
    }
    
    fun validateSlug(slug: String): Result<Unit> {
        if (slug.isBlank()) {
            return Result.failure(Exception(ValidationError.SlugEmpty.message))
        }
        
        val slugRegex = Regex("^[a-z0-9-]+$")
        if (!slugRegex.matches(slug)) {
            return Result.failure(Exception(ValidationError.SlugInvalid.message))
        }
        
        return Result.success(Unit)
    }
    
    fun validateTitle(title: String): Result<Unit> {
        if (title.isBlank()) {
            return Result.failure(Exception(ValidationError.TitleEmpty.message))
        }
        return Result.success(Unit)
    }
    
    fun validateSlugUnique(
        workspace: Workspace,
        projectId: String,
        slug: String,
        excludePageId: String? = null
    ): Result<Unit> {
        val project = workspace.projects.find { it.id == projectId }
            ?: return Result.failure(Exception("Proyecto no encontrado"))
        
        val pages = project.tools?.info?.pages ?: emptyList()
        val duplicate = pages.find { 
            it.slug == slug && it.id != excludePageId 
        }
        
        if (duplicate != null) {
            return Result.failure(Exception(ValidationError.SlugDuplicate.message))
        }
        
        return Result.success(Unit)
    }
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/model/InfoPage.kt`**
   - Data classes: `InfoPage`, `InfoTranslation`

2. **`src/commonMain/kotlin/com/kodeforge/domain/model/InfoTool.kt`**
   - Data class: `InfoTool`

3. **`src/commonMain/kotlin/com/kodeforge/domain/validation/InfoValidator.kt`**
   - Validaciones de slug, título, unicidad

4. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/InfoUseCases.kt`**
   - CRUD de páginas Info

5. **`src/jvmTest/kotlin/com/kodeforge/InfoUseCasesTest.kt`**
   - Tests de CRUD y persistencia

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/model/Project.kt`**
   - Añadir `tools: ProjectTools?`

2. **`src/commonMain/kotlin/com/kodeforge/domain/model/ProjectTools.kt`** (crear si no existe)
   - Data class con todos los tools

---

## 🧪 TESTS

### **InfoUseCasesTest.kt:**

```kotlin
class InfoUseCasesTest {
    
    @Test
    fun `createPage - creates page with default content`()
    
    @Test
    fun `createPage - validates slug format`()
    
    @Test
    fun `createPage - validates slug uniqueness`()
    
    @Test
    fun `updatePage - updates title and html`()
    
    @Test
    fun `deletePage - removes page`()
    
    @Test
    fun `reorderPages - changes order`()
    
    @Test
    fun `getPageBySlug - finds page`()
    
    @Test
    fun `persistence - pages survive save and load`()
}
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Modelo InfoPage | `InfoPage.kt` |
| Modelo InfoTool | `InfoTool.kt` |
| CRUD de páginas | `InfoUseCases` |
| Validación de slug | `InfoValidator` |
| Persistencia en JSON | Serializable |
| Tests de load/save | `InfoUseCasesTest` |
| NO UI de editor | Correcto |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `InfoPage.kt` y `InfoTranslation`
2. ✅ Crear `InfoTool.kt`
3. ✅ Crear/modificar `ProjectTools.kt`
4. ✅ Modificar `Project.kt` para incluir `tools`
5. ✅ Crear `InfoValidator.kt`
6. ✅ Crear `InfoUseCases.kt`
7. ✅ Crear `InfoUseCasesTest.kt`
8. ✅ Compilar y ejecutar tests
9. ✅ Validar persistencia

---

**Tiempo estimado:** 2-3 horas  
**Complejidad:** Media  
**Dependencias:** Project, Workspace, WorkspaceRepository

---

*Diseño completado - Listo para implementación*

