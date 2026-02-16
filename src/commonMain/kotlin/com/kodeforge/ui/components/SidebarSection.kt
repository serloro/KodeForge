package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Sección reutilizable del sidebar (Projects o Personas).
 * 
 * Layout refinado según specs/p1.png:
 * - Padding horizontal más generoso: 16dp
 * - Título con font-size ligeramente mayor
 * - Espaciado más claro entre header y lista
 */
@Composable
fun <T> SidebarSection(
    title: String,
    onManage: () -> Unit,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Fila: Título + Botón Gestionar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp), // Padding más generoso
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título de la sección (font-size mayor según p1.png)
            Text(
                text = title,
                fontSize = 15.sp, // Aumentado de 14sp implícito
                color = KodeForgeColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
            
            // Botón "Gestionar" discreto
            TextButton(
                onClick = onManage,
                modifier = Modifier.height(28.dp), // Ligeramente más alto
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF8E8E93) // Gris más claro como en p1.png
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Gestionar $title",
                    modifier = Modifier.size(13.dp), // Ligeramente mayor
                    tint = Color(0xFF8E8E93)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Gestionar",
                    fontSize = 12.sp, // Ligeramente mayor
                    fontWeight = FontWeight.Normal
                )
            }
        }
        
        Spacer(Modifier.height(8.dp)) // Espaciado entre header y lista
        
        // Lista de items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp), // Padding más generoso
            verticalArrangement = Arrangement.spacedBy(4.dp) // Espaciado mayor entre items
        ) {
            items.forEach { item ->
                itemContent(item)
            }
        }
    }
}
