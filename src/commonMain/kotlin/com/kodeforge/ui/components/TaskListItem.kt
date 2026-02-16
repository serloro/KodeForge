package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Item de tarea en la lista de gestión.
 * 
 * Muestra:
 * - [Prioridad] Título
 * - Descripción (si hay)
 * - Badge status + costo horas
 * - Assignee (si hay)
 * - Botones: Asignar/Reasignar | Editar | Eliminar
 */
@Composable
fun TaskListItem(
    task: Task,
    assignee: Person?,
    onAssign: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Info de la tarea
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fila 1: [Prioridad] Título
                Row(
                    verticalAlignment = Alignment.CenterVertically,
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
                
                // Fila 2: Descripción (si hay)
                task.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = KodeForgeColors.TextSecondary,
                        maxLines = 2
                    )
                }
                
                // Fila 3: Status + Costo + Assignee
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    
                    // Costo horas badge
                    Surface(
                        color = KodeForgeColors.Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${task.costHours}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = KodeForgeColors.Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    
                    // Assignee (si hay)
                    if (assignee != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Avatar pequeño
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E5EA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = assignee.displayName.take(1).uppercase(),
                                    color = Color(0xFF5A5A5F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = assignee.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = KodeForgeColors.TextSecondary
                            )
                        }
                    } else {
                        Text(
                            text = "Sin asignar",
                            style = MaterialTheme.typography.bodySmall,
                            color = KodeForgeColors.TextTertiary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            // Derecha: Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Asignar/Reasignar
                IconButton(
                    onClick = onAssign,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (assignee == null) Icons.Default.Add else Icons.Default.Person,
                        contentDescription = if (assignee == null) "Asignar" else "Reasignar",
                        tint = KodeForgeColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Botón Editar
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar ${task.title}",
                        tint = KodeForgeColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Botón Eliminar
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ${task.title}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

