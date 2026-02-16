package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Sidebar de herramientas/utilidades para el modo proyecto.
 * 
 * Muestra las herramientas disponibles en formato vertical
 * con iconos y nombres descriptivos.
 */
@Composable
fun ToolsSidebar(
    selectedToolId: String?,
    onToolClick: (String) -> Unit,
    onBackToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(KodeForgeSpacing.SidebarWidth) // 240dp
            .fillMaxHeight(),
        color = KodeForgeColors.BackgroundSecondary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(KodeForgeSpacing.SM) // 12dp
        ) {
            // Botón "Hub del Proyecto"
            ToolItem(
                icon = Icons.Default.Home,
                label = "Hub del Proyecto",
                color = KodeForgeColors.Primary,
                isSelected = selectedToolId == null,
                onClick = onBackToHub
            )
            
            Spacer(Modifier.height(KodeForgeSpacing.MD)) // 16dp
            
            // Título de sección
            Text(
                text = "Herramientas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextSecondary,
                modifier = Modifier.padding(horizontal = KodeForgeSpacing.SM)
            )
            
            Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
            
            // Lista de herramientas
            val tools = listOf(
                ToolData("smtp", "SMTP Fake", Icons.Default.Email, Color(0xFF2196F3)),
                ToolData("rest", "REST/SOAP API", Icons.Default.Settings, Color(0xFF4CAF50)),
                ToolData("info", "Info - Documentación", Icons.Default.Info, Color(0xFFFF9800)),
                ToolData("bbdd", "Base de Datos", Icons.Default.Star, Color(0xFF9C27B0)),
                ToolData("sftp", "SFTP/SSH", Icons.Default.List, Color(0xFFF44336)),
                ToolData("tasks", "Gestión de Tareas", Icons.Default.CheckCircle, Color(0xFFFFC107))
            )
            
            tools.forEach { tool ->
                ToolItem(
                    icon = tool.icon,
                    label = tool.label,
                    color = tool.color,
                    isSelected = selectedToolId == tool.id,
                    onClick = { onToolClick(tool.id) }
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Item individual de herramienta en el sidebar.
 */
@Composable
private fun ToolItem(
    icon: ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        color.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }
    
    val textColor = if (isSelected) {
        color
    } else {
        KodeForgeColors.TextPrimary
    }
    
    val iconColor = if (isSelected) {
        color
    } else {
        KodeForgeColors.Gray400
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = KodeForgeSpacing.SM), // 12dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
        
        // Nombre de la herramienta
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/**
 * Datos de una herramienta.
 */
private data class ToolData(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

