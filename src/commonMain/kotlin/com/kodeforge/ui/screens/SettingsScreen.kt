package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.TaskStatus
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.components.UnifiedHeader
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla de configuración de la aplicación.
 * 
 * Permite configurar:
 * - Estados de tareas (Kanban)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    workspace: Workspace,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var taskStatuses by remember { mutableStateOf(workspace.app.settings.ui.taskStatuses.toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var statusToEdit by remember { mutableStateOf<TaskStatus?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        UnifiedHeader(
            breadcrumbs = listOf("KodeForge", "Configuración"),
            onBack = onBack
        )
        
        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título
            Text(
                text = "Configuración de la Aplicación",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Mensajes
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            if (successMessage != null) {
                Surface(
                    color = KodeForgeColors.Success.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = successMessage!!,
                        color = KodeForgeColors.Success,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Estados de tareas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estados de Tareas (Kanban)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configura las columnas del tablero Kanban",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(
                            onClick = { showAddDialog = true }
                        ) {
                            Icon(Icons.Default.Add, "Agregar estado")
                        }
                    }
                    
                    Divider()
                    
                    // Lista de estados
                    taskStatuses.sortedBy { it.order }.forEach { status ->
                        TaskStatusItem(
                            status = status,
                            onEdit = { statusToEdit = it },
                            onDelete = {
                                if (taskStatuses.size > 1) {
                                    taskStatuses = taskStatuses.filter { s -> s.id != it.id }.toMutableList()
                                    errorMessage = null
                                    successMessage = null
                                } else {
                                    errorMessage = "Debe haber al menos un estado"
                                }
                            },
                            onMoveUp = {
                                val index = taskStatuses.indexOfFirst { s -> s.id == it.id }
                                if (index > 0) {
                                    val temp = taskStatuses[index]
                                    taskStatuses[index] = taskStatuses[index - 1].copy(order = temp.order)
                                    taskStatuses[index - 1] = temp.copy(order = taskStatuses[index].order)
                                    taskStatuses = taskStatuses.toMutableList()
                                }
                            },
                            onMoveDown = {
                                val index = taskStatuses.indexOfFirst { s -> s.id == it.id }
                                if (index < taskStatuses.size - 1) {
                                    val temp = taskStatuses[index]
                                    taskStatuses[index] = taskStatuses[index + 1].copy(order = temp.order)
                                    taskStatuses[index + 1] = temp.copy(order = taskStatuses[index].order)
                                    taskStatuses = taskStatuses.toMutableList()
                                }
                            }
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        // Validar
                        if (taskStatuses.isEmpty()) {
                            errorMessage = "Debe haber al menos un estado"
                            return@Button
                        }
                        
                        // Actualizar workspace
                        val updatedSettings = workspace.app.settings.copy(
                            ui = workspace.app.settings.ui.copy(
                                taskStatuses = taskStatuses.mapIndexed { index, status ->
                                    status.copy(order = index)
                                }
                            )
                        )
                        
                        val updatedWorkspace = workspace.copy(
                            app = workspace.app.copy(
                                settings = updatedSettings
                            )
                        )
                        
                        onWorkspaceUpdate(updatedWorkspace)
                        successMessage = "✓ Configuración guardada correctamente"
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
    
    // Dialog para agregar/editar estado
    if (showAddDialog || statusToEdit != null) {
        TaskStatusDialog(
            status = statusToEdit,
            onSave = { id, label, color ->
                if (statusToEdit != null) {
                    // Editar
                    taskStatuses = taskStatuses.map {
                        if (it.id == statusToEdit!!.id) {
                            it.copy(label = label, color = color)
                        } else it
                    }.toMutableList()
                } else {
                    // Agregar
                    val newStatus = TaskStatus(
                        id = id,
                        label = label,
                        color = color,
                        order = taskStatuses.size
                    )
                    taskStatuses = (taskStatuses + newStatus).toMutableList()
                }
                showAddDialog = false
                statusToEdit = null
                errorMessage = null
                successMessage = null
            },
            onCancel = {
                showAddDialog = false
                statusToEdit = null
            }
        )
    }
}

@Composable
private fun TaskStatusItem(
    status: TaskStatus,
    onEdit: (TaskStatus) -> Unit,
    onDelete: (TaskStatus) -> Unit,
    onMoveUp: (TaskStatus) -> Unit,
    onMoveDown: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = parseHexColor(status.color),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        
        // Label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "ID: ${status.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Botones
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onMoveUp(status) }) {
                Text("↑")
            }
            TextButton(onClick = { onMoveDown(status) }) {
                Text("↓")
            }
            TextButton(onClick = { onEdit(status) }) {
                Text("Editar")
            }
            IconButton(onClick = { onDelete(status) }) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    Divider()
}

@Composable
private fun TaskStatusDialog(
    status: TaskStatus?,
    onSave: (id: String, label: String, color: String) -> Unit,
    onCancel: () -> Unit
) {
    var id by remember { mutableStateOf(status?.id ?: "") }
    var label by remember { mutableStateOf(status?.label ?: "") }
    var color by remember { mutableStateOf(status?.color ?: "#9E9E9E") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(if (status == null) "Nuevo Estado" else "Editar Estado")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                OutlinedTextField(
                    value = id,
                    onValueChange = {
                        id = it.lowercase().replace(" ", "_")
                        errorMessage = null
                    },
                    label = { Text("ID *") },
                    placeholder = { Text("backlog") },
                    enabled = status == null, // Solo editable al crear
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        errorMessage = null
                    },
                    label = { Text("Etiqueta *") },
                    placeholder = { Text("Backlog") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = color,
                    onValueChange = {
                        color = it
                        errorMessage = null
                    },
                    label = { Text("Color (Hex) *") },
                    placeholder = { Text("#9E9E9E") },
                    singleLine = true,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = parseHexColor(color),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (id.isBlank()) {
                        errorMessage = "El ID es obligatorio"
                        return@Button
                    }
                    if (label.isBlank()) {
                        errorMessage = "La etiqueta es obligatoria"
                        return@Button
                    }
                    if (!color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                        errorMessage = "Color inválido (formato: #RRGGBB)"
                        return@Button
                    }
                    onSave(id, label, color)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        Color(
            red = ((colorInt shr 16) and 0xFF) / 255f,
            green = ((colorInt shr 8) and 0xFF) / 255f,
            blue = (colorInt and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Color.Gray
    }
}

