package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Grid de tiles de utilidades del proyecto (según p2.png).
 * 
 * Muestra 5-6 utilidades horizontalmente:
 * - Tempo 1 (azul)
 * - Tempo 2 (verde)
 * - SMTP Fake (naranja)
 * - REST API (morado)
 * - Ajustes (rojo)
 * - Info (opcional)
 */
@Composable
fun UtilityTilesGrid(
    onUtilityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título de la sección
        Text(
            text = "Utilidades del Proyecto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Grid horizontal de tiles
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(utilities) { utility ->
                UtilityTile(
                    icon = utility.icon,
                    title = utility.title,
                    subtitle = utility.subtitle,
                    backgroundColor = utility.backgroundColor,
                    iconColor = utility.iconColor,
                    onClick = { onUtilityClick(utility.id) }
                )
            }
        }
    }
}

/**
 * Definición de utilidades disponibles.
 */
private data class Utility(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String?,
    val backgroundColor: Color,
    val iconColor: Color
)

private val utilities = listOf(
    Utility(
        id = "tempo1",
        icon = Icons.Default.DateRange,
        title = "Tempo",
        subtitle = "Gestión Tarea 1",
        backgroundColor = Color(0xFFE3F2FD), // Azul claro
        iconColor = Color(0xFF2196F3) // Azul
    ),
    Utility(
        id = "tempo2",
        icon = Icons.Default.DateRange,
        title = "Tempo",
        subtitle = "Hory Franquimonos",
        backgroundColor = Color(0xFFE8F5E9), // Verde claro
        iconColor = Color(0xFF4CAF50) // Verde
    ),
    Utility(
        id = "smtp",
        icon = Icons.Default.Email,
        title = "SMTP Fake",
        subtitle = "Enviar correos...",
        backgroundColor = Color(0xFFFFF3E0), // Naranja claro
        iconColor = Color(0xFFFF9800) // Naranja
    ),
    Utility(
        id = "rest",
        icon = Icons.Default.Settings,
        title = "REST API",
        subtitle = "Energizante",
        backgroundColor = Color(0xFFF3E5F5), // Morado claro
        iconColor = Color(0xFF9C27B0) // Morado
    ),
    Utility(
        id = "ajustes",
        icon = Icons.Default.Settings,
        title = "Ajustes",
        subtitle = "J&0.0.2.9876",
        backgroundColor = Color(0xFFFFEBEE), // Rojo claro
        iconColor = Color(0xFFF44336) // Rojo
    ),
    Utility(
        id = "info",
        icon = Icons.Default.Info,
        title = "Info",
        subtitle = "Documentación",
        backgroundColor = Color(0xFFFFF9C4), // Amarillo claro
        iconColor = Color(0xFFFBC02D) // Amarillo
    )
)

