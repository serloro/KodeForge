package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.InfoPage

/**
 * Item de lista de página Info.
 * 
 * Muestra:
 * - Título de la página
 * - Botones: Renombrar, Eliminar, Arriba, Abajo
 * 
 * @param page Página Info
 * @param isSelected Si la página está seleccionada
 * @param canMoveUp Si se puede mover arriba
 * @param canMoveDown Si se puede mover abajo
 * @param onSelect Callback al seleccionar
 * @param onRename Callback al renombrar
 * @param onDelete Callback al eliminar
 * @param onMoveUp Callback al mover arriba
 * @param onMoveDown Callback al mover abajo
 */
@Composable
fun InfoPageListItem(
    page: InfoPage,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Título
            Text(
                text = page.title["es"] ?: page.title["en"] ?: page.slug,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botones de reordenar
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Mover arriba",
                            tint = if (canMoveUp) Color(0xFF666666) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mover abajo",
                            tint = if (canMoveDown) Color(0xFF666666) else Color(0xFFCCCCCC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Botones de editar/eliminar
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onRename,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Renombrar",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

