package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Bloque visual de una tarea en el timeline (según p2.png).
 * 
 * Layout refinado según specs/p2.png:
 * - Bordes redondeados: 4px
 * - Colores vibrantes:
 *   - Normal: Success (#10B981) verde
 *   - Excedido: Error (#EF4444) rojo
 * - Altura: 32dp
 * - Padding: 4dp
 * - Texto: 11sp medium, blanco
 * 
 * @param taskTitle Título de la tarea
 * @param isOverloaded Si la tarea está en un bloque excedido
 * @param widthDp Ancho del bloque en dp (proporcional a duración)
 * @param onClick Callback al hacer click
 */
@Composable
fun TaskBlock(
    taskTitle: String,
    isOverloaded: Boolean = false,
    widthDp: Float,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isOverloaded) {
        KodeForgeColors.Error // Rojo vibrante según specs
    } else {
        KodeForgeColors.Success // Verde vibrante según specs
    }
    
    Box(
        modifier = modifier
            .width(widthDp.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp)) // 4px según specs
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(KodeForgeSpacing.XXS), // 4dp
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = taskTitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
