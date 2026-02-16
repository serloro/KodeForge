package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.ScheduleBlock
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.datetime.LocalDate
import kotlin.math.absoluteValue

/**
 * Calendario/timeline horizontal para persona.
 * 
 * Muestra:
 * - Días en timeline horizontal
 * - Bloques de tareas por día
 * - Línea vertical "Hoy"
 * - Scroll horizontal
 */
@Composable
fun PersonCalendar(
    scheduleBlocks: List<ScheduleBlock>,
    tasks: List<Task>,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Calendario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextPrimary
            )
            
            if (scheduleBlocks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay tareas planificadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                }
            } else {
                // Agrupar bloques por fecha
                val blocksByDate = scheduleBlocks
                    .groupBy { it.date }
                    .toSortedMap()
                
                // Timeline horizontal
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(blocksByDate.entries.toList()) { (date, blocks) ->
                        DayColumn(
                            date = date,
                            blocks = blocks,
                            tasks = tasks,
                            isToday = date == today.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: String,
    blocks: List<ScheduleBlock>,
    tasks: List<Task>,
    isToday: Boolean
) {
    Column(
        modifier = Modifier.width(140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header del día
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Fecha
            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) KodeForgeColors.Primary else KodeForgeColors.TextSecondary
            )
            
            // Indicador "Hoy"
            if (isToday) {
                Surface(
                    color = KodeForgeColors.Primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "HOY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        // Línea vertical
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(if (blocks.isEmpty()) 100.dp else (blocks.size * 80).dp)
                .background(
                    if (isToday) KodeForgeColors.Primary 
                    else KodeForgeColors.Divider
                )
                .align(Alignment.CenterHorizontally)
        )
        
        // Bloques de tareas
        blocks.forEach { block ->
            val task = tasks.find { it.id == block.taskId }
            TaskBlock(
                task = task,
                hoursPlanned = block.hoursPlanned,
                projectId = block.projectId
            )
        }
    }
}

@Composable
private fun TaskBlock(
    task: Task?,
    hoursPlanned: Double,
    projectId: String
) {
    val projectColor = getProjectColor(projectId)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(8.dp),
        color = projectColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = projectColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Título de la tarea
            Text(
                text = task?.title ?: "Tarea desconocida",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = KodeForgeColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
            
            // Horas planificadas
            Text(
                text = "${hoursPlanned.toInt()}h",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = projectColor
            )
        }
    }
}

/**
 * Formatea fecha YYYY-MM-DD a "Lun 17"
 */
private fun formatDate(date: String): String {
    return try {
        val parts = date.split("-")
        val localDate = LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        val dayOfWeek = when (localDate.dayOfWeek.name) {
            "MONDAY" -> "Lun"
            "TUESDAY" -> "Mar"
            "WEDNESDAY" -> "Mié"
            "THURSDAY" -> "Jue"
            "FRIDAY" -> "Vie"
            "SATURDAY" -> "Sáb"
            "SUNDAY" -> "Dom"
            else -> ""
        }
        "$dayOfWeek ${parts[2].toInt()}"
    } catch (e: Exception) {
        date
    }
}

/**
 * Obtiene color para un proyecto (basado en hash del ID).
 */
private fun getProjectColor(projectId: String): Color {
    val projectColors = listOf(
        Color(0xFF2196F3), // Azul
        Color(0xFF4CAF50), // Verde
        Color(0xFFFF9800), // Naranja
        Color(0xFF9C27B0), // Púrpura
        Color(0xFF00BCD4), // Cian
        Color(0xFFE91E63)  // Rosa
    )
    
    val index = projectId.hashCode() % projectColors.size
    return projectColors[index.absoluteValue]
}

