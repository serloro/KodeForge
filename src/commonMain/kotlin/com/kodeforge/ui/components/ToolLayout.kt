package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Project
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Layout común para todas las pantallas de herramientas.
 * 
 * Incluye:
 * - Header con breadcrumb
 * - Sidebar de herramientas (responsive)
 * - Área de contenido principal
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        // Header con breadcrumb
        TopAppBar(
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ProjectFlow",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = toolTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF1A1A1A)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
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

