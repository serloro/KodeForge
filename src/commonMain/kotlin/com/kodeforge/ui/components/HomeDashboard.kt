package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Dashboard principal del Home con resumen visual.
 */
@Composable
fun HomeDashboard(
    workspace: Workspace,
    modifier: Modifier = Modifier
) {
    val totalProjects = workspace.projects.size
    val totalPeople = workspace.people.size
    val totalTasks = workspace.tasks.size
    val completedTasks = workspace.tasks.count { it.status == "done" }
    val inProgressTasks = workspace.tasks.count { it.status == "in_progress" }
    val backlogTasks = workspace.tasks.count { it.status == "backlog" }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Título
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextPrimary
            )
        }
        
        item {
            // Cards de métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Proyectos",
                    value = totalProjects.toString(),
                    icon = "📁",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Personas",
                    value = totalPeople.toString(),
                    icon = "👥",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Tareas Totales",
                    value = totalTasks.toString(),
                    icon = "✅",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            // Resumen de tareas
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Estado de Tareas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (totalTasks > 0) {
                        TaskStatusBar(
                            completed = completedTasks,
                            inProgress = inProgressTasks,
                            backlog = backlogTasks,
                            total = totalTasks
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TaskStatusLegend(
                                label = "Backlog",
                                count = backlogTasks,
                                color = Color(0xFF9E9E9E)
                            )
                            TaskStatusLegend(
                                label = "En Progreso",
                                count = inProgressTasks,
                                color = Color(0xFF2196F3)
                            )
                            TaskStatusLegend(
                                label = "Completadas",
                                count = completedTasks,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    } else {
                        Text(
                            text = "No hay tareas creadas aún",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
        
        item {
            // Proyectos recientes
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Proyectos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (workspace.projects.isEmpty()) {
                        Text(
                            text = "No hay proyectos creados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    } else {
                        workspace.projects.take(5).forEach { project ->
                            val projectTasks = workspace.tasks.filter { it.projectId == project.id }
                            ProjectSummaryRow(
                                projectName = project.name,
                                totalTasks = projectTasks.size,
                                completedTasks = projectTasks.count { it.status == "done" },
                                members = project.members.size
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de métrica individual.
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                }
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Barra de progreso de tareas.
 */
@Composable
private fun TaskStatusBar(
    completed: Int,
    inProgress: Int,
    backlog: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val completedPercent = (completed.toFloat() / total) * 100
    val inProgressPercent = (inProgress.toFloat() / total) * 100
    val backlogPercent = (backlog.toFloat() / total) * 100
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
        ) {
            if (backlog > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(backlogPercent)
                        .background(Color(0xFF9E9E9E), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            }
            if (inProgress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(inProgressPercent)
                        .background(Color(0xFF2196F3))
                )
            }
            if (completed > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(completedPercent)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                )
            }
        }
    }
}

/**
 * Leyenda de estado de tareas.
 */
@Composable
private fun TaskStatusLegend(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
    }
}

/**
 * Fila de resumen de proyecto.
 */
@Composable
private fun ProjectSummaryRow(
    projectName: String,
    totalTasks: Int,
    completedTasks: Int,
    members: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = projectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "$completedTasks/$totalTasks tareas • $members miembros",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
        
        if (totalTasks > 0) {
            val progress = (completedTasks.toFloat() / totalTasks) * 100
            Text(
                text = "${progress.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (progress >= 100) Color(0xFF4CAF50) else Color(0xFF2196F3)
            )
        }
    }
}

