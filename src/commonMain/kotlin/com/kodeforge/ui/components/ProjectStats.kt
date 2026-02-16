package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace

/**
 * Estadísticas del proyecto (según p2.png).
 * 
 * Muestra:
 * - Total de tareas
 * - Tareas completadas
 * - Porcentaje de completitud
 * - Métricas clave (opcional)
 */
@Composable
fun ProjectStats(
    workspace: Workspace,
    project: Project,
    modifier: Modifier = Modifier
) {
    val projectTasks = remember(workspace.tasks, project.id) {
        workspace.tasks.filter { it.projectId == project.id }
    }
    
    val totalTasks = projectTasks.size
    val completedTasks = projectTasks.count { it.status == "completed" }
    val completionPercentage = if (totalTasks > 0) {
        (completedTasks * 100) / totalTasks
    } else {
        0
    }
    
    val totalHours = projectTasks.sumOf { it.costHours }
    val completedHours = projectTasks.filter { it.status == "completed" }.sumOf { it.costHours }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Total tas'a los",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Estadísticas principales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Total tareas
            StatCard(
                value = totalTasks.toString(),
                label = "tareas",
                sublabel = "Vestibolados mires"
            )
            
            // Completadas
            StatCard(
                value = completedTasks.toString(),
                label = "completadas",
                sublabel = "7 incumplidas"
            )
            
            // Porcentaje
            StatCard(
                value = "$completionPercentage%",
                label = "de abierta incumplides",
                sublabel = null
            )
        }
        
        // Métricas clave
        Text(
            text = "MÉTRICAS CLAVE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            MetricCard(
                title = "GUDD",
                value = "${totalHours.toInt()}h",
                subtitle = "Gasto en Fundaciones"
            )
            
            MetricCard(
                title = "SSS Prioridades",
                value = "${completedHours.toInt()}h",
                subtitle = "Overdue de todas"
            )
            
            MetricCard(
                title = "RETRITO AUTO-REGURO",
                value = "$completionPercentage%",
                subtitle = "Ajo pilatar las listres"
            )
        }
    }
}

/**
 * Card para una estadística principal.
 */
@Composable
private fun StatCard(
    value: String,
    label: String,
    sublabel: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        sublabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

/**
 * Card para una métrica clave.
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = Color(0xFF999999)
        )
    }
}

