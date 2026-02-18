package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.components.ProjectStats
import com.kodeforge.ui.components.ProjectTimeline
import com.kodeforge.ui.components.ToolsSidebar
import com.kodeforge.ui.components.UnifiedHeader
import com.kodeforge.ui.components.UtilityTilesGrid
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Vista Proyecto (Modo Proyecto) con sidebar de herramientas.
 * 
 * Layout responsive:
 * - Sidebar lateral con herramientas (240dp)
 * - Contenido central con hub del proyecto
 * - Se adapta a diferentes tamaños de pantalla
 * 
 * Muestra:
 * 1. Header con breadcrumb
 * 2. Sidebar con herramientas
 * 3. Hub del proyecto (Timeline, Stats, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectViewScreen(
    workspace: Workspace,
    project: Project,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header unificado con breadcrumb
        UnifiedHeader(
            breadcrumbs = listOf("KodeForge", project.name),
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
                        selectedToolId = null, // null = Hub del proyecto
                        onToolClick = onToolClick,
                        onBackToHub = { /* Ya estamos en el hub */ }
                    )
                }
                
                // Contenido principal (Hub del Proyecto)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = if (showSidebar) 24.dp else 16.dp,
                            vertical = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Título del Hub
                    Text(
                        text = "Hub del Proyecto",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = KodeForgeColors.TextPrimary
                    )
                    
                    // 1. Utilidades del Proyecto (solo en móvil/tablet sin sidebar)
                    if (!showSidebar) {
                        UtilityTilesGrid(
                            onUtilityClick = { utilityId ->
                                onToolClick(utilityId)
                            }
                        )
                        Divider(color = Color(0xFFE0E0E0))
                    }
                    
                    // 2. Timeline del Proyecto
                    ProjectTimeline(
                        workspace = workspace,
                        project = project
                    )
                    
                    Divider(color = Color(0xFFE0E0E0))
                    
                    // 3. Estadísticas del Proyecto
                    ProjectStats(
                        workspace = workspace,
                        project = project
                    )
                    
                    // Espaciado final
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

