package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace

/**
 * Resumen visual del proyecto con métricas clave.
 */
@Composable
fun ProjectSummary(
    workspace: Workspace,
    project: Project,
    modifier: Modifier = Modifier
) {
    val projectTasks = workspace.tasks.filter { it.projectId == project.id }
    val totalTasks = projectTasks.size
    val completedTasks = projectTasks.count { it.status == "done" }
    val inProgressTasks = projectTasks.count { it.status == "in_progress" }
    val backlogTasks = projectTasks.count { it.status == "backlog" }
    val members = project.members.size
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    project.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // Badge de estado
                Surface(
                    color = when (project.status) {
                        "active" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "paused" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        else -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = when (project.status) {
                            "active" -> "Activo"
                            "paused" -> "Pausado"
                            else -> "Inactivo"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (project.status) {
                            "active" -> Color(0xFF2E7D32)
                            "paused" -> Color(0xFFE65100)
                            else -> Color(0xFF616161)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Divider()
            
            // Métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProjectMetric(
                    label = "Tareas",
                    value = totalTasks.toString(),
                    icon = "📋",
                    color = Color(0xFF2196F3)
                )
                
                ProjectMetric(
                    label = "Completadas",
                    value = completedTasks.toString(),
                    icon = "✅",
                    color = Color(0xFF4CAF50)
                )
                
                ProjectMetric(
                    label = "En Progreso",
                    value = inProgressTasks.toString(),
                    icon = "🔄",
                    color = Color(0xFFFF9800)
                )
                
                ProjectMetric(
                    label = "Miembros",
                    value = members.toString(),
                    icon = "👥",
                    color = Color(0xFF9C27B0)
                )
            }
            
            // Barra de progreso
            if (totalTasks > 0) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progreso del Proyecto",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${((completedTasks.toFloat() / totalTasks) * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = completedTasks.toFloat() / totalTasks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

/**
 * Métrica individual del proyecto.
 */
@Composable
private fun ProjectMetric(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
    }
}

