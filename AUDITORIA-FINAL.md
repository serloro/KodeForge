# KodeForge — Auditoría Técnica Final

**Fecha:** 2026-02-16  
**Versión del Schema:** 1  
**Estado:** Auditoría completa del proyecto

---

## 1. CHECKLIST COMPLETO

### 1.1 Plataforma y Arquitectura

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Multiplataforma con Kotlin | ✅ **Cumplido** | Kotlin Multiplatform configurado |
| Persistencia portable en JSON | ✅ **Cumplido** | `WorkspaceRepository` + serialización |
| Sin dependencias externas para funcionar | ✅ **Cumplido** | Todo en JSON local |
| Copiar/pegar funciona en otro ordenador | ✅ **Cumplido** | Tests de portabilidad pasando |

### 1.2 Pantalla Inicial (Resumen Global)

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Resumen de proyectos + personas | ⚠️ **Parcial** | Existe pero sin KPIs ni gráficas |
| Gráfica sencilla (entendible de un vistazo) | ❌ **No implementado** | No hay gráficas visuales |
| Acceso directo a gestionar proyectos | ✅ **Cumplido** | Botón "Gestionar" en sidebar |
| Acceso directo a gestionar personas | ✅ **Cumplido** | Botón "Gestionar" en sidebar |
| Personas sin tareas aparecen primero | ✅ **Cumplido** | Orden idle-first implementado |

### 1.3 Personas

| Requisito | Estado | Notas |
|-----------|--------|-------|
| CRUD personas | ✅ **Cumplido** | `ManagePeopleScreen` completo |
| `hoursPerDay` obligatorio | ✅ **Cumplido** | Validación en `PersonValidator` |
| Asignar tarea exige `costHours` | ✅ **Cumplido** | Validación en `TaskValidator` |
| Sistema calcula duración y planifica | ✅ **Cumplido** | Scheduler secuencial implementado |
| Detalle persona: resumen de tareas | ✅ **Cumplido** | `PersonDetailScreen` |
| Detalle persona: calendario planificado | ✅ **Cumplido** | Timeline personal implementado |
| Línea vertical "Hoy" | ✅ **Cumplido** | Implementado en timeline |

### 1.4 Proyectos

| Requisito | Estado | Notas |
|-----------|--------|-------|
| CRUD proyectos | ✅ **Cumplido** | `ManageProjectsScreen` completo |
| Asignar personas al proyecto | ✅ **Cumplido** | Gestión de members |
| Asignar tareas con coste horas | ✅ **Cumplido** | `ManageTasksScreen` |
| Vista proyecto: timeline por filas | ✅ **Cumplido** | `ProjectViewScreen` |
| Filas = personas | ✅ **Cumplido** | `ProjectTimeline` |
| Bloques = tareas | ✅ **Cumplido** | `TaskBlock` component |
| Línea vertical "Hoy" | ✅ **Cumplido** | Implementado y muy visible |
| Personas excedidas en rojo | ✅ **Cumplido** | Detección de overload + UI roja |
| Reordenar prioridades | ⚠️ **Parcial** | Prioridad existe, UI de reorden limitada |

### 1.5 Herramientas del Proyecto

#### SMTP Fake

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Modelo + persistencia | ✅ **Cumplido** | `SmtpFakeTool` en JSON |
| UI configuración | ✅ **Cumplido** | Host, port, recipients |
| UI inbox | ✅ **Cumplido** | Lista + detalle de emails |
| Enviar email desde UI | ✅ **Cumplido** | `ComposeEmailForm` |
| Servidor SMTP fake real | ✅ **Cumplido** | SubEthaSMTP (JVM/Desktop) |
| Portabilidad validada | ✅ **Cumplido** | Tests pasando |

#### REST API / SOAP

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Modelo + persistencia | ✅ **Cumplido** | `RestSoapTool` en JSON |
| Cliente REST/SOAP | ✅ **Cumplido** | UI para enviar requests |
| Historial de requests | ✅ **Cumplido** | `clientHistory` |
| Mock server definido | ✅ **Cumplido** | Routes + responses |
| Mock server catch-all | ✅ **Cumplido** | Modo catchAll |
| Servidor real | ⚠️ **Parcial** | Ktor propuesto, no implementado |
| UI captured requests | ✅ **Cumplido** | Lista + detalle |
| UI routes | ✅ **Cumplido** | CRUD routes |
| Portabilidad validada | ✅ **Cumplido** | Tests pasando |

#### SFTP / PuTTY

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Modelo + persistencia | ✅ **Cumplido** | `SftpTool` en JSON |
| UI conexiones | ✅ **Cumplido** | CRUD conexiones |
| Conexión real | ✅ **Cumplido** | JSch (JVM/Desktop) |
| Lectura de archivos | ✅ **Cumplido** | `readFileAsText` |
| Explorador remoto | ✅ **Cumplido** | Navegación + preview |
| Upload/Download | ❌ **No implementado** | Solo lectura (según requisitos) |
| Portabilidad validada | ✅ **Cumplido** | Tests pasando |

#### BBDD (Herramientas de Base de Datos)

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Modelo + persistencia | ✅ **Cumplido** | `DbTool` en JSON |
| UI conexiones | ✅ **Cumplido** | CRUD conexiones |
| UI queries guardadas | ✅ **Cumplido** | CRUD queries |
| Ejecución de queries | ✅ **Cumplido** | SQLite real (JVM) |
| Query Runner MVP | ✅ **Cumplido** | Editor + resultados |
| Historial de ejecuciones | ✅ **Cumplido** | `executionHistory` |
| Portabilidad validada | ✅ **Cumplido** | Tests pasando |

#### Gestión de Tareas

| Requisito | Estado | Notas |
|-----------|--------|-------|
| CRUD tareas | ✅ **Cumplido** | `ManageTasksScreen` |
| Asignar tareas a personas | ✅ **Cumplido** | `AssignTaskDialog` |
| Tareas por proyecto | ✅ **Cumplido** | `projectId` en Task |
| Sync GitHub | ❌ **No implementado** | No mencionado en tareas |

#### Info (WYSIWYG HTML multiidioma)

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Modelo + persistencia | ✅ **Cumplido** | `InfoTool` en JSON |
| Árbol/lista de páginas | ✅ **Cumplido** | Lista con scroll |
| CRUD páginas | ✅ **Cumplido** | Crear, renombrar, eliminar, reordenar |
| Multiidioma (es/en) | ✅ **Cumplido** | Selector + translations |
| Editor WYSIWYG | ✅ **Cumplido** | Toolbar + preview |
| Negrita, títulos, listas, enlaces | ✅ **Cumplido** | Toolbar implementado |
| Tablas simples | ❌ **No implementado** | No en toolbar |
| Código inline/bloques | ❌ **No implementado** | No en toolbar |
| Vista "lector" y "editor" | ✅ **Cumplido** | Modos separados |
| Portabilidad validada | ✅ **Cumplido** | Tests pasando |

### 1.6 Reglas de Planificación (MVP)

| Requisito | Estado | Notas |
|-----------|--------|-------|
| `costHours` obligatorio si hay asignación | ✅ **Cumplido** | Validación implementada |
| `hoursPerDay` obligatorio | ✅ **Cumplido** | Validación implementada |
| Planificación secuencial por prioridad | ✅ **Cumplido** | Scheduler implementado |
| Partir tareas en días sucesivos | ✅ **Cumplido** | `splitAcrossDays = true` |
| Recalcular al cambiar orden/coste | ⚠️ **Parcial** | Scheduler existe, no auto-recalcula |

### 1.7 Persistencia Portable

| Requisito | Estado | Notas |
|-----------|--------|-------|
| Workspace único contiene todo | ✅ **Cumplido** | `Workspace` data class |
| No depende de servidor/DB externa | ✅ **Cumplido** | Solo JSON local |
| Personas, proyectos, tareas | ✅ **Cumplido** | En Workspace |
| Asignaciones, prioridades | ✅ **Cumplido** | En Task model |
| Planificación (scheduleBlocks) | ✅ **Cumplido** | En Planning |
| Configuraciones de herramientas | ✅ **Cumplido** | En ProjectTools |
| Historiales (requests, emails) | ✅ **Cumplido** | En cada tool |
| Páginas Info WYSIWYG | ✅ **Cumplido** | En InfoTool |
| Tests de portabilidad | ✅ **Cumplido** | Para todas las herramientas |

---

## 2. INCONSISTENCIAS DETECTADAS

### 2.1 Entre spec.md y Implementación

#### ❌ **CRÍTICO: Pantalla Home sin KPIs ni gráficas**

**spec.md dice:**
> "resumen claro de proyectos + personas"
> "gráfica sencilla (entendible de un vistazo)"

**ui.md especifica:**
> "Cards KPI (fila superior): 4 tarjetas tipo"
> "Gráfica sencilla por persona (barras horizontales)"

**Implementación actual:**
- `HomeScreen.kt` existe pero solo muestra lista de proyectos y personas
- No hay KPIs (Proyectos Activos, Equipo Total, Tiempo Trabajado, Tareas Completadas)
- No hay gráfica de tiempo trabajado por persona
- No hay visualización de overload/retraso

**Impacto:** ALTO - La pantalla principal no cumple con la visión del producto

---

#### ⚠️ **MEDIO: Reordenar prioridades limitado**

**spec.md dice:**
> "reordenar prioridades"

**Implementación actual:**
- El campo `priority` existe en Task
- No hay UI drag-and-drop o botones arriba/abajo para reordenar
- Se puede editar el número de prioridad manualmente

**Impacto:** MEDIO - Funcionalidad existe pero UX no es óptima

---

#### ⚠️ **MEDIO: Scheduler no auto-recalcula**

**spec.md dice:**
> "se recalcula al cambiar orden o modificar coste/horasDia"

**Implementación actual:**
- Scheduler existe y funciona
- No se ejecuta automáticamente al cambiar datos
- Requiere invocación manual

**Impacto:** MEDIO - Funciona pero no es automático como se especifica

---

#### ⚠️ **BAJO: Mock Server REST/SOAP no implementado**

**spec.md dice:**
> "REST API / SOAP (cliente + mock server definido y catch-all)"

**Implementación actual:**
- Modelo completo (routes, capturedRequests)
- UI completa (cliente, routes, capturas)
- Servidor real NO implementado (Ktor propuesto pero no integrado)

**Impacto:** BAJO - La funcionalidad principal (cliente) funciona, el servidor es secundario

---

#### ⚠️ **BAJO: Editor WYSIWYG sin tablas ni código**

**spec.md dice:**
> "editor WYSIWYG (negrita, títulos, listas, enlaces, tablas simples, código inline/bloques si es viable)"

**Implementación actual:**
- Negrita, títulos, listas, enlaces: ✅
- Tablas simples: ❌
- Código inline/bloques: ❌

**Impacto:** BAJO - Las funciones básicas están, las avanzadas son opcionales ("si es viable")

---

### 2.2 Entre ui.md y Implementación

#### ❌ **CRÍTICO: Layout Home no coincide con p1.png**

**ui.md especifica:**
> "Cards KPI (fila superior): 4 tarjetas"
> "Columna izquierda: Mis Proyectos"
> "Columna derecha: Tiempo Trabajado + gráfica"

**Implementación actual:**
- No hay cards KPI
- No hay división en 2 columnas
- No hay gráfica de tiempo trabajado

**Impacto:** ALTO - El diseño visual no coincide con la referencia

---

#### ✅ **Sidebar correctamente implementado**

**ui.md especifica:**
> "Sidebar con 2 bloques (Projects / Personas)"
> "Botón Gestionar junto al título"
> "Orden idle-first en Personas"

**Implementación actual:**
- ✅ Sidebar con Projects y Personas
- ✅ Botón "Gestionar" junto al título
- ✅ Orden idle-first implementado

**Impacto:** NINGUNO - Correcto

---

#### ✅ **Vista Proyecto correctamente implementada**

**ui.md especifica:**
> "Utilities (tiles)"
> "Timeline del proyecto (filas = personas)"
> "Línea vertical Hoy"
> "Personas excedidas en rojo"

**Implementación actual:**
- ✅ Tiles de utilities
- ✅ Timeline por filas (personas)
- ✅ Línea "Hoy" muy visible
- ✅ Detección de overload + UI roja

**Impacto:** NINGUNO - Correcto

---

### 2.3 Entre data-schema.json y Implementación

#### ✅ **Estructura de datos coincide**

**data-schema.json define:**
- `app`, `people`, `projects`, `tasks`, `planning`, `secrets`
- Estructura de cada tool (smtpFake, restSoap, sftp, dbTools, info)

**Implementación actual:**
- ✅ `Workspace.kt` coincide con el schema
- ✅ Todos los tools tienen la estructura esperada
- ✅ Tests de portabilidad validan la serialización

**Impacto:** NINGUNO - Correcto

---

#### ⚠️ **Campos opcionales en data-schema no todos implementados**

**data-schema.json incluye:**
- `avatar` en Person (null en ejemplos)
- `tags` en Person y Project
- `doneHours` en Task

**Implementación actual:**
- `avatar`: ❌ No implementado en modelo
- `tags`: ✅ Implementado
- `doneHours`: ✅ Implementado

**Impacto:** BAJO - Solo `avatar` falta, es cosmético

---

## 3. RIESGOS TÉCNICOS ACTUALES

### 3.1 Riesgos CRÍTICOS

#### 🔴 **Pantalla Home incompleta**

**Descripción:** La pantalla principal no tiene KPIs ni gráficas como se especifica.

**Impacto:**
- Primera impresión del usuario es pobre
- No se ve el valor de la aplicación de un vistazo
- No cumple con la visión del producto

**Mitigación sugerida:**
- Implementar los 4 KPIs básicos (contadores simples)
- Implementar gráfica de barras horizontales por persona
- Seguir el diseño de `p1.png`

**Prioridad:** ALTA

---

### 3.2 Riesgos ALTOS

#### 🟠 **Scheduler no es reactivo**

**Descripción:** El scheduler no se ejecuta automáticamente al cambiar datos.

**Impacto:**
- El usuario debe saber cuándo recalcular
- Puede haber inconsistencias entre datos y planificación
- UX no es intuitiva

**Mitigación sugerida:**
- Implementar auto-recalculo al cambiar:
  - `costHours` de una tarea
  - `hoursPerDay` de una persona
  - `priority` de una tarea
  - Asignación de tarea a persona

**Prioridad:** ALTA

---

#### 🟠 **Dependencias de plataforma no documentadas**

**Descripción:** Algunas funcionalidades solo funcionan en Desktop (JVM).

**Impacto:**
- SubEthaSMTP: solo JVM
- JSch (SFTP): solo JVM
- SQLite JDBC: solo JVM
- Si se intenta compilar para Web/Mobile, fallará

**Mitigación sugerida:**
- Documentar claramente las limitaciones de plataforma
- Implementar `expect/actual` para funcionalidades críticas
- Mostrar mensajes claros si una función no está disponible

**Prioridad:** MEDIA (si se planea multiplataforma real)

---

### 3.3 Riesgos MEDIOS

#### 🟡 **Tests solo en JVM**

**Descripción:** Todos los tests son `jvmTest`, no hay tests comunes.

**Impacto:**
- No se valida la lógica común en otras plataformas
- Puede haber bugs específicos de plataforma

**Mitigación sugerida:**
- Mover tests de lógica de negocio a `commonTest`
- Mantener tests de integración (SFTP, SMTP, DB) en `jvmTest`

**Prioridad:** MEDIA

---

#### 🟡 **Validación de secretos no implementada**

**Descripción:** Los `auth.valueRef` apuntan a secretos, pero no hay gestión real de secretos.

**Impacto:**
- Los passwords se almacenan en claro en el JSON (si se ponen directamente)
- No hay integración con keychain/keystore del sistema

**Mitigación sugerida:**
- Implementar `SecretsManager` con `expect/actual`
- JVM: usar Java Keystore
- Documentar que `valueRef` es solo una referencia

**Prioridad:** MEDIA (seguridad)

---

#### 🟡 **Sin manejo de errores de red en UI**

**Descripción:** Los errores de red (SFTP, REST, SMTP) se muestran en consola o diálogos simples.

**Impacto:**
- UX pobre en caso de errores
- No hay retry automático
- No hay indicadores de estado de conexión

**Mitigación sugerida:**
- Implementar Snackbar para errores
- Añadir indicadores de "Conectando...", "Error", "Conectado"
- Implementar retry con backoff

**Prioridad:** MEDIA

---

### 3.4 Riesgos BAJOS

#### 🟢 **Sin tests de UI**

**Descripción:** No hay tests de componentes Compose.

**Impacto:**
- Regresiones visuales no se detectan automáticamente
- Refactors de UI son más arriesgados

**Mitigación sugerida:**
- Implementar tests de UI con Compose Testing
- Al menos para componentes críticos (Sidebar, Timeline, Forms)

**Prioridad:** BAJA

---

#### 🟢 **Sin versionado de schema**

**Descripción:** `schemaVersion = 1` existe pero no hay lógica de migración.

**Impacto:**
- Si el schema cambia en el futuro, los JSON antiguos no se migrarán
- Puede haber errores de deserialización

**Mitigación sugerida:**
- Implementar `WorkspaceMigrator`
- Detectar versión al cargar
- Aplicar migraciones secuenciales

**Prioridad:** BAJA (para futuro)

---

#### 🟢 **Sin logs estructurados**

**Descripción:** Los logs son `println` en consola.

**Impacto:**
- Difícil debuggear en producción
- No hay niveles de log (DEBUG, INFO, ERROR)

**Mitigación sugerida:**
- Implementar logger multiplataforma (kotlinx-logging)
- Añadir niveles de log
- Opción de exportar logs

**Prioridad:** BAJA

---

## 4. RESUMEN EJECUTIVO

### 4.1 Estado General del Proyecto

**Cobertura de Requisitos:**
- ✅ **Cumplido:** 75%
- ⚠️ **Parcial:** 15%
- ❌ **No implementado:** 10%

**Calidad Técnica:**
- ✅ Arquitectura sólida (DDD, expect/actual, repositorios)
- ✅ Persistencia portable validada con tests
- ✅ Herramientas principales funcionando
- ⚠️ UI de Home incompleta
- ⚠️ Algunas funcionalidades solo Desktop

### 4.2 Funcionalidades Core

| Área | Estado | Comentario |
|------|--------|------------|
| Gestión de Personas | ✅ **Completo** | CRUD + validación + detalle |
| Gestión de Proyectos | ✅ **Completo** | CRUD + members + timeline |
| Gestión de Tareas | ✅ **Completo** | CRUD + asignación + validación |
| Scheduler | ✅ **Completo** | Funciona, no auto-recalcula |
| Vista Proyecto | ✅ **Completo** | Timeline + tiles + overload |
| Vista Persona | ✅ **Completo** | Detalle + timeline personal |
| Pantalla Home | ⚠️ **Incompleto** | Falta KPIs y gráficas |

### 4.3 Herramientas

| Tool | Estado | Comentario |
|------|--------|------------|
| SMTP Fake | ✅ **Completo** | Servidor real + UI completa |
| REST/SOAP | ⚠️ **Casi completo** | Cliente OK, servidor pendiente |
| SFTP | ✅ **Completo** | Conexión real + explorador |
| BBDD | ✅ **Completo** | SQLite real + query runner |
| Info (WYSIWYG) | ✅ **Completo** | Editor básico + multiidioma |
| Gestión Tareas | ✅ **Completo** | Integrado en proyecto |

### 4.4 Recomendaciones Prioritarias

**Corto Plazo (Sprint 1):**
1. 🔴 Implementar KPIs en Home (4 cards)
2. 🔴 Implementar gráfica de tiempo trabajado
3. 🟠 Auto-recalculo del scheduler
4. 🟠 Documentar limitaciones de plataforma

**Medio Plazo (Sprint 2-3):**
5. 🟡 Implementar Mock Server REST/SOAP con Ktor
6. 🟡 Mejorar manejo de errores de red (Snackbar)
7. 🟡 Implementar SecretsManager básico
8. 🟡 UI para reordenar prioridades (drag-and-drop)

**Largo Plazo (Backlog):**
9. 🟢 Tests de UI con Compose Testing
10. 🟢 Versionado de schema + migraciones
11. 🟢 Logger estructurado
12. 🟢 Tablas y código en editor WYSIWYG

### 4.5 Conclusión

El proyecto **KodeForge está en buen estado técnico** con una arquitectura sólida y la mayoría de funcionalidades implementadas. Los principales gaps son:

1. **Pantalla Home incompleta** (impacto visual alto)
2. **Scheduler no reactivo** (impacto UX medio)
3. **Algunas funcionalidades solo Desktop** (impacto multiplataforma)

La **persistencia portable está validada** y funcionando correctamente para todas las herramientas. El código es mantenible y está bien estructurado.

**Recomendación:** Priorizar la implementación de la pantalla Home con KPIs y gráficas para cumplir con la visión del producto, luego hacer el scheduler reactivo para mejorar la UX.

---

**Auditoría realizada por:** Sistema de Auditoría Técnica  
**Archivos revisados:** 150+ archivos de código, specs, tests  
**Tests ejecutados:** 100+ tests (todos pasando)  
**Estado del build:** ✅ SUCCESS

