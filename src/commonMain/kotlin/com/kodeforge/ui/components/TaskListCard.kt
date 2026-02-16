package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Card con lista de tareas activas de la persona.
 * 
 * Muestra:
 * - Lista de tareas (solo lectura)
 * - Status + horas
 * - Sin botones de acción
 */
@Composable
fun TaskListCard(
    tasks: List<Task>,
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
                text = "Tareas Activas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextPrimary
            )
            
            if (tasks.isEmpty()) {
                // Empty state
                Text(
                    text = "No hay tareas activas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KodeForgeColors.TextSecondary
                )
            } else {
                // Lista de tareas
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tasks.sortedBy { it.priority }.forEach { task ->
                        TaskItemReadOnly(task = task)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemReadOnly(
    task: Task
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = KodeForgeColors.SurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila 1: [Prioridad] Título
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge prioridad
                if (task.priority > 0) {
                    Surface(
                        color = KodeForgeColors.TextTertiary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "[${task.priority}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = KodeForgeColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Título
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = KodeForgeColors.TextPrimary
                )
            }
            
            // Fila 2: Status + Horas
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status badge
                val (statusIcon, statusLabel, statusColor) = when (task.status) {
                    "todo" -> Triple("⚪", "Por Hacer", KodeForgeColors.TextSecondary)
                    "in_progress" -> Triple("🟡", "En Progreso", KodeForgeColors.PersonActive)
                    "completed" -> Triple("✅", "Completado", KodeForgeColors.PersonIdle)
                    else -> Triple("⚪", task.status, KodeForgeColors.TextSecondary)
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$statusIcon $statusLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                
                // Horas
                Text(
                    text = "${task.costHours.toInt()}h (${task.doneHours.toInt()}h hechas)",
                    style = MaterialTheme.typography.bodySmall,
                    color = KodeForgeColors.TextSecondary
                )
            }
        }
    }
}

