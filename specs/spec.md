KodeForge — Spec (SDD)
1) Objetivo

Crear una app multiplataforma (Kotlin) para gestionar trabajo por proyectos y personas, basada en:

horas/día por persona

coste en horas por tarea

planificación automática en calendarios/timelines

y con un modo “Proyecto” que incluye un conjunto de herramientas (SMTP fake, REST/SOAP, SFTP, BBDD, Tareas, y Info WYSIWYG multiidioma).

2) Plataforma y arquitectura

Multiplataforma con Kotlin.

Persistencia portable: el estado completo de la app se guarda en JSON (sin dependencias externas para funcionar).

Principio: si el usuario copia/pega el directorio/archivo de datos en otro ordenador, todo queda igual (proyectos, personas, tareas, planificación, herramientas, páginas HTML, configuraciones, historiales).

3) Alcance funcional
3.1 Pantalla inicial (Resumen global)

Al abrir KodeForge:

resumen claro de proyectos + personas

gráfica sencilla (entendible de un vistazo)

accesos directos a:

gestionar proyectos

gestionar personas

Regla UX:

personas sin tareas aparecen primero.

3.2 Personas

Datos:

hoursPerDay obligatorio

Comportamiento:

al asignar tarea → se indica costHours

el sistema calcula duración y planifica en calendario

Detalle persona:

resumen de tareas + trabajo realizado

calendario con distribución automática de tareas (se ve carga y fecha fin)

3.3 Proyectos

Al seleccionar proyecto → UI cambia a modo proyecto:

timeline por filas (cada fila una persona)

tareas como bloques

línea vertical “Hoy”

personas excedidas resaltadas en rojo

Acciones:

asignar personas

asignar tareas (con coste horas)

reordenar prioridades

3.4 Herramientas del Proyecto (Utilities)

En modo proyecto hay herramientas:

SMTP Fake

REST API / SOAP (cliente + mock server definido y catch-all)

SFTP / PuTTY (conexión + lectura)

Gestión de tareas (con posible sync GitHub)

BBDD (conexiones + consultas)

Info (WYSIWYG HTML multiidioma)

3.4.6 Herramienta Info (WYSIWYG HTML multiidioma)

Objetivo: crear documentación interna por proyecto en forma de “páginas”.

Requisitos:

crear y organizar páginas (árbol o lista)

editor WYSIWYG (negrita, títulos, listas, enlaces, tablas simples, código inline/bloques si es viable)

almacenamiento interno como JSON

multiidioma por página:

una misma página puede tener variantes por idioma (es, en, …)

el usuario puede cambiar idioma y editar contenido específico

vista “lector” y vista “editor”

export/import implícito: al copiar el JSON, toda la documentación viaja con el proyecto

4) Reglas de planificación (MVP)

tareas: costHours obligatorio si hay asignación

persona: hoursPerDay obligatorio

planificación secuencial por prioridad/orden

se parte tareas en días sucesivos si no caben

se recalcula al cambiar orden o modificar coste/horasDia

5) Persistencia portable (JSON)
5.1 Principios

un único “workspace” (carpeta/archivo) contiene todo

no depende de servidor ni DB externa para funcionar

5.2 Contenido a persistir (mínimo)

personas, proyectos, tareas

asignaciones, prioridades

planificación (o datos suficientes para recalcular y obtener el mismo resultado)

configuraciones de herramientas por proyecto:

smtp, rest mock, sftp, db, integraciones

historiales básicos (requests recibidas, emails capturados) si se decide guardarlos

páginas Info WYSIWYG multiidioma (en JSON)

6) Criterios de aceptación (actualizados)
Global

 Al abrir, resumen global + gráfica sencilla.

 Gestión de proyectos y personas accesible desde el sidebar (botón junto al título).

 Personas sin tareas primero.

Personas

 CRUD personas con hoursPerDay obligatorio.

 Asignar tarea exige costHours.

 Detalle persona: resumen + calendario planificado.

Proyectos

 Vista proyecto: timeline por filas + línea “Hoy”.

 Excedidos en rojo.

 Asignación de personas y tareas desde proyecto.

Tools

 SMTP Fake funcionando.

 REST/SOAP cliente + mock server.

 SFTP lectura.

 Task manager + base de sync.

 BBDD conexiones + consultas.

 Info: crear páginas HTML, editarlas en WYSIWYG, multiidioma y persistir en JSON.

Persistencia

 Export/import “por copia”: copiando el JSON a otro equipo funciona igual.