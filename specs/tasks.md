tasks.md (v2)
Tasks — KodeForge
T0 — KMP + JSON Workspace

Crear proyecto Kotlin Multiplatform (targets definidos).

Implementar “workspace” portable (carpeta/archivo JSON).

Versionado de schema JSON (campo schemaVersion).

Repositorio: load/save atómico (evitar corrupción).

T1 — UI base + sidebar con gestión

Header minimal: icono KodeForge + botón “Nuevo Proyecto”.

Sidebar: Projects con botón pequeño “Gestionar” al lado del título.

Sidebar: Personas con botón pequeño “Gestionar” al lado del título.

Scroll en Projects/Personas si excede altura.

Ordenación idle-first (personas sin tareas primero).

T2 — Home resumen global

Cards de métricas globales.

Gráfica sencilla combinada (carga personas + estado proyectos).

Selección desde sidebar actualiza el main.

T3 — CRUD Personas

CRUD personas + validación hoursPerDay > 0.

Pantalla “Gestionar Personas” (buscador + lista).

T4 — CRUD Proyectos

CRUD proyectos.

Pantalla “Gestionar Proyectos”.

T5 — Tareas + asignación + scheduler MVP

CRUD tareas (title, costHours, status, priority).

Asignar tarea a persona exige costHours.

Scheduler secuencial por persona (consume hoursPerDay por día).

Detalle persona: resumen + calendario.

T6 — Vista Proyecto timeline

Asignar personas al proyecto.

Asignar tareas del proyecto a personas.

Timeline filas=personas, bloques=tareas.

Línea “Hoy”.

Regla excedidos en rojo.

T7 — Tools: SMTP

SMTP Fake config + inbox + envío.

T8 — Tools: REST/SOAP

Cliente REST/SOAP + historial.

Mock server (catch-all + definido por JSON).

Viewer de requests.

T9 — Tools: SFTP

Conexiones SFTP + listado + lectura.

T10 — Tools: Task Manager + GitHub base

Campos avanzados + etiquetas.

Config base integración GitHub (placeholder).

T11 — Tools: DB

Conexiones + query runner + resultados.

T12 — Tools: Info WYSIWYG multiidioma

Modelo JSON para “Info Pages”:

id, title, order/tree

translations: { langCode: { html, updatedAt } }

UI: lista/árbol de páginas + crear/renombrar/borrar.

Editor WYSIWYG (modo editar) + visor (modo leer).

Selector de idioma por página.

Guardado en JSON workspace + persistencia portable garantizada.

T13 — QA + portabilidad

Tests scheduler + idle-first + carga/save JSON.

Validar “copiar JSON a otro equipo” reproduce exactamente.

Checklist final vs spec.md.