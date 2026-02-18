package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Project
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Layout común para todas las pantallas de herramientas.
 * 
 * Incluye:
 * - Header unificado con breadcrumb
 * - Sidebar de herramientas (responsive)
 * - Área de contenido principal
 */
@Composable
fun ToolLayout(
    project: Project,
    toolTitle: String,
    selectedToolId: String,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit,
    onBackToHub: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header unificado con breadcrumb
        UnifiedHeader(
            breadcrumbs = listOf("KodeForge", project.name, toolTitle),
            onBack = onBack
        )
        
        // Layout responsive: Sidebar + Contenido
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val showSidebar = maxWidth >= 800.dp
            
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sidebar de herramientas (solo en pantallas grandes)
                if (showSidebar) {
                    ToolsSidebar(
                        selectedToolId = selectedToolId,
                        onToolClick = onToolClick,
                        onBackToHub = onBackToHub
                    )
                }
                
                // Contenido principal
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KodeForgeColors.Background)
                ) {
                    content()
                }
            }
        }
    }
}

