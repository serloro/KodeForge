# T6 - CRUD Proyectos + Miembros - Diseño

**Objetivo:** Implementar gestión completa de proyectos (crear, editar, eliminar) y asignación de personas como miembros.

**Alcance:** SOLO CRUD + miembros. NO timeline, NO asignación de tareas, NO tools.

---

## 📋 REQUISITOS

### **CRUD Proyectos:**
- Crear proyecto (nombre, descripción, status)
- Editar proyecto
- Eliminar proyecto
- Validaciones: nombre obligatorio

### **Gestión de Miembros:**
- Asignar personas a proyecto (members)
- Quitar personas de proyecto
- Ver lista de miembros

### **Persistencia:**
- Guardar en workspace.projects
- Actualizar project.members (lista de IDs de personas)

---

## 🏗️ ARQUITECTURA

### **Validador:**
```
ProjectValidator
├── validate(project: Project): List<String>
│   ├── Nombre obligatorio
│   ├── Nombre max 100 chars
│   ├── Status válido (active, paused, completed)
│   └── Descripción max 500 chars
```

### **Use Cases:**
```
ProjectUseCases
├── createProject(workspace, name, description, status): Pair<Workspace, List<String>>
├── updateProject(workspace, projectId, name, description, status, members): Pair<Workspace, List<String>>
├── deleteProject(workspace, projectId): Workspace
├── addMember(workspace, projectId, personId): Pair<Workspace, List<String>>
├── removeMember(workspace, projectId, personId): Workspace
└── searchProjects(workspace, query): List<Project>
```

### **UI Components:**
```
ProjectForm
├── Campos: name*, description, status, members
├── Validación en tiempo real
└── Dropdown para status

ProjectListItem
├── Avatar proyecto (inicial)
├── Nombre + descripción
├── Badge status
├── Contador miembros
└── Botones: Editar | Eliminar

MemberSelector
├── Lista de personas disponibles
├── Checkbox por persona
└── Botón "Guardar"
```

### **Pantalla:**
```
ManageProjectsScreen
├── TopAppBar (← Volver)
├── SearchBar
├── Lista de proyectos (LazyColumn)
├── FloatingActionButton (+ Crear)
└── Diálogos: Create, Edit, Delete, Members
```

---

## 📊 MODELO DE DATOS

### **Project (ya existe en Workspace.kt):**
```kotlin
@Serializable
data class Project(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String = "active", // active, paused, completed
    val members: List<String> = emptyList(), // List of person IDs
    val createdAt: String,
    val updatedAt: String,
    val tools: ProjectTools = ProjectTools()
)
```

**Campos a gestionar en T6:**
- ✅ `id` - Auto-generado
- ✅ `name` - Obligatorio
- ✅ `description` - Opcional
- ✅ `status` - Dropdown (active, paused, completed)
- ✅ `members` - Lista de person IDs
- ✅ `createdAt` - Auto-generado
- ✅ `updatedAt` - Auto-actualizado
- ⚠️ `tools` - No tocar (T7)

---

## 🎨 UI DISEÑO

### **ManageProjectsScreen:**
```
┌─────────────────────────────────────────────┐
│ ← Gestionar Proyectos      [+ Crear]       │
├─────────────────────────────────────────────┤
│ 🔍 [Buscar proyectos...]                   │
├─────────────────────────────────────────────┤
│                                             │
│ ⚪ Cloud Scale UI           [✏️] [🗑️]      │
│    Sistema de gestión cloud                 │
│    ✅ Activo · 3 miembros                   │
│                                             │
│ ⚪ Mobile App Redesign      [✏️] [🗑️]      │
│    Rediseño completo de la app móvil        │
│    ⏸️ Pausado · 2 miembros                  │
│                                             │
└─────────────────────────────────────────────┘
```

### **ProjectForm (Crear/Editar):**
```
┌─────────────────────────────────┐
│ Crear Proyecto           [×]    │
├─────────────────────────────────┤
│ Nombre *                        │
│ [___________________________]   │
│                                 │
│ Descripción (opcional)          │
│ [___________________________]   │
│ [___________________________]   │
│                                 │
│ Estado                          │
│ [Activo ▼]                      │
│   • Activo                      │
│   • Pausado                     │
│   • Completado                  │
│                                 │
│ Miembros (3 seleccionados)      │
│ [Gestionar Miembros →]          │
│                                 │
│     [Cancelar]  [Guardar]       │
└─────────────────────────────────┘
```

### **MemberSelector:**
```
┌─────────────────────────────────┐
│ Miembros del Proyecto    [×]    │
├─────────────────────────────────┤
│ Selecciona personas:            │
│                                 │
│ ☑ Basso7 (Dev)                  │
│ ☑ Blanco J (Designer)           │
│ ☐ Bocera J (QA)                 │
│ ☐ García M (PM)                 │
│                                 │
│     [Cancelar]  [Guardar]       │
└─────────────────────────────────┘
```

---

## ✅ VALIDACIONES

### **ProjectValidator:**

| Campo | Validación | Mensaje |
|-------|------------|---------|
| name | No vacío | "El nombre es obligatorio" |
| name | Max 100 chars | "Nombre muy largo (max 100)" |
| description | Max 500 chars | "Descripción muy larga (max 500)" |
| status | En lista válida | "Estado no válido" |
| members | IDs existen en workspace | "Persona no encontrada" |

---

## 🔄 FLUJO DE DATOS

### **Crear Proyecto:**
```
UI Form → ProjectUseCases.createProject()
       → ProjectValidator.validate() ✅
       → Genera ID (proj_1708098534234_4562) ✅
       → Genera createdAt/updatedAt (ISO 8601) ✅
       → workspace.copy(projects = projects + newProject) ✅
       → onWorkspaceUpdate(newWorkspace) ✅
       → WorkspaceRepository.save() ✅
```

### **Editar Proyecto:**
```
UI Form → ProjectUseCases.updateProject()
       → ProjectValidator.validate() ✅
       → Actualiza solo campos modificados ✅
       → updatedAt = now() ✅
       → workspace.copy(projects = projectsUpdated) ✅
```

### **Asignar Miembro:**
```
MemberSelector → ProjectUseCases.addMember()
              → Valida persona existe ✅
              → Añade personId a project.members ✅
              → workspace.copy(projects = projectsUpdated) ✅
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/validation/ProjectValidator.kt`**
   - Validaciones de Project

2. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/ProjectUseCases.kt`**
   - CRUD + gestión de miembros

3. **`src/commonMain/kotlin/com/kodeforge/ui/components/ProjectForm.kt`**
   - Formulario Create/Edit

4. **`src/commonMain/kotlin/com/kodeforge/ui/components/ProjectListItem.kt`**
   - Item de lista con badges

5. **`src/commonMain/kotlin/com/kodeforge/ui/components/MemberSelector.kt`**
   - Selector de miembros

6. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ManageProjectsScreen.kt`**
   - Pantalla completa

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`**
   - Navegación a ManageProjectsScreen
   - onClick "Gestionar Proyectos" en sidebar

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| CRUD proyectos | Create, Update, Delete |
| Validación nombre obligatorio | ProjectValidator |
| Asignar/quitar miembros | addMember(), removeMember() |
| Persistencia JSON | workspace.projects |
| Pantalla "Gestionar Proyectos" | ManageProjectsScreen |
| Búsqueda | searchProjects() |
| NO timeline | Correcto, no implementar |
| NO asignación tareas | Correcto, no implementar |
| NO tools | Correcto, no tocar |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear ProjectValidator
2. ✅ Crear ProjectUseCases
3. ✅ Crear ProjectForm
4. ✅ Crear ProjectListItem
5. ✅ Crear MemberSelector
6. ✅ Crear ManageProjectsScreen
7. ✅ Modificar HomeScreen (navegación)
8. ✅ Validar contra specs/spec.md

---

**Tiempo estimado:** 3-4 horas  
**Complejidad:** Media  
**Dependencias:** WorkspaceRepository, Person, Project

---

*Diseño completado - Listo para implementación*

