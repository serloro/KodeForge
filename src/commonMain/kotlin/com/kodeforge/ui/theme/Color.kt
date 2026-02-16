package com.kodeforge.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sistema de colores de KodeForge basado en specs/p1.png y specs/p2.png
 * Refinado para mejor contraste y consistencia visual
 */
object KodeForgeColors {
    // Primarios (azul más oscuro según specs)
    val Primary = Color(0xFF2563EB)
    val PrimaryLight = Color(0xFF3B82F6)
    val PrimaryDark = Color(0xFF1D4ED8)
    
    // Estados (más vibrantes según specs)
    val Success = Color(0xFF10B981)        // Verde más vibrante
    val Error = Color(0xFFEF4444)          // Rojo más vibrante
    val Warning = Color(0xFFF59E0B)        // Naranja
    val Info = Color(0xFF3B82F6)
    
    // Escala de grises (completa para mejor consistencia)
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
    
    // Fondos
    val Background = Color(0xFFFFFFFF)
    val BackgroundSecondary = Color(0xFFF7F8FA)  // Sidebar según specs
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF3F4F6)
    
    // Sidebar
    val SidebarBackground = Color(0xFFF7F8FA)    // Actualizado según specs
    val SidebarItemSelected = Color(0xFFEBF5FF)  // Primary con alpha
    val SidebarItemHover = Color(0xFFF3F4F6)
    
    // Texto (jerarquía clara)
    val TextPrimary = Color(0xFF111827)          // Gray900
    val TextSecondary = Color(0xFF6B7280)        // Gray500
    val TextTertiary = Color(0xFF9CA3AF)         // Gray400
    
    // Estados de personas (actualizados)
    val PersonIdle = Color(0xFF10B981)           // Success
    val PersonActive = Color(0xFFF59E0B)         // Warning
    val PersonOverload = Color(0xFFEF4444)       // Error
    
    // Colores de proyectos
    val ProjectBlue = Color(0xFF3B82F6)
    val ProjectTeal = Color(0xFF14B8A6)
    val ProjectOrange = Color(0xFFF59E0B)
    val ProjectRed = Color(0xFFEF4444)
    val ProjectPurple = Color(0xFFA855F7)
    val ProjectPink = Color(0xFFEC4899)
    
    // Bordes y divisores
    val Border = Color(0xFFE5E7EB)               // Gray200
    val Divider = Color(0xFFF3F4F6)              // Gray100
}

