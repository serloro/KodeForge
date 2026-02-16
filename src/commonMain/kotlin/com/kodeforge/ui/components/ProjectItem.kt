package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Item de proyecto en el sidebar.
 * 
 * Layout refinado según specs/p1.png:
 * - Altura: 40dp
 * - Padding: 12dp horizontal
 * - Selected: fondo Primary alpha 0.1 + borde izquierdo 3px
 * - Icono: 18dp
 * - Spacing: 12dp entre elementos
 */
@Composable
fun ProjectItem(
    project: Project,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        KodeForgeColors.Primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }
    
    val textColor = if (isSelected) {
        KodeForgeColors.Primary
    } else {
        KodeForgeColors.TextPrimary
    }
    
    val iconColor = if (isSelected) {
        KodeForgeColors.Primary
    } else {
        KodeForgeColors.Gray400
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .drawBehind {
                // Borde izquierdo cuando está seleccionado
                if (isSelected) {
                    drawLine(
                        color = KodeForgeColors.Primary,
                        start = Offset(0f, size.height * 0.2f),
                        end = Offset(0f, size.height * 0.8f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = KodeForgeSpacing.SM), // 12dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de proyecto
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
        
        // Nombre del proyecto
        Text(
            text = project.name,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
