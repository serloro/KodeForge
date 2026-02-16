package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Tile para una utilidad del proyecto (según p2.png).
 * 
 * Layout refinado según specs/p2.png:
 * - Altura: 80dp
 * - Padding: 16dp
 * - Icono: 20dp en círculo de 40dp
 * - Título: 12sp
 * - Bordes redondeados: 12px
 * - Sombra: 1dp
 * 
 * @param icon Icono de la utilidad
 * @param title Título de la utilidad
 * @param iconTint Color del icono
 * @param iconBackground Color de fondo del círculo del icono
 * @param onClick Callback al hacer click
 */
@Composable
fun UtilityTile(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(KodeForgeSpacing.UtilityTileHeight) // 80dp según specs
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = KodeForgeColors.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KodeForgeSpacing.MD), // 16dp
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono en círculo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.height(KodeForgeSpacing.XS)) // 8dp
            
            // Título
            Text(
                text = title,
                fontSize = 12.sp,
                color = KodeForgeColors.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
