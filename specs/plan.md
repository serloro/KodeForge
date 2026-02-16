Plan de implementación — KodeForge
Fase 0 — Base multiplataforma + storage JSON

KMP (Kotlin Multiplatform) estructura

modelos base + repositorio JSON

“workspace” portable

Fase 1 — Shell UI

header: icono app + “Nuevo Proyecto”

sidebar:

Projects + botón Gestionar

Personas + botón Gestionar

scroll en listas

ordenación idle-first

main: home resumen global + gráfica

Fase 2 — Personas + planificador MVP

CRUD personas (hoursPerDay)

CRUD tareas (costHours)

scheduler secuencial

detalle persona + calendario

Fase 3 — Proyectos + timeline por filas

CRUD proyectos

asignación personas + tareas

timeline por persona

“Hoy” + excedidos en rojo

Fase 4 — Herramientas (por módulo)

SMTP

REST/SOAP

SFTP

Task Manager + integración base GitHub

DB Tools

Info WYSIWYG multiidioma

Fase 5 — Pulido + portabilidad

robustez JSON (migraciones de schema con versionado)

tests

validación final contra checklist