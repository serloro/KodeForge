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

/**
 * Header principal de KodeForge.
 * 
 * Layout refinado según specs/p1.png:
 * - Izquierda: Icono azul + "KodeForge"
 * - Derecha: Botón "+ Nuevo Proyecto"
 * 
 * Ajustes de spacing:
 * - Altura: 72dp (más generosa)
 * - Padding horizontal: 32dp (más espacioso)
 * - Icono: 36dp (más compacto)
 */
@Composable
fun Header(
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KodeForgeColors.Surface,
        shadowElevation = 1.dp // Sombra más sutil
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // Aumentado de 64dp para más espacio
                .padding(horizontal = 32.dp, vertical = 16.dp), // Padding más generoso
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo + Nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp) // Reducido ligeramente
            ) {
                // Icono azul en cuadrado redondeado (más pequeño según p1.png)
                Box(
                    modifier = Modifier
                        .size(36.dp) // Reducido de 40dp
                        .background(
                            color = KodeForgeColors.Primary,
                            shape = RoundedCornerShape(6.dp) // Border radius proporcional
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = Color.White,
                        fontSize = 18.sp, // Proporcional al tamaño
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // Nombre de la app
                Text(
                    text = "KodeForge",
                    fontSize = 20.sp, // Tamaño explícito para mayor control
                    color = KodeForgeColors.Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp // Más compacto como en p1.png
                )
            }
            
            // Botón "Nuevo Proyecto"
            Button(
                onClick = onNewProject,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp, // Sombra más sutil
                    pressedElevation = 3.dp
                ),
                shape = RoundedCornerShape(6.dp), // Border radius más redondeado
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp) // Icono más pequeño
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Nuevo Proyecto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
