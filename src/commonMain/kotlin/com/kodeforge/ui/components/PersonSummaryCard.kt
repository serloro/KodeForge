package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Card con resumen de KPIs de la persona.
 * 
 * Muestra:
 * - Tareas activas
 * - Horas planificadas
 * - Horas realizadas
 * - Progreso (%)
 * - Fecha fin estimada
 */
@Composable
fun PersonSummaryCard(
    activeTasksCount: Int,
    plannedHours: Double,
    doneHours: Double,
    totalHours: Double,
    estimatedEndDate: String?,
    modifier: Modifier = Modifier
) {
    val progress = if (totalHours > 0) (doneHours / totalHours * 100).toInt() else 0
    
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
                text = "Resumen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextPrimary
            )
            
            // Grid de KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Tareas activas
                KpiItem(
                    label = "Tareas Activas",
                    value = activeTasksCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                // Horas planificadas
                KpiItem(
                    label = "Horas Planificadas",
                    value = "${plannedHours.toInt()}h",
                    modifier = Modifier.weight(1f)
                )
                
                // Horas realizadas
                KpiItem(
                    label = "Horas Realizadas",
                    value = "${doneHours.toInt()}h",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Progreso
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progreso",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KodeForgeColors.Primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = KodeForgeColors.Primary,
                    trackColor = KodeForgeColors.PrimaryLight
                )
            }
            
            // Fecha fin estimada
            estimatedEndDate?.let { date ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha Fin Estimada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KodeForgeColors.TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = KodeForgeColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = KodeForgeColors.TextPrimary
        )
    }
}

