package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.PersonUseCases
import com.kodeforge.domain.usecases.PlanningUseCases
import com.kodeforge.ui.components.PersonCalendar
import com.kodeforge.ui.components.PersonForm
import com.kodeforge.ui.components.PersonSummaryCard
import com.kodeforge.ui.components.TaskListCard
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Pantalla de detalle de persona.
 * 
 * Muestra:
 * - Resumen (KPIs)
 * - Calendario/timeline horizontal
 * - Lista de tareas activas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    workspace: Workspace,
    person: Person,
    onWorkspaceUpdate: (Workspace) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val planningUseCases = remember { PlanningUseCases() }
    val personUseCases = remember { PersonUseCases() }
    var showEditDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Calcular datos
    val activeTasks = remember(workspace.tasks, person.id) {
        workspace.tasks.filter { 
            it.assigneeId == person.id && it.status != "completed" 
        }
    }
    
    val scheduleBlocks = remember(workspace.planning.scheduleBlocks, person.id) {
        planningUseCases.getScheduleForPerson(workspace, person.id)
    }
    
    val plannedHours = remember(scheduleBlocks) {
        scheduleBlocks.sumOf { it.hoursPlanned }
    }
    
    val doneHours = remember(activeTasks) {
        activeTasks.sumOf { it.doneHours }
    }
    
    val totalHours = remember(activeTasks) {
        activeTasks.sumOf { it.costHours }
    }
    
    val estimatedEndDate = remember(workspace, person.id) {
        planningUseCases.getEstimatedEndDate(workspace, person.id)
    }
    
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header
        TopAppBar(
            title = { 
                Column {
                    Text(person.displayName)
                    person.role?.let { role ->
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            },
            actions = {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, "Editar persona")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Card Resumen
            PersonSummaryCard(
                activeTasksCount = activeTasks.size,
                plannedHours = plannedHours,
                doneHours = doneHours,
                totalHours = totalHours,
                estimatedEndDate = estimatedEndDate
            )
            
            // Card Calendario
            PersonCalendar(
                scheduleBlocks = scheduleBlocks,
                tasks = workspace.tasks,
                today = today
            )
            
            // Card Tareas Activas
            TaskListCard(
                tasks = activeTasks
            )
            
            // Espaciador final
            Spacer(Modifier.height(16.dp))
        }
    }
    
    // Dialog para editar persona
    if (showEditDialog) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                PersonForm(
                    person = person,
                    availableProjects = workspace.projects,
                    currentProjectIds = workspace.projects.filter { person.id in it.members }.map { it.id },
                    onSave = { displayName, hoursPerDay, hoursPerWeekday, role, tags, active, projectIds, avatar ->
                        val result = personUseCases.updatePerson(
                            workspace = workspace,
                            personId = person.id,
                            displayName = displayName,
                            hoursPerDay = hoursPerDay,
                            hoursPerWeekday = hoursPerWeekday,
                            updateWeekdayHours = true,
                            role = role,
                            tags = tags,
                            active = active,
                            avatar = avatar
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            // Actualizar asignación de proyectos
                            var finalWorkspace = updatedWorkspace
                            
                            val currentProjects = finalWorkspace.projects.filter { person.id in it.members }
                            val currentProjectIds = currentProjects.map { it.id }.toSet()
                            val newProjectIds = projectIds.toSet()
                            
                            val projectsToAdd = newProjectIds - currentProjectIds
                            val projectsToRemove = currentProjectIds - newProjectIds
                            
                            projectsToAdd.forEach { projectId ->
                                val project = finalWorkspace.projects.find { it.id == projectId }
                                if (project != null) {
                                    val updatedProject = project.copy(
                                        members = project.members + person.id
                                    )
                                    finalWorkspace = finalWorkspace.copy(
                                        projects = finalWorkspace.projects.map {
                                            if (it.id == projectId) updatedProject else it
                                        }
                                    )
                                }
                            }
                            
                            projectsToRemove.forEach { projectId ->
                                val project = finalWorkspace.projects.find { it.id == projectId }
                                if (project != null) {
                                    val updatedProject = project.copy(
                                        members = project.members.filter { it != person.id }
                                    )
                                    finalWorkspace = finalWorkspace.copy(
                                        projects = finalWorkspace.projects.map {
                                            if (it.id == projectId) updatedProject else it
                                        }
                                    )
                                }
                            }
                            
                            onWorkspaceUpdate(finalWorkspace)
                            showEditDialog = false
                            errorMessage = null
                        }.onFailure { error ->
                            errorMessage = error.message
                        }
                    },
                    onCancel = { showEditDialog = false }
                )
            }
        }
    }
}

