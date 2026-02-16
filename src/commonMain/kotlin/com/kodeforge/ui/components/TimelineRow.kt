package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing
import kotlinx.datetime.LocalDate

/**
 * Fila de timeline para una persona (según p2.png).
 * 
 * Layout refinado según specs/p2.png:
 * - Altura: 40dp
 * - Avatar: 32dp con inicial
 * - Nombre: 14sp
 * - Si isOverloaded: texto rojo + icono warning
 * - Spacing: 12dp entre elementos
 * 
 * @param person Persona
 * @param tasks Tareas asignadas a esta persona
 * @param startDate Fecha de inicio del timeline
 * @param endDate Fecha de fin del timeline
 * @param pixelsPerDay Píxeles por día en el timeline
 * @param isOverloaded Si la persona está excedida en algún día del rango
 */
@Composable
fun TimelineRow(
    person: Person,
    tasks: List<Task>,
    startDate: LocalDate,
    endDate: LocalDate,
    pixelsPerDay: Float,
    isOverloaded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = if (isOverloaded) {
        KodeForgeColors.Error
    } else {
        KodeForgeColors.TextPrimary
    }
    
    val avatarBackground = if (isOverloaded) {
        KodeForgeColors.Error.copy(alpha = 0.1f)
    } else {
        KodeForgeColors.Gray200
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(KodeForgeSpacing.TimelineRowHeight), // 40dp según specs
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Nombre (ancho fijo)
        Row(
            modifier = Modifier
                .width(180.dp)
                .padding(end = KodeForgeSpacing.MD), // 16dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(avatarBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.displayName.take(1).uppercase(),
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
            
            // Nombre (rojo si excedido)
            Text(
                text = person.displayName,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = if (isOverloaded) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Indicador de excedido
            if (isOverloaded) {
                Spacer(Modifier.width(KodeForgeSpacing.XXS)) // 4dp
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Excedido",
                    tint = KodeForgeColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Timeline de tareas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // TODO: Renderizar bloques de tareas basándose en scheduleBlocks
            // Por ahora, placeholder
            tasks.forEach { task ->
                // Calcular posición y ancho basándose en fechas
                // TaskBlock(...)
            }
        }
    }
}
