package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Dashboard principal del Home con diseño moderno y profesional.
 */
@Composable
fun HomeDashboard(
    workspace: Workspace,
    onProjectClick: (Project) -> Unit,
    onManageProjects: () -> Unit,
    onManagePeople: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalProjects = workspace.projects.size
    val totalPeople = workspace.people.size
    val activePeople = workspace.people.count { it.active }
    val totalTasks = workspace.tasks.size
    val completedTasks = workspace.tasks.count { it.status == "done" }
    val inProgressTasks = workspace.tasks.count { 
        it.status !in listOf("done", "backlog") 
    }
    val completionRate = if (totalTasks > 0) {
        (completedTasks.toFloat() / totalTasks * 100).toInt()
    } else 0
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header con bienvenida
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bienvenido a KodeForge",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = KodeForgeColors.TextPrimary
                )
                Text(
                    text = "Tu centro de gestión de proyectos y equipos",
                    style = MaterialTheme.typography.titleMedium,
                    color = KodeForgeColors.TextSecondary
                )
            }
        }
        
        // Métricas principales en cards grandes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Card de Proyectos
                ModernMetricCard(
                    title = "Proyectos Activos",
                    value = totalProjects.toString(),
                    subtitle = "En gestión",
                    icon = Icons.Default.Star,
                    gradientColors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onManageProjects
                )
                
                // Card de Equipo
                ModernMetricCard(
                    title = "Miembros del Equipo",
                    value = activePeople.toString(),
                    subtitle = "de $totalPeople totales",
                    icon = Icons.Default.Person,
                    gradientColors = listOf(
                        Color(0xFF11998e),
                        Color(0xFF38ef7d)
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onManagePeople
                )
                
                // Card de Tareas
                ModernMetricCard(
                    title = "Tareas Completadas",
                    value = "$completionRate%",
                    subtitle = "$completedTasks de $totalTasks tareas",
                    icon = Icons.Default.CheckCircle,
                    gradientColors = listOf(
                        Color(0xFFf093fb),
                        Color(0xFFf5576c)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Estadísticas de tareas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado de Tareas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Indicador de progreso circular
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = completionRate / 100f,
                                modifier = Modifier.size(60.dp),
                                strokeWidth = 6.dp,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "$completionRate%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    // Barras de progreso por estado
                    TaskStatusBar(
                        label = "En Progreso",
                        count = inProgressTasks,
                        total = totalTasks,
                        color = Color(0xFF2196F3)
                    )
                    
                    TaskStatusBar(
                        label = "Completadas",
                        count = completedTasks,
                        total = totalTasks,
                        color = Color(0xFF4CAF50)
                    )
                    
                    TaskStatusBar(
                        label = "Pendientes",
                        count = totalTasks - completedTasks - inProgressTasks,
                        total = totalTasks,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
        
        // Proyectos recientes
        if (workspace.projects.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Proyectos Recientes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(onClick = onManageProjects) {
                            Text("Ver todos")
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(workspace.projects.take(5)) { project ->
                            ProjectCard(
                                project = project,
                                workspace = workspace,
                                onClick = { onProjectClick(project) }
                            )
                        }
                    }
                }
            }
        }
        
        // Acciones rápidas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        title = "Nuevo Proyecto",
                        description = "Crear un nuevo proyecto",
                        icon = Icons.Default.Add,
                        color = Color(0xFF2196F3),
                        onClick = onManageProjects,
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickActionCard(
                        title = "Gestionar Equipo",
                        description = "Administrar personas",
                        icon = Icons.Default.Person,
                        color = Color(0xFF4CAF50),
                        onClick = onManagePeople,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = value,
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TaskStatusBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count de $total",
                style = MaterialTheme.typography.bodyMedium,
                color = KodeForgeColors.TextSecondary
            )
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    workspace: Workspace,
    onClick: () -> Unit
) {
    val projectTasks = workspace.tasks.filter { it.projectId == project.id }
    val completedTasks = projectTasks.count { it.status == "done" }
    val totalTasks = projectTasks.size
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (project.description != null) {
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = KodeForgeColors.TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
            
            Divider()
            
            // Estadísticas del proyecto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tareas",
                        fontSize = 11.sp,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "$completedTasks/$totalTasks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Equipo",
                        fontSize = 11.sp,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "${project.members.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Barra de progreso
            if (totalTasks > 0) {
                LinearProgressIndicator(
                    progress = completedTasks.toFloat() / totalTasks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = KodeForgeColors.TextSecondary
                )
            }
        }
    }
}
