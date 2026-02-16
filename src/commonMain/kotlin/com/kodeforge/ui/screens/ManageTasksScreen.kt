package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Task
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.TaskUseCases
import com.kodeforge.ui.components.AssignTaskDialog
import com.kodeforge.ui.components.TaskForm
import com.kodeforge.ui.components.TaskListItem
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla "Gestionar Tareas" de un proyecto (parte inicial de T5).
 * 
 * Funcionalidades:
 * - Lista de tareas del proyecto
 * - Crear tarea
 * - Editar tarea
 * - Eliminar tarea
 * - Asignar/desasignar tarea a persona
 * 
 * NO implementado aún (siguiente fase):
 * - Calendario visual
 * - Scheduler (distribución automática)
 * - Modo proyecto completo (T6)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTasksScreen(
    workspace: Workspace,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val taskUseCases = remember { TaskUseCases() }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskToAssign by remember { mutableStateOf<Task?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Obtener tareas del proyecto
    val projectTasks = remember(workspace.tasks, project.id) {
        taskUseCases.getTasksByProject(workspace, project.id)
    }
    
    // Filtrar personas: solo miembros del proyecto (T6B)
    val projectMembers = remember(workspace.people, project.members) {
        workspace.people.filter { person -> person.id in project.members }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header
        TopAppBar(
            title = { Text("Proyecto: ${project.name}") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            },
            actions = {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KodeForgeColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Tarea")
                }
                Spacer(Modifier.width(16.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Mensaje de error global
        errorMessage?.let { error ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Lista de tareas o empty state
        if (projectTasks.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No hay tareas en este proyecto",
                        style = MaterialTheme.typography.titleMedium,
                        color = KodeForgeColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KodeForgeColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Crear Primera Tarea")
                    }
                }
            }
        } else {
            // Lista de tareas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(projectTasks, key = { it.id }) { task ->
                    val assignee = task.assigneeId?.let { id ->
                        workspace.people.find { it.id == id }
                    }
                    
                    TaskListItem(
                        task = task,
                        assignee = assignee,
                        onAssign = { taskToAssign = task },
                        onEdit = { taskToEdit = task },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }
    }
    
    // Dialog: Crear Tarea
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                TaskForm(
                    task = null,
                    availablePeople = projectMembers, // Solo miembros del proyecto (T6B)
                    onSave = { title, costHours, description, status, priority, assigneeId ->
                        val result = taskUseCases.createTask(
                            workspace = workspace,
                            projectId = project.id,
                            title = title,
                            costHours = costHours,
                            description = description,
                            status = status,
                            priority = priority,
                            assigneeId = assigneeId
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showCreateDialog = false
                            errorMessage = null
                        }.onFailure { error ->
                            errorMessage = error.message
                        }
                    },
                    onCancel = { showCreateDialog = false }
                )
            }
        }
    }
    
    // Dialog: Editar Tarea
    taskToEdit?.let { task ->
        Dialog(onDismissRequest = { taskToEdit = null }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                TaskForm(
                    task = task,
                    availablePeople = projectMembers, // Solo miembros del proyecto (T6B)
                    onSave = { title, costHours, description, status, priority, assigneeId ->
                        // Primero actualizar campos básicos
                        val updateResult = taskUseCases.updateTask(
                            workspace = workspace,
                            taskId = task.id,
                            title = title,
                            costHours = costHours,
                            description = description,
                            status = status,
                            priority = priority
                        )
                        
                        updateResult.onSuccess { updatedWorkspace ->
                            // Si cambió la asignación, actualizar también
                            if (assigneeId != task.assigneeId) {
                                val assignResult = if (assigneeId != null) {
                                    taskUseCases.assignTaskToPerson(
                                        workspace = updatedWorkspace,
                                        taskId = task.id,
                                        personId = assigneeId,
                                        costHours = costHours
                                    )
                                } else {
                                    taskUseCases.unassignTask(
                                        workspace = updatedWorkspace,
                                        taskId = task.id
                                    )
                                }
                                
                                assignResult.onSuccess { finalWorkspace ->
                                    onWorkspaceUpdate(finalWorkspace)
                                    taskToEdit = null
                                    errorMessage = null
                                }.onFailure { error ->
                                    errorMessage = error.message
                                }
                            } else {
                                onWorkspaceUpdate(updatedWorkspace)
                                taskToEdit = null
                                errorMessage = null
                            }
                        }.onFailure { error ->
                            errorMessage = error.message
                        }
                    },
                    onCancel = { taskToEdit = null }
                )
            }
        }
    }
    
    // Dialog: Asignar Tarea
    taskToAssign?.let { task ->
        Dialog(onDismissRequest = { taskToAssign = null }) {
            AssignTaskDialog(
                task = task,
                availablePeople = projectMembers, // Solo miembros del proyecto (T6B)
                onAssign = { personId ->
                    val result = taskUseCases.assignTaskToPerson(
                        workspace = workspace,
                        taskId = task.id,
                        personId = personId,
                        costHours = task.costHours
                    )
                    
                    result.onSuccess { updatedWorkspace ->
                        onWorkspaceUpdate(updatedWorkspace)
                        taskToAssign = null
                        errorMessage = null
                    }.onFailure { error ->
                        errorMessage = error.message
                    }
                },
                onDismiss = { taskToAssign = null }
            )
        }
    }
    
    // Dialog: Confirmar Eliminación
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Eliminar Tarea") },
            text = { 
                Text("¿Estás seguro de que deseas eliminar la tarea \"${task.title}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = taskUseCases.deleteTask(
                            workspace = workspace,
                            taskId = task.id
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            taskToDelete = null
                            errorMessage = null
                        }.onFailure { error ->
                            errorMessage = error.message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

