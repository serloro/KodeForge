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
import com.kodeforge.domain.model.Workspace
import com.kodeforge.smtp.SmtpServerManager
import com.kodeforge.ui.components.HomeDashboard
import com.kodeforge.ui.components.UnifiedHeader
import com.kodeforge.ui.components.Sidebar
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * HomeScreen - Pantalla principal de KodeForge.
 * 
 * Layout según specs/p1.png:
 * - Header en la parte superior
 * - Sidebar a la izquierda (280dp)
 * - Main content a la derecha (resto del espacio)
 * 
 * T1: Layout básico
 * T2: Contenido del main (KPIs, gráficas, etc.)
 * T3: Navegación a ManagePeopleScreen
 * T5: Navegación a ManageTasksScreen al seleccionar proyecto
 * T5: Navegación a PersonDetailScreen al seleccionar persona
 */
@Composable
fun HomeScreen(
    workspace: Workspace,
    smtpServerManager: SmtpServerManager,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProjectId by remember { mutableStateOf(workspace.uiState.selectedProjectId) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    // Determinar qué pantalla mostrar
    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeMainContent(
                workspace = workspace,
                selectedProjectId = selectedProjectId,
                onProjectClick = { project ->
                    selectedProjectId = project.id
                    currentScreen = Screen.ProjectView(project)
                },
                onPersonClick = { person ->
                    currentScreen = Screen.PersonDetail(person)
                },
                onSettings = {
                    currentScreen = Screen.Settings
                },
                onManageProjects = {
                    currentScreen = Screen.ManageProjects
                },
                onManagePeople = {
                    currentScreen = Screen.ManagePeople
                }
            )
        }
        is Screen.ManagePeople -> {
            ManagePeopleScreen(
                workspace = workspace,
                onWorkspaceUpdate = onWorkspaceUpdate,
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.ManageProjects -> {
            ManageProjectsScreen(
                workspace = workspace,
                onWorkspaceUpdate = onWorkspaceUpdate,
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.ProjectView -> {
            val project = workspace.projects.find { it.id == screen.project.id }
            if (project != null) {
                ProjectViewScreen(
                    workspace = workspace,
                    project = project,
                    onBack = { currentScreen = Screen.Home },
                    onToolClick = { toolType ->
                        currentScreen = Screen.Tool(toolType, project)
                    },
                    onManageProject = {
                        currentScreen = Screen.ManageProject(project.id)
                    }
                )
            } else {
                // Proyecto no encontrado, volver al home
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Home
                }
            }
        }
        is Screen.Tool -> {
            val project = workspace.projects.find { it.id == screen.project.id }
            if (project != null) {
                ToolScreen(
                    toolType = screen.toolType,
                    project = project,
                    workspace = workspace,
                    smtpServerManager = smtpServerManager,
                    onWorkspaceUpdate = onWorkspaceUpdate,
                    onBack = { currentScreen = Screen.ProjectView(project) },
                    onToolClick = { newToolType ->
                        currentScreen = Screen.Tool(newToolType, project)
                    },
                    onBackToHub = { currentScreen = Screen.ProjectView(project) }
                )
            } else {
                // Proyecto no encontrado, volver al home
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Home
                }
            }
        }
        is Screen.ManageTasks -> {
            val project = workspace.projects.find { it.id == screen.project.id }
            if (project != null) {
                ManageTasksScreen(
                    workspace = workspace,
                    project = project,
                    onWorkspaceUpdate = onWorkspaceUpdate,
                    onBack = { currentScreen = Screen.Home }
                )
            } else {
                // Proyecto no encontrado, volver al home
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Home
                }
            }
        }
        is Screen.PersonDetail -> {
            val person = workspace.people.find { it.id == screen.person.id }
            if (person != null) {
                PersonDetailScreen(
                    workspace = workspace,
                    person = person,
                    onBack = { currentScreen = Screen.Home }
                )
            } else {
                // Persona no encontrada, volver al home
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Home
                }
            }
        }
        is Screen.ManageProject -> {
            val project = workspace.projects.find { it.id == screen.projectId }
            if (project != null) {
                ManageProjectScreen(
                    workspace = workspace,
                    project = project,
                    onWorkspaceUpdate = onWorkspaceUpdate,
                    onBack = { currentScreen = Screen.ProjectView(project) }
                )
            } else {
                // Proyecto no encontrado, volver al home
                LaunchedEffect(Unit) {
                    currentScreen = Screen.Home
                }
            }
        }
        is Screen.Settings -> {
            SettingsScreen(
                workspace = workspace,
                onWorkspaceUpdate = onWorkspaceUpdate,
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
}

/**
 * Contenido principal del Home (sidebar + main content).
 */
@Composable
private fun HomeMainContent(
    workspace: Workspace,
    selectedProjectId: String?,
    onProjectClick: (com.kodeforge.domain.model.Project) -> Unit,
    onPersonClick: (com.kodeforge.domain.model.Person) -> Unit,
    onManageProjects: () -> Unit,
    onManagePeople: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header unificado
        UnifiedHeader(
            breadcrumbs = listOf("KodeForge"),
            onBack = null,
            onSettings = onSettings
        )
        
        // Contenido: Sidebar + Main
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sidebar
            Sidebar(
                projects = workspace.projects,
                people = workspace.people,
                tasks = workspace.tasks,
                selectedProjectId = selectedProjectId,
                onProjectClick = onProjectClick,
                onPersonClick = onPersonClick,
                onManageProjects = onManageProjects,
                onManagePeople = onManagePeople
            )
            
            // Dashboard principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KodeForgeColors.Background)
            ) {
                HomeDashboard(workspace = workspace)
            }
        }
    }
}

/**
 * Sealed class para representar las diferentes pantallas.
 */
private sealed class Screen {
    object Home : Screen()
    object ManagePeople : Screen()
    object ManageProjects : Screen()
    object Settings : Screen()
    data class ProjectView(val project: Project) : Screen()
    data class ManageTasks(val project: Project) : Screen()
    data class PersonDetail(val person: Person) : Screen()
    data class Tool(val toolType: String, val project: Project) : Screen()
    data class ManageProject(val projectId: String) : Screen()
}
