# ui.md — KodeForge (Wireframes + Comportamiento UI)

## Visual Reference (MANDATORY STYLE GUIDE)

- p1.png → Home / Resumen global
- p2.png → Modo Proyecto (timeline + utilities)

These images define:
- layout proportions
- card style
- spacing
- sidebar structure
- timeline visual structure

They do NOT define business logic.
Business logic is defined in spec.md.

## 0) Layout global (común)
### Header (minimal)
- Izquierda: **icono + nombre** (KodeForge)
- Derecha: botón primario **+ Nuevo Proyecto**
- (No usuario, no reports)

### Sidebar (columna izquierda)
Contiene 2 bloques principales, cada uno con:
- Título (Projects / Personas)
- Botón pequeño junto al título: **Gestionar** (icono engranaje o lápiz)
- Lista debajo con scroll independiente si hay muchos items

**Bloque 1 — Projects**
- Título: `Projects`  [Gestionar]
- Lista de proyectos (click selecciona proyecto y cambia modo)
- Proyecto activo: resaltado (background + borde)

**Bloque 2 — Personas**
- Título: `Personas`  [Gestionar]
- Lista de personas (click selecciona persona y va a detalle persona)
- Orden: **idle-first** (personas sin tareas activas arriba)
- Indicadores opcionales:
  - punto verde: libre / sin tareas
  - barra/mini-chip: “cargado”, “excedido”, “en progreso”
  - rojo si excedido

---

## 1) Pantalla Home — Resumen Global (p1.png)
**Objetivo:** ver de un vistazo el estado general.

### 1.1 Encabezado del contenido
- Título grande: `Resumen de Proyectos` o `Dashboard Global`
- Subtexto breve opcional: “Visión general de proyectos y carga de equipo”

### 1.2 Cards KPI (fila superior)
4 tarjetas tipo:
- Proyectos Activos
- Equipo Total
- Tiempo Trabajado (horas totales)
- Tareas Completadas
> Cada card: icono + valor grande + nota pequeña (últimos 7 días / promedio)

### 1.3 Cuerpo principal (2 columnas)
#### Columna izquierda (mayor ancho)
**Mis Proyectos**
- Lista tipo cards: proyecto + estado + barra progreso
- Tags: En progreso / Pausado / Atrasado
- Click en proyecto → entra a **Modo Proyecto**

#### Columna derecha
**Tiempo Trabajado**
- “Tiempo Trackeado” + “Promedio por persona”
- **Gráfica sencilla por persona**
  - Barras horizontales por persona
  - Segmento verde: planificado / en tiempo
  - Segmento rojo: excedido (overload o retraso)
  - Línea vertical “Hoy” si aplica a timeline reducido
- Resumen inferior: horas semana actual, nº sobrecargas, hoy, nº miembros

### 1.4 Estados importantes (Home)
- Sin proyectos: CTA “Nuevo Proyecto”
- Sin personas: CTA “Gestionar Personas”
- Sin tareas: mostrar personas idle destacadas

---

## 2) Pantalla Persona — Detalle (nuevo, consistente con el sistema)
**Entrada:** click en una persona desde sidebar.

### 2.1 Encabezado
- `Persona: {Nombre}`
- Chips: `hours/day` (ej. 6h/día) · `idle/on-track/excedido`

### 2.2 Resumen rápido
- Tareas activas / pendientes / completadas
- Horas planificadas (próximos 7-30 días)
- Horas realizadas (doneHours)
- Estimación de finalización de su cola (si aplica)

### 2.3 Calendario / Timeline personal
- Vista por semanas (horizontal)
- Bloques por tarea (con color por estado)
- Línea vertical: **Hoy**
- Si excede capacidad (más de hours/day en un día) resaltar en rojo ese tramo

### 2.4 Lista de tareas (debajo o lateral)
- Orden por prioridad
- Cada tarea: `costHours`, `doneHours`, estado, fecha estimada fin
- Acciones: cambiar prioridad, marcar progreso, reasignar

---

## 3) Modo Proyecto — Utilities + Timeline (p2.png adaptado a KodeForge)
**Entrada:** click en un proyecto desde sidebar o desde “Mis Proyectos”.

### 3.1 Header del contenido
- Breadcrumb: `KodeForge / {Proyecto}`
- Título: `Utilidades del Proyecto`
- Opcional: botón “Configurar Atajos” o “Ajustes del proyecto”

### 3.2 Sección “Utilities” (tiles)
Tiles (cards clicables):
- SMTP Fake
- REST API / SOAP
- SFTP / PuTTY
- BBDD
- Gestión de tareas
- **Info (WYSIWYG HTML multiidioma)**

> Nota: el tile “Tiempo” puede ser una vista/resumen del timeline del proyecto.

### 3.3 Timeline del proyecto (bloque central)
**Estructura:**
- Filas = personas asignadas al proyecto
- Columnas = días/semanas
- Bloques = tareas asignadas dentro del proyecto
- Línea vertical “Hoy” (muy visible)
- Personas excedidas:
  - nombre/row en rojo
  - segmentos sobrecargados en rojo

### 3.4 Panel inferior (resumen/actividad)
- Métricas clave del proyecto (horas, tareas, eficiencia, etc.)
- Acciones:
  - Asignar personas al proyecto
  - Asignar tareas (con coste horas)
  - Exportar (CSV) si interesa

---

## 4) Pantallas de Gestión (modales o páginas)
### 4.1 Gestionar Projects
- Lista + buscador + crear/editar/borrar
- Ver miembros, estado, tags
- Acción: “Abrir proyecto” (selecciona y entra)

### 4.2 Gestionar Personas
- Lista + buscador + crear/editar/borrar
- Campo obligatorio: **Horas al día**
- Vista rápida: estado (idle / cargado / excedido)

---

## 5) Tool: Info (WYSIWYG HTML multiidioma)
**Dentro del proyecto** → tile “Info”.

### 5.1 Estructura
- Panel izquierdo: árbol/lista de páginas
- Panel derecho: visor/editor WYSIWYG

### 5.2 Multiidioma
- Selector de idioma arriba (ES / EN / ...)
- Cada idioma mantiene su HTML
- Botón “duplicar desde idioma X” (útil para traducir)

### 5.3 Persistencia portable
- Todo se guarda en JSON del workspace
- Copiar el workspace a otro equipo reproduce el árbol, idioma y contenido

---

## 6) Reglas UI clave (para que Copilot/Cursor lo respete)
- Sidebar con scroll independiente en Projects y Personas
- Botón **Gestionar** junto al título (no abajo)
- Orden idle-first en Personas
- “Hoy” siempre visible en timelines
- Excedidos resaltados en rojo de forma consistente