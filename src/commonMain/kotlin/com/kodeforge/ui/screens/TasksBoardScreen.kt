package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Task
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.TaskUseCases
import com.kodeforge.ui.components.TaskForm
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla de tareas con vista de tablón Kanban.
 * 
 * Características:
 * - Vista de columnas por estado
 * - Filtros en la cabecera (por defecto: solo abiertas)
 * - Tarjetas con avatar de persona asignada
 * - Estados configurables desde settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBoardScreen(
    workspace: Workspace,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val taskUseCases = remember { TaskUseCases() }
    val taskStatuses = workspace.app.settings.ui.taskStatuses
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Filtros
    var showOnlyOpen by remember { mutableStateOf(true) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedAssigneeFilter by remember { mutableStateOf<String?>(null) }
    
    // Obtener tareas del proyecto
    val allProjectTasks = remember(workspace.tasks, project.id) {
        taskUseCases.getTasksByProject(workspace, project.id)
    }
    
    // Filtrar personas: solo miembros del proyecto
    val projectMembers = remember(workspace.people, project.members) {
        workspace.people.filter { person -> person.id in project.members }
    }
    
    // Aplicar filtros
    val filteredTasks = remember(allProjectTasks, showOnlyOpen, selectedAssigneeFilter) {
        allProjectTasks.filter { task ->
            val matchesOpenFilter = if (showOnlyOpen) {
                task.status != "done"
            } else {
                true
            }
            
            val matchesAssigneeFilter = if (selectedAssigneeFilter != null) {
                task.assigneeId == selectedAssigneeFilter
            } else {
                true
            }
            
            matchesOpenFilter && matchesAssigneeFilter
        }
    }
    
    ToolLayout(
        project = project,
        toolTitle = "Tasks",
        selectedToolId = "tasks",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra de filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtros
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Filtros",
                            tint = Color(0xFF666666)
                        )
                        
                        FilterChip(
                            selected = showOnlyOpen,
                            onClick = { showOnlyOpen = !showOnlyOpen },
                            label = { Text("Solo abiertas") }
                        )
                        
                        // Filtro por persona
                        var expandedPersonFilter by remember { mutableStateOf(false) }
                        
                        Box {
                            FilterChip(
                                selected = selectedAssigneeFilter != null,
                                onClick = { expandedPersonFilter = true },
                                label = {
                                    Text(
                                        if (selectedAssigneeFilter != null) {
                                            projectMembers.find { it.id == selectedAssigneeFilter }?.displayName ?: "Persona"
                                        } else {
                                            "Todas las personas"
                                        }
                                    )
                                },
                                trailingIcon = if (selectedAssigneeFilter != null) {
                                    {
                                        IconButton(
                                            onClick = { selectedAssigneeFilter = null },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Text("×", style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                } else null
                            )
                            
                            DropdownMenu(
                                expanded = expandedPersonFilter,
                                onDismissRequest = { expandedPersonFilter = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas las personas") },
                                    onClick = {
                                        selectedAssigneeFilter = null
                                        expandedPersonFilter = false
                                    }
                                )
                                Divider()
                                projectMembers.forEach { person ->
                                    DropdownMenuItem(
                                        text = { Text(person.displayName) },
                                        onClick = {
                                            selectedAssigneeFilter = person.id
                                            expandedPersonFilter = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "${filteredTasks.size} tareas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                    
                    // Botón crear tarea
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
                }
            }
            
            // Mensaje de error
            errorMessage?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Tablón Kanban
            if (filteredTasks.isEmpty()) {
                // Estado vacío
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
                            text = "No hay tareas que mostrar",
                            style = MaterialTheme.typography.titleMedium,
                            color = KodeForgeColors.TextSecondary
                        )
                        
                        if (showOnlyOpen && allProjectTasks.isNotEmpty()) {
                            Text(
                                text = "Todas las tareas están completadas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                            TextButton(onClick = { showOnlyOpen = false }) {
                                Text("Mostrar todas")
                            }
                        } else {
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
                }
            } else {
                // Vista de tablón
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(taskStatuses.sortedBy { it.order }) { status ->
                        KanbanColumn(
                            status = status,
                            tasks = filteredTasks.filter { it.status == status.id },
                            people = workspace.people,
                            onTaskClick = { taskToEdit = it },
                            onTaskDelete = { taskToDelete = it }
                        )
                    }
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
                    availablePeople = projectMembers,
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
                    availablePeople = projectMembers,
                    onSave = { title, costHours, description, status, priority, assigneeId ->
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
    
    // Dialog: Confirmar Eliminación
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Eliminar Tarea") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la tarea \"${task.title}\"?")
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

/**
 * Columna del tablón Kanban.
 */
@Composable
private fun KanbanColumn(
    status: com.kodeforge.domain.model.TaskStatus,
    tasks: List<Task>,
    people: List<com.kodeforge.domain.model.Person>,
    onTaskClick: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header de la columna
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = parseHexColor(status.color),
                                shape = CircleShape
                            )
                    )
                    
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
                
                Text(
                    text = "${tasks.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
            
            Divider()
            
            // Lista de tareas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks.sortedBy { it.priority }) { task ->
                    TaskCard(
                        task = task,
                        assignee = task.assigneeId?.let { id -> people.find { it.id == id } },
                        onClick = { onTaskClick(task) },
                        onDelete = { onTaskDelete(task) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de tarea en el tablón.
 */
@Composable
private fun TaskCard(
    task: Task,
    assignee: com.kodeforge.domain.model.Person?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Descripción (si existe)
            task.description?.let { desc ->
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Footer: Avatar + Horas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar de persona asignada
                if (assignee != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(KodeForgeColors.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = assignee.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = assignee.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                } else {
                    Text(
                        text = "Sin asignar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF999999)
                    )
                }
                
                // Horas
                Text(
                    text = "${task.doneHours.toInt()}/${task.costHours.toInt()}h",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * Parsea un color hexadecimal a Color de Compose.
 */
private fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        val colorInt = cleanHex.toLong(16)
        Color(
            red = ((colorInt shr 16) and 0xFF) / 255f,
            green = ((colorInt shr 8) and 0xFF) / 255f,
            blue = (colorInt and 0xFF) / 255f,
            alpha = 1f
        )
    } catch (e: Exception) {
        Color.Gray // Color por defecto si hay error
    }
}

