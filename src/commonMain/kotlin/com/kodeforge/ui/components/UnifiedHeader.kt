package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
 * Header unificado de KodeForge con breadcrumb dinámico.
 * 
 * Características:
 * - Logo y nombre de la app siempre visible
 * - Breadcrumb dinámico según el contexto
 * - Sin botón "Nuevo Proyecto" cuando estás dentro de un proyecto
 * - Botón de volver cuando no estás en Home
 * 
 * @param breadcrumbs Lista de migas de pan (ej: ["KodeForge", "Proyecto X", "REST API"])
 * @param onBack Callback para volver (null si estás en Home)
 */
@Composable
fun UnifiedHeader(
    breadcrumbs: List<String> = listOf("KodeForge"),
    onBack: (() -> Unit)? = null,
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
            // Lado izquierdo: Botón volver (si aplica) + Logo + Breadcrumb
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KodeForgeSpacing.SM) // 12dp
            ) {
                // Botón de volver (solo si no estamos en Home)
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = KodeForgeColors.TextPrimary
                        )
                    }
                }
                
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
                
                // Breadcrumb dinámico
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    breadcrumbs.forEachIndexed { index, crumb ->
                        // Nombre del nivel
                        Text(
                            text = crumb,
                            fontSize = if (index == breadcrumbs.lastIndex) 20.sp else 16.sp,
                            color = if (index == breadcrumbs.lastIndex) 
                                KodeForgeColors.Primary 
                            else 
                                Color(0xFF666666),
                            fontWeight = if (index == breadcrumbs.lastIndex) 
                                FontWeight.Bold 
                            else 
                                FontWeight.Normal
                        )
                        
                        // Separador (excepto en el último)
                        if (index < breadcrumbs.lastIndex) {
                            Text(
                                text = ">",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

