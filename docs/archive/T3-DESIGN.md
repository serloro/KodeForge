# T3 - CRUD Personas - Estructura de Implementación

## 📋 Análisis de Especificaciones

### **spec.md - Criterios:**
- ✅ "CRUD personas con hoursPerDay obligatorio"
- ✅ "Asignar tarea exige costHours" (no aplicable en T3, es T5)
- ✅ "Detalle persona: resumen + calendario planificado" (no aplicable en T3, es T5)

### **tasks.md - T3:**
- ✅ "CRUD personas + validación hoursPerDay > 0"
- ✅ "Pantalla 'Gestionar Personas' (buscador + lista)"

### **Modelo Person existente:**
```kotlin
data class Person(
    val id: String,                    // REQUIRED
    val displayName: String,           // REQUIRED
    val avatar: String? = null,        // OPTIONAL
    val role: String? = null,          // OPTIONAL
    val hoursPerDay: Double,           // REQUIRED > 0
    val active: Boolean = true,        // DEFAULT true
    val tags: List<String> = emptyList(), // DEFAULT []
    val meta: PersonMeta = PersonMeta() // DEFAULT
)
```

---

## 🏗️ Arquitectura de Implementación

### **1. Capa Domain (Validación)**
```
PersonValidator.kt
├─ validateCreate(name, hoursPerDay): Result<ValidationError>
├─ validateUpdate(person, name?, hoursPerDay?): Result<ValidationError>
└─ rules:
   - displayName: no vacío, trim, max 100 chars
   - hoursPerDay: > 0, <= 24, no NaN/Infinity
   - role: opcional, max 50 chars
   - tags: max 20 tags, cada uno max 30 chars
```

### **2. Capa Use Cases**
```
PersonUseCases.kt
├─ createPerson(name, hoursPerDay, role?, avatar?, tags?)
│  ├─ Valida datos
│  ├─ Genera ID único (UUID)
│  ├─ Genera createdAt (ISO 8601)
│  ├─ Crea Person
│  └─ Actualiza workspace
│
├─ updatePerson(id, name?, hoursPerDay?, role?, avatar?, tags?, active?)
│  ├─ Valida datos
│  ├─ Busca persona existente
│  ├─ Actualiza campos modificados
│  └─ Actualiza workspace
│
├─ deletePerson(id)
│  ├─ Busca persona existente
│  ├─ Verifica si tiene tareas asignadas (warning, no bloquea)
│  └─ Elimina del workspace
│
└─ searchPeople(query): List<Person>
   ├─ Busca en displayName, role, tags
   └─ Case-insensitive
```

### **3. Capa UI - Pantalla "Gestionar Personas"**
```
ManagePeopleScreen.kt
├─ Lista de personas (scrollable)
│  ├─ Buscador en header
│  ├─ Botón "+ Crear Persona"
│  ├─ Cada item:
│  │  ├─ Avatar/inicial
│  │  ├─ Nombre + role
│  │  ├─ hoursPerDay (badge)
│  │  ├─ Botón "Editar"
│  │  └─ Botón "Eliminar"
│  └─ Empty state si no hay personas
│
└─ Modal Create/Edit
   ├─ Formulario:
   │  ├─ displayName (TextField, REQUIRED)
   │  ├─ hoursPerDay (TextField numérico, REQUIRED, > 0)
   │  ├─ role (TextField, opcional)
   │  ├─ tags (TextField separado por comas, opcional)
   │  └─ active (Checkbox, default true)
   ├─ Validación en tiempo real
   ├─ Botón "Guardar" / "Actualizar"
   └─ Botón "Cancelar"
```

### **4. Integración con Workspace**
```
HomeScreen.kt
├─ onClick "Gestionar Personas"
│  └─ Navega a ManagePeopleScreen
│
ManagePeopleScreen.kt
├─ Recibe: workspace, onWorkspaceUpdate
├─ Usa: PersonUseCases para CRUD
└─ Actualiza: workspace.people
```

---

## 📁 Archivos a Crear/Modificar

### **Nuevos:**
1. `src/commonMain/kotlin/com/kodeforge/domain/validation/PersonValidator.kt`
2. `src/commonMain/kotlin/com/kodeforge/domain/usecases/PersonUseCases.kt`
3. `src/commonMain/kotlin/com/kodeforge/ui/screens/ManagePeopleScreen.kt`
4. `src/commonMain/kotlin/com/kodeforge/ui/components/PersonForm.kt`
5. `src/commonMain/kotlin/com/kodeforge/ui/components/PersonListItem.kt`

### **Modificados:**
1. `src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`
   - onClick "Gestionar Personas" → abrir ManagePeopleScreen

---

## 🎨 UI Layout (Simple y Funcional)

```
┌─────────────────────────────────────────┐
│ ← Gestionar Personas    [+ Crear]      │
├─────────────────────────────────────────┤
│ 🔍 [Buscar personas...]                │
├─────────────────────────────────────────┤
│                                         │
│ ⚪ Basso7           [Editar] [×]       │
│    Developer · 8h/día                   │
│                                         │
│ ⚪ Blanco J         [Editar] [×]       │
│    Designer · 6h/día                    │
│                                         │
│ ⚪ Bocera J         [Editar] [×]       │
│    QA · 7h/día                          │
│                                         │
└─────────────────────────────────────────┘

Modal Crear/Editar:
┌─────────────────────────────────┐
│ Crear Persona              [×]  │
├─────────────────────────────────┤
│ Nombre *                        │
│ [___________________________]   │
│                                 │
│ Horas por día *                 │
│ [_______] (ej: 8)               │
│                                 │
│ Rol (opcional)                  │
│ [___________________________]   │
│                                 │
│ Tags (opcional)                 │
│ [___________________________]   │
│ (separados por comas)           │
│                                 │
│ ☑ Activo                        │
│                                 │
│     [Cancelar]  [Guardar]       │
└─────────────────────────────────┘
```

---

## ✅ Validaciones Implementadas

| Campo | Validación | Mensaje Error |
|-------|------------|---------------|
| displayName | No vacío | "El nombre es obligatorio" |
| | Max 100 chars | "Nombre muy largo (max 100)" |
| hoursPerDay | > 0 | "Debe ser mayor a 0" |
| | <= 24 | "Máximo 24 horas por día" |
| | Numérico válido | "Valor numérico inválido" |
| role | Max 50 chars | "Rol muy largo (max 50)" |
| tags | Max 20 tags | "Máximo 20 tags" |
| | Cada tag max 30 chars | "Tag muy largo (max 30)" |

---

## 🔄 Flujo de Datos

### **Crear Persona:**
```
UI Form → PersonUseCases.createPerson()
       → PersonValidator.validateCreate()
       → Genera ID (UUID) + createdAt (ISO 8601)
       → Crea Person
       → workspace.copy(people = people + newPerson)
       → onWorkspaceUpdate(newWorkspace)
       → WorkspaceRepository.save()
```

### **Editar Persona:**
```
UI Form → PersonUseCases.updatePerson()
       → PersonValidator.validateUpdate()
       → Busca persona por ID
       → Actualiza campos modificados
       → workspace.copy(people = peopleUpdated)
       → onWorkspaceUpdate(newWorkspace)
       → WorkspaceRepository.save()
```

### **Eliminar Persona:**
```
UI Button → PersonUseCases.deletePerson()
         → Busca persona por ID
         → (Opcional: warning si tiene tareas)
         → workspace.copy(people = people - person)
         → onWorkspaceUpdate(newWorkspace)
         → WorkspaceRepository.save()
```

---

## 🚫 Fuera del Alcance (T3)

- ❌ Detalle de persona (pantalla individual) → T5
- ❌ Calendario de persona → T5
- ❌ Asignación de tareas → T5
- ❌ Gestión de avatar (subir imagen) → Futuro
- ❌ Gestión avanzada de tags (autocompletado) → Futuro

---

## 📊 Criterios de Validación (T3)

| Criterio | Implementado |
|----------|--------------|
| CRUD completo (Create, Read, Update, Delete) | ✅ |
| Validación hoursPerDay > 0 | ✅ |
| Pantalla "Gestionar Personas" | ✅ |
| Buscador | ✅ |
| Lista de personas | ✅ |
| Persistencia en workspace JSON | ✅ |
| Campos según spec.md | ✅ |
| Sin campos adicionales no definidos | ✅ |

---

## ⏭️ Preparación para T5

El diseño permite fácil extensión para T5:
- ✅ `PersonUseCases` listo para añadir asignación de tareas
- ✅ `Person.active` preparado para filtrar en scheduling
- ✅ `Person.tags` preparado para filtrado avanzado
- ✅ Navegación a detalle de persona (solo falta implementar pantalla)

---

**Siguiente paso:** Implementación del código.

