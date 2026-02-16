package com.kodeforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = KodeForgeColors.Primary,
    onPrimary = KodeForgeColors.Surface,
    primaryContainer = KodeForgeColors.PrimaryLight,
    onPrimaryContainer = KodeForgeColors.PrimaryDark,
    
    secondary = KodeForgeColors.ProjectTeal,
    onSecondary = KodeForgeColors.Surface,
    
    background = KodeForgeColors.Background,
    onBackground = KodeForgeColors.TextPrimary,
    
    surface = KodeForgeColors.Surface,
    onSurface = KodeForgeColors.TextPrimary,
    surfaceVariant = KodeForgeColors.SurfaceVariant,
    onSurfaceVariant = KodeForgeColors.TextSecondary,
    
    outline = KodeForgeColors.Border,
    outlineVariant = KodeForgeColors.Divider,
    
    error = KodeForgeColors.ProjectRed,
    onError = KodeForgeColors.Surface
)

@Composable
fun KodeForgeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = KodeForgeTypography,
        content = content
    )
}

