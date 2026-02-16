package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Item de proyecto en el sidebar.
 * 
 * Layout refinado según specs/p1.png:
 * - Padding vertical más generoso: 11dp
 * - Icono: 20dp (ligeramente mayor)
 * - Font-size: 14.5sp
 * - Spacing más claro: 10dp entre icono y texto
 */
@Composable
fun ProjectItem(
    project: Project,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp)) // Border radius más suave
            .background(
                color = if (isSelected) KodeForgeColors.SidebarItemSelected 
                       else Color.Transparent
            )
            .drawBehind {
                // Borde izquierdo grueso solo cuando está seleccionado
                if (isSelected) {
                    drawLine(
                        color = KodeForgeColors.Primary,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx() // Reducido a 3dp para más sutileza
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp), // Padding vertical mayor
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Spacing mayor
    ) {
        // Icono circular con inicial (ajustado según p1.png)
        Box(
            modifier = Modifier
                .size(20.dp) // Aumentado de 18dp
                .clip(CircleShape)
                .background(KodeForgeColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = project.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 10.sp, // Proporcional
                fontWeight = FontWeight.Bold
            )
        }
        
        // Nombre del proyecto
        Text(
            text = project.name,
            fontSize = 14.5.sp, // Tamaño explícito para mayor control
            color = KodeForgeColors.TextPrimary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
