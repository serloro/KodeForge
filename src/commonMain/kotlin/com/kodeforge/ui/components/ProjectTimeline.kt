package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Task
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.datetime.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * Timeline del proyecto mejorado.
 * 
 * Muestra:
 * - Columnas: Días (desde hoy hasta la última tarea asignada)
 * - Filas: Personas del proyecto
 * - Bloques de tareas con duración y progreso
 * - Scroll horizontal automático
 */
@Composable
fun ProjectTimeline(
    workspace: Workspace,
    project: Project,
    modifier: Modifier = Modifier
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    
    // Filtrar personas miembro del proyecto
    val projectMembers = remember(workspace.people, project.members) {
        workspace.people.filter { it.id in project.members }
    }
    
    // Obtener todas las tareas del proyecto
    val projectTasks = remember(workspace.tasks, project.id) {
        workspace.tasks.filter { it.projectId == project.id && it.status != "done" }
    }
    
    // Calcular la fecha máxima necesaria basándose en las tareas asignadas
    val maxEndDate = remember(projectTasks, projectMembers) {
        var maxDate = today
        
        projectMembers.forEach { person ->
            val personTasks = projectTasks.filter { it.assigneeId == person.id }
            var currentDate = today
            
            personTasks.sortedBy { it.priority }.forEach { task ->
                // Calcular días necesarios para esta tarea
                val hoursPerDay = if (person.hoursPerWeekday != null) {
                    person.hoursPerWeekday.values.average()
                } else {
                    person.hoursPerDay
                }
                
                val daysNeeded = if (hoursPerDay > 0) {
                    ceil(task.costHours / hoursPerDay).toInt()
                } else {
                    1
                }
                
                currentDate = currentDate.plus(daysNeeded, DateTimeUnit.DAY)
            }
            
            if (currentDate > maxDate) {
                maxDate = currentDate
            }
        }
        
        // Agregar un margen de 7 días
        maxDate.plus(7, DateTimeUnit.DAY)
    }
    
    val startDate = today
    val endDate = maxEndDate
    val totalDays = startDate.daysUntil(endDate)
    
    // Ancho por día
    val dayWidth = 60.dp
    val personColumnWidth = 200.dp
    
    // Estado de scroll
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Timeline del Proyecto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Planificación de tareas por persona",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Leyenda
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Completado")
                LegendItem(color = Color(0xFF2196F3), label = "En progreso")
                LegendItem(color = Color(0xFF9E9E9E), label = "Pendiente")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Contenedor con scroll horizontal
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // Header con días
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Columna de personas (fija)
                    Box(
                        modifier = Modifier
                            .width(personColumnWidth)
                            .height(60.dp)
                            .background(Color(0xFFF5F7FA))
                            .border(1.dp, Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Persona",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Columnas de días (con scroll)
                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                            .background(Color(0xFFF5F7FA))
                    ) {
                        repeat(totalDays) { dayIndex ->
                            val date = startDate.plus(dayIndex, DateTimeUnit.DAY)
                            val isToday = date == today
                            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || 
                                          date.dayOfWeek == DayOfWeek.SUNDAY
                            
                            Box(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .height(60.dp)
                                    .background(
                                        if (isToday) Color(0xFFE3F2FD)
                                        else if (isWeekend) Color(0xFFFAFAFA)
                                        else Color(0xFFF5F7FA)
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else 1.dp,
                                        color = if (isToday) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = date.dayOfWeek.name.take(3),
                                        fontSize = 10.sp,
                                        color = if (isToday) Color(0xFF2196F3) 
                                               else if (isWeekend) Color(0xFF999999)
                                               else Color(0xFF666666),
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${date.dayOfMonth}",
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) Color(0xFF2196F3) else Color(0xFF333333)
                                    )
                                    Text(
                                        text = date.month.name.take(3),
                                        fontSize = 9.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)
                
                // Filas de personas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    items(projectMembers) { person ->
                        PersonTimelineRow(
                            person = person,
                            tasks = projectTasks.filter { it.assigneeId == person.id },
                            startDate = startDate,
                            totalDays = totalDays,
                            dayWidth = dayWidth,
                            personColumnWidth = personColumnWidth,
                            scrollState = scrollState
                        )
                        Divider(color = Color(0xFFE0E0E0))
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun PersonTimelineRow(
    person: Person,
    tasks: List<Task>,
    startDate: LocalDate,
    totalDays: Int,
    dayWidth: androidx.compose.ui.unit.Dp,
    personColumnWidth: androidx.compose.ui.unit.Dp,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Columna de persona (fija)
        Box(
            modifier = Modifier
                .width(personColumnWidth)
                .fillMaxHeight()
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.displayName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
                
                Column {
                    Text(
                        text = person.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (person.hoursPerWeekday != null) {
                            val totalWeekHours = person.hoursPerWeekday.values.sum()
                            "${totalWeekHours.toInt()}h/sem"
                        } else {
                            "${person.hoursPerDay}h/día"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
        
        // Timeline de tareas (con scroll)
        Box(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxHeight()
        ) {
            Row {
                // Calcular y renderizar bloques de tareas
                var currentDate = startDate
                val sortedTasks = tasks.sortedBy { it.priority }
                
                sortedTasks.forEach { task ->
                    // Calcular días desde el inicio hasta esta tarea
                    val daysFromStart = startDate.daysUntil(currentDate)
                    
                    // Calcular duración de la tarea en días
                    val hoursPerDay = if (person.hoursPerWeekday != null) {
                        person.hoursPerWeekday.values.average()
                    } else {
                        person.hoursPerDay
                    }
                    
                    val taskDurationDays = if (hoursPerDay > 0) {
                        ceil(task.costHours / hoursPerDay).toInt()
                    } else {
                        1
                    }
                    
                    // Renderizar el bloque de tarea
                    if (daysFromStart >= 0 && daysFromStart < totalDays) {
                        Box(
                            modifier = Modifier
                                .offset(x = dayWidth * daysFromStart)
                                .width(dayWidth * taskDurationDays)
                                .fillMaxHeight()
                                .padding(4.dp)
                        ) {
                            TaskBlock(
                                task = task,
                                durationDays = taskDurationDays
                            )
                        }
                    }
                    
                    // Avanzar la fecha actual
                    currentDate = currentDate.plus(taskDurationDays, DateTimeUnit.DAY)
                }
                
                // Espacio para cubrir todos los días
                Spacer(Modifier.width(dayWidth * totalDays))
            }
        }
    }
}

@Composable
private fun TaskBlock(
    task: Task,
    durationDays: Int
) {
    val progress = if (task.costHours > 0) {
        (task.doneHours / task.costHours).coerceIn(0.0, 1.0)
    } else {
        0.0
    }
    
    val backgroundColor = when (task.status) {
        "done" -> Color(0xFF4CAF50)
        "in_progress", "in_review", "testing" -> Color(0xFF2196F3)
        else -> Color(0xFF9E9E9E)
    }
    
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Barra de progreso
            if (progress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.toFloat())
                        .background(backgroundColor.copy(alpha = 0.5f))
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = backgroundColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
                
                Spacer(Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${durationDays}d",
                        fontSize = 9.sp,
                        color = backgroundColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (progress > 0) {
                        Text(
                            text = "•",
                            fontSize = 9.sp,
                            color = backgroundColor
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 9.sp,
                            color = backgroundColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
