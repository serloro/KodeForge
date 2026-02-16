package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.PlanningUseCases
import kotlinx.datetime.*

/**
 * Timeline del proyecto (según p2.png).
 * 
 * Muestra:
 * - Header con fechas y columnas
 * - Línea vertical "Hoy" muy visible
 * - Filas de personas con bloques de tareas
 * 
 * @param workspace Workspace completo
 * @param project Proyecto actual
 */
@Composable
fun ProjectTimeline(
    workspace: Workspace,
    project: Project,
    modifier: Modifier = Modifier
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    
    // Calcular rango de fechas (hoy ± 14 días)
    val startDate = remember { today.minus(14, DateTimeUnit.DAY) }
    val endDate = remember { today.plus(30, DateTimeUnit.DAY) }
    val totalDays = remember { startDate.daysUntil(endDate) }
    
    // Píxeles por día (ajustable)
    val pixelsPerDay = 40f
    val timelineWidth = remember { totalDays * pixelsPerDay }
    
    // Filtrar personas miembro del proyecto
    val projectMembers = remember(workspace.people, project.members) {
        workspace.people.filter { it.id in project.members }
    }
    
    // Detectar sobrecargas (T7B)
    val planningUseCases = remember { PlanningUseCases() }
    val overloads = remember(workspace.planning.scheduleBlocks, startDate, endDate) {
        planningUseCases.detectOverloads(
            workspace = workspace,
            projectId = project.id,
            startDate = startDate,
            endDate = endDate
        )
    }
    
    // Calcular posición de "Hoy"
    val todayPosition = remember {
        val daysFromStart = startDate.daysUntil(today)
        150f + (daysFromStart * pixelsPerDay) // 150dp = ancho de columna de nombres
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Timeline del Proyecto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Timeline container con línea "Hoy"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .drawBehind {
                    // Línea vertical "Hoy"
                    drawLine(
                        color = Color(0xFF2196F3),
                        start = Offset(todayPosition, 0f),
                        end = Offset(todayPosition, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                }
        ) {
            Column {
                // Header con fechas
                TimelineHeader(
                    startDate = startDate,
                    endDate = endDate,
                    today = today,
                    pixelsPerDay = pixelsPerDay
                )
                
                Divider(color = Color(0xFFE0E0E0))
                
                // Filas de personas
                LazyColumn {
                    items(projectMembers) { person ->
                        val personTasks = workspace.tasks.filter {
                            it.assigneeId == person.id &&
                                    it.projectId == project.id &&
                                    it.status != "completed"
                        }
                        
                        TimelineRow(
                            person = person,
                            tasks = personTasks,
                            startDate = startDate,
                            endDate = endDate,
                            pixelsPerDay = pixelsPerDay,
                            isOverloaded = person.id in overloads
                        )
                        
                        Divider(color = Color(0xFFF0F0F0))
                    }
                }
            }
            
            // Label "Hoy" flotante
            Box(
                modifier = Modifier
                    .offset(x = (todayPosition - 20).dp, y = 8.dp)
                    .background(Color(0xFF2196F3))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hoy",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${today.dayOfMonth} ${today.month.name.take(3)}",
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

/**
 * Header del timeline con columnas de fechas.
 */
@Composable
private fun TimelineHeader(
    startDate: LocalDate,
    endDate: LocalDate,
    today: LocalDate,
    pixelsPerDay: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFFF5F7FA)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Espacio para nombres de personas
        Box(modifier = Modifier.width(150.dp))
        
        // Columnas de fechas (cada 7 días)
        var currentDate = startDate
        while (currentDate <= endDate) {
            Box(
                modifier = Modifier.width((pixelsPerDay * 7).dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${currentDate.dayOfMonth}",
                        fontSize = 12.sp,
                        fontWeight = if (currentDate == today) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentDate == today) Color(0xFF2196F3) else Color(0xFF666666)
                    )
                    Text(
                        text = currentDate.month.name.take(3),
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
            currentDate = currentDate.plus(7, DateTimeUnit.DAY)
        }
    }
}

