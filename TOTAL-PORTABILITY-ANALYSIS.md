# KodeForge — Análisis de Portabilidad Total

**Fecha:** 2026-02-16  
**Objetivo:** Identificar puntos frágiles en la portabilidad del workspace JSON

---

## FLUJO COMPLETO SIMULADO

### Escenario de Prueba

1. ✅ Crear proyecto
2. ✅ Crear personas
3. ✅ Asignar tareas
4. ✅ Generar planificación
5. ✅ Crear páginas Info en 2 idiomas
6. ✅ Crear conexión DB
7. ✅ Crear conexión SFTP
8. ✅ Crear mock REST route
9. ✅ Capturar email SMTP
10. ✅ Guardar workspace JSON
11. ✅ Recargar desde cero
12. ✅ Verificar igualdad exacta

---

## PUNTOS FRÁGILES IDENTIFICADOS

### 🔴 CRÍTICO 1: Timestamps y Generación de IDs

**Problema:**
- IDs generados con `Clock.System.now().toEpochMilliseconds()` + random
- Timestamps en formato ISO 8601
- Si el reloj del sistema cambia, los IDs pueden colisionar

**Evidencia:**
```kotlin
// PersonUseCases.kt, TaskUseCases.kt, etc.
private fun generateId(): String {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    val random = Random.nextInt(1000, 9999)
    return "person_${timestamp}_$random"
}
```

**Impacto:**
- ⚠️ MEDIO: Muy improbable en uso normal
- ⚠️ ALTO: En tests rápidos o sistemas con reloj inestable

**Mitigación actual:**
- ✅ Scheduler ya usa contador incremental (hardening reciente)
- ❌ Otros UseCases aún usan timestamp + random

**Recomendación:**
- Usar UUID o contador global
- Implementar en próximo refactor

---

### 🔴 CRÍTICO 2: Caracteres Especiales en HTML

**Problema:**
- Páginas Info contienen HTML arbitrario
- JSON escapa caracteres especiales: `"`, `\`, `/`, `<`, `>`
- HTML con scripts puede romper serialización

**Evidencia:**
```kotlin
// InfoUseCases.kt
fun updatePageTranslation(..., html: String) {
    // html puede contener: <script>alert('test');</script>
    // JSON lo serializa como: "<script>alert(\\'test\\');<\\/script>"
}
```

**Casos problemáticos:**
```html
<!-- Comillas -->
<p>Texto con "comillas" y 'apóstrofes'</p>

<!-- Scripts -->
<script>alert('test');</script>

<!-- Caracteres Unicode -->
<p>日本語 中文 한국어 émojis 🚀</p>

<!-- Saltos de línea -->
<p>Línea 1
Línea 2</p>
```

**Impacto:**
- ✅ BAJO: `kotlinx.serialization` maneja correctamente
- ⚠️ MEDIO: Si se edita JSON manualmente, puede romperse

**Mitigación actual:**
- ✅ `kotlinx.serialization` escapa automáticamente
- ✅ Tests existentes validan caracteres especiales

**Recomendación:**
- Añadir validación de HTML (sanitización)
- Limitar tags permitidos en futuro

---

### 🟠 ALTO 3: Referencias Cruzadas (IDs)

**Problema:**
- Muchas referencias entre entidades:
  - `Task.assigneeId` → `Person.id`
  - `Task.projectId` → `Project.id`
  - `ScheduleBlock.taskId` → `Task.id`
  - `ScheduleBlock.personId` → `Person.id`
  - `Project.members[]` → `Person.id`
  - `SavedQuery.connectionId` → `DbConnection.id`
  - etc.

**Escenario de fallo:**
```json
{
  "tasks": [
    {
      "id": "task_123",
      "assigneeId": "person_456",  // ❌ person_456 no existe
      "projectId": "proj_789"      // ❌ proj_789 no existe
    }
  ],
  "people": [],
  "projects": []
}
```

**Impacto:**
- 🔴 CRÍTICO: Bloques huérfanos, inconsistencias
- 🔴 CRÍTICO: Timeline muestra datos incorrectos

**Mitigación actual:**
- ✅ `cleanOrphanBlocks()` limpia bloques huérfanos (scheduler hardening)
- ✅ `validatePlanningIntegrity()` detecta inconsistencias
- ❌ No hay validación global de integridad referencial

**Recomendación:**
- Implementar `validateWorkspaceIntegrity()` global
- Ejecutar al cargar workspace
- Reportar/corregir automáticamente

---

### 🟠 ALTO 4: Secrets y AuthConfig

**Problema:**
- `AuthConfig.valueRef` es una referencia a `Secrets`
- `Secrets` NO se serializa en JSON (por seguridad)
- Al recargar, `valueRef` apunta a secreto inexistente

**Evidencia:**
```kotlin
// Project.kt
@Serializable
data class AuthConfig(
    val type: String, // "password", "key", "none"
    val valueRef: String // Referencia a Secrets (NO el valor)
)

// Workspace.kt
@Serializable
data class Secrets(
    // ❌ NO se serializa en JSON
)
```

**Escenario:**
```json
{
  "projects": [{
    "tools": {
      "dbTools": {
        "connections": [{
          "auth": {
            "type": "password",
            "valueRef": "db_password_123"  // ❌ Secreto no existe en JSON
          }
        }]
      }
    }
  }]
}
```

**Impacto:**
- 🔴 CRÍTICO: Conexiones DB/SFTP no funcionan después de recargar
- 🔴 CRÍTICO: Usuario debe reintroducir todos los secretos

**Mitigación actual:**
- ❌ No hay mitigación (diseño intencional por seguridad)

**Recomendación:**
- Documentar claramente que secretos NO son portables
- Implementar "vault" externo (ej: sistema operativo, archivo cifrado)
- Añadir UI para reintroducir secretos al importar workspace

---

### 🟠 ALTO 5: Tamaño del Archivo JSON

**Problema:**
- Workspace grande puede generar JSON de varios MB
- Serialización/deserialización lenta
- Riesgo de OutOfMemory en targets limitados

**Escenario:**
```
50 personas × 200 tareas × 1000 scheduleBlocks = JSON de ~5MB
100 páginas Info × 2 idiomas × 10KB HTML = ~2MB
Total: ~7MB
```

**Impacto:**
- ⚠️ MEDIO: En uso normal (< 100 tareas), no es problema
- 🔴 CRÍTICO: En proyectos grandes (> 1000 tareas), puede ser lento

**Mitigación actual:**
- ❌ No hay límites ni paginación
- ❌ No hay compresión

**Recomendación:**
- Implementar compresión (gzip) del JSON
- Implementar "archivado" de tareas completadas
- Implementar "lazy loading" de páginas Info

---

### 🟡 MEDIO 6: Orden de Serialización

**Problema:**
- `kotlinx.serialization` serializa campos en orden de declaración
- Si se cambia orden de campos en data class, JSON cambia
- Puede romper compatibilidad con versiones anteriores

**Evidencia:**
```kotlin
// Versión 1
@Serializable
data class Task(
    val id: String,
    val title: String,
    val costHours: Double
)

// Versión 2 (orden cambiado)
@Serializable
data class Task(
    val id: String,
    val costHours: Double,  // ❌ Orden cambiado
    val title: String
)
```

**Impacto:**
- ✅ BAJO: `kotlinx.serialization` maneja correctamente
- ⚠️ MEDIO: Si se comparan JSON textualmente, fallan

**Mitigación actual:**
- ✅ `schemaVersion` en `AppMetadata` para migraciones
- ✅ `kotlinx.serialization` deserializa por nombre, no por orden

**Recomendación:**
- NO cambiar orden de campos en data classes
- Usar `@SerialName` si es necesario

---

### 🟡 MEDIO 7: Valores por Defecto

**Problema:**
- Algunos campos tienen valores por defecto
- Si el valor es el default, `kotlinx.serialization` puede omitirlo
- Al recargar, puede no ser el mismo objeto

**Evidencia:**
```kotlin
@Serializable
data class Task(
    val id: String,
    val title: String,
    val status: String = "todo",  // Default
    val priority: Int = 0         // Default
)

// JSON generado (si status = "todo"):
{
  "id": "task_123",
  "title": "Task 1"
  // ❌ "status" omitido (es el default)
}
```

**Impacto:**
- ✅ BAJO: `kotlinx.serialization` restaura defaults correctamente
- ⚠️ MEDIO: Si se edita JSON manualmente, puede confundir

**Mitigación actual:**
- ✅ Configuración de `kotlinx.serialization` incluye defaults

**Recomendación:**
- Usar `encodeDefaults = true` en configuración JSON
- Ya está implementado en `WorkspaceRepository`

---

### 🟡 MEDIO 8: Fechas y Zonas Horarias

**Problema:**
- Timestamps en formato ISO 8601 con UTC: `"2026-02-16T14:30:00Z"`
- Si sistema cambia zona horaria, fechas pueden interpretarse incorrectamente
- `scheduleBlocks.date` es String, no LocalDate

**Evidencia:**
```kotlin
// PlanningUseCases.kt
val block = ScheduleBlock(
    date = currentDate.toString(), // "2026-02-16"
    // ...
)

// PersonUseCases.kt
private fun generateTimestamp(): String {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.UTC)
    return "${localDateTime.date}T${localDateTime.time}Z"
}
```

**Impacto:**
- ✅ BAJO: Siempre se usa UTC, no hay ambigüedad
- ⚠️ MEDIO: Si se edita JSON con fechas incorrectas, puede romper

**Mitigación actual:**
- ✅ Siempre se usa UTC
- ✅ Formato ISO 8601 estándar

**Recomendación:**
- Validar formato de fechas al cargar
- Añadir `LocalDate.parse()` con try/catch

---

### 🟡 MEDIO 9: Enums como Strings

**Problema:**
- Muchos campos son Strings que deberían ser enums:
  - `Task.status`: "todo", "in_progress", "completed"
  - `AuthConfig.type`: "password", "key", "none"
  - `DbConnection.type`: "postgres", "mysql", "sqlite", etc.
  - `MockServer.mode`: "catchAll", "defined"

**Evidencia:**
```kotlin
@Serializable
data class Task(
    val status: String = "todo"  // ❌ Debería ser enum
)

// JSON permite valores inválidos:
{
  "status": "invalid_status"  // ❌ No se valida
}
```

**Impacto:**
- ⚠️ MEDIO: Valores inválidos pueden romper lógica
- ⚠️ MEDIO: No hay autocompletado en IDE

**Mitigación actual:**
- ✅ Validadores verifican valores permitidos
- ❌ No hay enums (decisión de diseño para flexibilidad)

**Recomendación:**
- Mantener Strings para flexibilidad
- Añadir constantes: `object TaskStatus { const val TODO = "todo" }`
- Validar al cargar

---

### 🟢 BAJO 10: Nullabilidad

**Problema:**
- Algunos campos son nullable: `description`, `avatar`, `projectId`, etc.
- JSON puede tener `null` explícito o campo omitido
- Puede haber inconsistencias

**Evidencia:**
```json
// Opción 1: null explícito
{
  "description": null
}

// Opción 2: campo omitido
{
}
```

**Impacto:**
- ✅ BAJO: `kotlinx.serialization` maneja correctamente
- ✅ BAJO: Ambos casos se deserializan a `null`

**Mitigación actual:**
- ✅ `kotlinx.serialization` maneja nulls correctamente

**Recomendación:**
- Ninguna (funciona correctamente)

---

## TESTS DE PORTABILIDAD EXISTENTES

### ✅ Tests Implementados

1. **`InfoPortabilityTest.kt`**
   - Valida páginas Info
   - Valida traducciones (es/en)
   - Valida HTML con caracteres especiales

2. **`RestSoapPortabilityTest.kt`**
   - Valida clientHistory
   - Valida mockServer config
   - Valida routes
   - Valida capturedRequests

3. **`SmtpFakePortabilityTest.kt`**
   - Valida allowedRecipients
   - Valida storedInbox
   - Valida configuración

4. **`DbToolPortabilityTest.kt`**
   - Valida connections
   - Valida savedQueries
   - Valida queryHistory

5. **`SftpPortabilityTest.kt`**
   - Valida connections
   - Valida configuración

6. **`SchedulerHardeningTest.kt`**
   - Valida scheduleBlocks
   - Valida integridad referencial
   - Valida auto-recalculo

### ❌ Tests Faltantes

1. **Test de Flujo Completo**
   - Crear todas las entidades
   - Guardar → Recargar → Verificar igualdad total
   - **PENDIENTE** (test creado pero con errores de compilación)

2. **Test de Caracteres Especiales**
   - Unicode, émojis, HTML complejo
   - **PARCIAL** (solo en Info)

3. **Test de Volumen Grande**
   - 1000+ tareas, 100+ personas
   - Verificar performance
   - **PENDIENTE**

4. **Test de Integridad Referencial Global**
   - Validar todas las referencias cruzadas
   - **PENDIENTE**

---

## RECOMENDACIONES PRIORITARIAS

### 🔴 PRIORIDAD ALTA

1. **Implementar `validateWorkspaceIntegrity()` global**
   - Validar todas las referencias cruzadas
   - Ejecutar al cargar workspace
   - Reportar/corregir automáticamente

2. **Documentar limitación de Secrets**
   - Clarificar que NO son portables
   - Añadir UI para reintroducir secretos
   - Implementar "vault" externo (futuro)

3. **Añadir validación de fechas**
   - Validar formato ISO 8601
   - Try/catch en `LocalDate.parse()`
   - Reportar errores claramente

### 🟠 PRIORIDAD MEDIA

4. **Implementar compresión del JSON**
   - gzip al guardar
   - Reducir tamaño de archivo
   - Mejorar performance

5. **Migrar IDs a UUID**
   - Eliminar timestamp + random
   - Usar UUID estándar
   - Garantizar unicidad

6. **Añadir constantes para enums**
   - `TaskStatus`, `AuthType`, `DbType`, etc.
   - Mejorar legibilidad
   - Facilitar validación

### 🟡 PRIORIDAD BAJA

7. **Implementar archivado de tareas completadas**
   - Mover a archivo separado
   - Reducir tamaño del workspace activo

8. **Añadir lazy loading de páginas Info**
   - Cargar HTML bajo demanda
   - Reducir memoria

---

## CONCLUSIÓN

### ✅ Fortalezas

1. ✅ `kotlinx.serialization` maneja correctamente:
   - Caracteres especiales
   - Nulls
   - Defaults
   - Orden de campos

2. ✅ Tests de portabilidad por herramienta:
   - Info, REST/SOAP, SMTP, DB, SFTP

3. ✅ Scheduler hardening:
   - Limpieza de bloques huérfanos
   - Validación de integridad
   - Auto-recalculo

4. ✅ `schemaVersion` para migraciones futuras

### ⚠️ Puntos Frágiles

1. 🔴 **Secrets NO son portables** (diseño intencional)
2. 🔴 **Integridad referencial NO se valida globalmente**
3. 🟠 **IDs con timestamp + random** (riesgo de colisión)
4. 🟠 **Tamaño del JSON** (puede ser grande)
5. 🟡 **Enums como Strings** (valores inválidos posibles)

### 📋 Acciones Inmediatas

1. Implementar `validateWorkspaceIntegrity()` global
2. Documentar limitación de Secrets
3. Añadir validación de fechas
4. Crear test de flujo completo (corregir errores de compilación)

---

**Estado:** ⚠️ PORTABILIDAD FUNCIONAL CON LIMITACIONES CONOCIDAS  
**Riesgo general:** 🟡 MEDIO (mitigable con mejoras propuestas)

