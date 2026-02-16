package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing
import com.kodeforge.ui.theme.KodeForgeTextStyles

/**
 * Sección reutilizable del sidebar (Projects o Personas).
 * 
 * Layout refinado según specs/p1.png:
 * - Título: 14sp medium (TextSecondary)
 * - Botón gestionar: más compacto (28dp)
 * - Spacing entre items: 8dp
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
                .padding(horizontal = KodeForgeSpacing.XS), // 8dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título de la sección (14sp medium según specs)
            Text(
                text = title,
                style = KodeForgeTextStyles.SidebarSectionTitle,
                color = KodeForgeColors.TextSecondary
            )
            
            // Botón "Gestionar" discreto
            IconButton(
                onClick = onManage,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Gestionar $title",
                    modifier = Modifier.size(16.dp),
                    tint = KodeForgeColors.Gray400
                )
            }
        }
        
        Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
        
        // Lista de items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KodeForgeSpacing.XS) // 8dp según specs
        ) {
            items.forEach { item ->
                itemContent(item)
            }
        }
    }
}
