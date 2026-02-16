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
import com.kodeforge.ui.components.UtilityTilesGrid
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Vista Proyecto (Modo Proyecto) según p2.png.
 * 
 * Muestra:
 * 1. Header con breadcrumb
 * 2. Utilidades del Proyecto (tiles)
 * 3. Timeline del Proyecto (filas por persona)
 * 4. Estadísticas del Proyecto
 * 
 * T8: Navega a pantallas de tools al hacer click en tiles.
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
        
        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Utilidades del Proyecto
            UtilityTilesGrid(
                onUtilityClick = { utilityId ->
                    onToolClick(utilityId)
                }
            )
            
            Divider(color = Color(0xFFE0E0E0))
            
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

