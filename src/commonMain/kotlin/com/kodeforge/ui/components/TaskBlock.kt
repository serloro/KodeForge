package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bloque visual de una tarea en el timeline (según p2.png).
 * 
 * Representa una tarea asignada a una persona en un rango de fechas.
 * 
 * @param taskTitle Título de la tarea
 * @param status Estado de la tarea (todo, in_progress, completed)
 * @param widthDp Ancho del bloque en dp (proporcional a duración)
 * @param onClick Callback al hacer click
 */
@Composable
fun TaskBlock(
    taskTitle: String,
    status: String,
    widthDp: Float,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        "completed" -> Color(0xFF4CAF50) // Verde
        "in_progress" -> Color(0xFFFF9800) // Naranja
        "todo" -> Color(0xFF90CAF9) // Azul claro
        else -> Color(0xFFE0E0E0) // Gris
    }
    
    Box(
        modifier = modifier
            .width(widthDp.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = taskTitle,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

