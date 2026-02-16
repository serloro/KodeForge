package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tile para una utilidad del proyecto (según p2.png).
 * 
 * Muestra:
 * - Icono (grande, colorido)
 * - Título
 * - Subtítulo (opcional)
 * 
 * @param icon Icono de la utilidad
 * @param title Título de la utilidad
 * @param subtitle Subtítulo opcional
 * @param backgroundColor Color de fondo del tile
 * @param iconColor Color del icono
 * @param onClick Callback al hacer click
 */
@Composable
fun UtilityTile(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        
        // Título
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A)
        )
        
        // Subtítulo
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

