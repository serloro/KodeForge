package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Task
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.components.Header
import com.kodeforge.ui.components.Sidebar
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * HomeScreen - Pantalla principal de KodeForge.
 * 
 * Layout según specs/p1.png:
 * - Header en la parte superior
 * - Sidebar a la izquierda (240dp)
 * - Main content a la derecha (resto del espacio)
 * 
 * T1: Solo implementa layout básico.
 * T2: Implementará el contenido del main (KPIs, gráficas, etc.)
 */
@Composable
fun HomeScreen(
    workspace: Workspace,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProjectId by remember { mutableStateOf(workspace.uiState.selectedProjectId) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header
        Header(
            onNewProject = {
                // TODO T4: Implementar creación de proyecto
                println("Nuevo Proyecto clicked")
            }
        )
        
        // Contenido: Sidebar + Main
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Sidebar
            Sidebar(
                projects = workspace.projects,
                people = workspace.people,
                tasks = workspace.tasks,
                selectedProjectId = selectedProjectId,
                onProjectClick = { project ->
                    selectedProjectId = project.id
                    // TODO T6: Cambiar a modo proyecto
                    println("Proyecto seleccionado: ${project.name}")
                },
                onPersonClick = { person ->
                    // TODO T3: Mostrar detalle de persona
                    println("Persona seleccionada: ${person.displayName}")
                },
                onManageProjects = {
                    // TODO T4: Abrir gestión de proyectos
                    println("Gestionar Proyectos clicked")
                },
                onManagePeople = {
                    // TODO T3: Abrir gestión de personas
                    println("Gestionar Personas clicked")
                }
            )
            
            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KodeForgeColors.Background)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder para T2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Resumen de Proyectos",
                        style = MaterialTheme.typography.displayLarge,
                        color = KodeForgeColors.TextPrimary
                    )
                    Text(
                        text = "T2 implementará: KPIs, gráficas y lista de proyectos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KodeForgeColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Workspace cargado:",
                        style = MaterialTheme.typography.titleMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "• ${workspace.projects.size} proyectos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "• ${workspace.people.size} personas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    Text(
                        text = "• ${workspace.tasks.size} tareas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                }
            }
        }
    }
}

