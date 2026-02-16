package com.kodeforge.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Sistema de espaciado de KodeForge
 * Basado en escala de 4px para consistencia visual
 */
object KodeForgeSpacing {
    // Escala base
    val XXS = 4.dp
    val XS = 8.dp
    val SM = 12.dp
    val MD = 16.dp
    val LG = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
    
    // Componentes específicos (según specs)
    val CardPadding = MD                    // 16dp
    val CardSpacing = MD                    // 16dp entre cards
    val SectionSpacing = LG                 // 24dp entre secciones
    
    // Layout principal
    val SidebarWidth = 240.dp               // Actualizado según specs (era 200dp)
    val HeaderHeight = 64.dp                // Actualizado según specs (era 56dp)
    
    // Timeline
    val TimelineRowHeight = 40.dp           // Actualizado según specs (era 36dp)
    val TimelineRowSpacing = SM             // 12dp entre filas
    val TimelineBlockSpacing = XXS          // 4dp entre bloques
    
    // Utility tiles
    val UtilityTileHeight = 80.dp           // Actualizado según specs (era 72dp)
    val UtilityTileSpacing = MD             // 16dp entre tiles
}

