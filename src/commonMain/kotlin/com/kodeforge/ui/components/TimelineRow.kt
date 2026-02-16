package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.datetime.LocalDate

/**
 * Fila de timeline para una persona (según p2.png).
 * 
 * Muestra:
 * - Avatar + nombre de la persona
 * - Bloques de tareas en el timeline
 * 
 * T7B: Si isOverloaded = true, resalta en rojo.
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
    val nameColor = if (isOverloaded) Color(0xFFF44336) else Color(0xFF1A1A1A)
    val borderModifier = if (isOverloaded) {
        Modifier.border(2.dp, Color(0xFFF44336), RoundedCornerShape(8.dp))
    } else {
        Modifier
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 4.dp)
            .then(borderModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Nombre (ancho fijo)
        Row(
            modifier = Modifier.width(150.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5EA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.displayName.take(1).uppercase(),
                    color = Color(0xFF616161),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Nombre (rojo si excedido)
            Text(
                text = person.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = nameColor,
                fontWeight = if (isOverloaded) FontWeight.Bold else FontWeight.Normal
            )
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

