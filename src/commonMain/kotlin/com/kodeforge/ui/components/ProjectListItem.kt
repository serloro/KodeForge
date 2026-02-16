package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Item de lista para mostrar un proyecto.
 * 
 * Muestra:
 * - Avatar con inicial del proyecto
 * - Nombre y descripción
 * - Badge de estado
 * - Contador de miembros
 * - Botones de editar y eliminar
 */
@Composable
fun ProjectListItem(
    project: Project,
    onEdit: (Project) -> Unit,
    onDelete: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = KodeForgeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(KodeForgeColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = project.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Información del proyecto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KodeForgeColors.TextPrimary
                )
                
                // Descripción
                project.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Badges: Estado + Miembros
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de estado
                    StatusBadge(status = project.status)
                    
                    // Contador de miembros
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = KodeForgeColors.TextSecondary
                        )
                        Text(
                            text = "${project.members.size} miembros",
                            style = MaterialTheme.typography.bodySmall,
                            color = KodeForgeColors.TextSecondary
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { onEdit(project) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar proyecto",
                        tint = KodeForgeColors.TextSecondary
                    )
                }
                
                IconButton(onClick = { onDelete(project) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar proyecto",
                        tint = KodeForgeColors.PersonOverload
                    )
                }
            }
        }
    }
}

/**
 * Badge para mostrar el estado del proyecto.
 */
@Composable
private fun StatusBadge(status: String) {
    val (label, color, textColor) = when (status) {
        "active" -> Triple("Activo", Color(0xFFC8E6C9), Color(0xFF4CAF50))
        "paused" -> Triple("Pausado", Color(0xFFFFECB3), Color(0xFFFF9800))
        "completed" -> Triple("Completado", Color(0xFFBBDEFB), Color(0xFF2196F3))
        else -> Triple("Desconocido", Color.LightGray, Color.DarkGray)
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

