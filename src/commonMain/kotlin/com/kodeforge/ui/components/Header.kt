package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Header principal de KodeForge.
 * 
 * Layout refinado según specs/p1.png:
 * - Altura: 64dp
 * - Padding horizontal: 24dp
 * - Sombra: 1dp (sutil)
 * - Icono: 32dp con "K"
 * - Botón: Primary con bordes redondeados
 */
@Composable
fun Header(
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KodeForgeColors.Surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(KodeForgeSpacing.HeaderHeight) // 64dp según specs
                .padding(horizontal = KodeForgeSpacing.LG), // 24dp según specs
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo + Nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KodeForgeSpacing.SM) // 12dp
            ) {
                // Icono azul en cuadrado redondeado
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = KodeForgeColors.Primary,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // Nombre de la app
                Text(
                    text = "KodeForge",
                    fontSize = 20.sp,
                    color = KodeForgeColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Botón "Nuevo Proyecto"
            Button(
                onClick = onNewProject,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = KodeForgeSpacing.MD, vertical = KodeForgeSpacing.XS)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(KodeForgeSpacing.XS)) // 8dp
                Text(
                    text = "Nuevo Proyecto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
