package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.ui.components.UnifiedHeader
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de gestión de proyecto.
 * 
 * Permite:
 * - Editar información del proyecto
 * - Asignar/desasignar miembros del equipo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProjectScreen(
    workspace: Workspace,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var projectName by remember { mutableStateOf(project.name) }
    var projectDescription by remember { mutableStateOf(project.description ?: "") }
    var selectedMemberIds by remember { mutableStateOf(project.members.toSet()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        UnifiedHeader(
            breadcrumbs = listOf("KodeForge", project.name, "Gestionar"),
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
                text = "Gestionar Proyecto",
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
            
            // Información del proyecto
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
                    Text(
                        text = "Información del Proyecto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = { Text("Nombre del Proyecto *") },
                        placeholder = { Text("Mi Proyecto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = projectDescription,
                        onValueChange = {
                            projectDescription = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Descripción del proyecto...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Miembros del equipo
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
                    Text(
                        text = "Miembros del Equipo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Selecciona las personas que trabajarán en este proyecto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider()
                    
                    if (workspace.people.isEmpty()) {
                        Text(
                            text = "No hay personas disponibles. Crea personas primero.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        workspace.people.forEach { person ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = person.id in selectedMemberIds,
                                    onCheckedChange = { isChecked ->
                                        selectedMemberIds = if (isChecked) {
                                            selectedMemberIds + person.id
                                        } else {
                                            selectedMemberIds - person.id
                                        }
                                        errorMessage = null
                                        successMessage = null
                                    }
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (person.role != null) {
                                            Text(
                                                text = person.role,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (person.hoursPerWeekday != null) {
                                            val totalWeekHours = person.hoursPerWeekday.values.sum()
                                            Text(
                                                text = "• ${totalWeekHours.toInt()}h/sem",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Text(
                                                text = "• ${person.hoursPerDay}h/día",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                
                                if (!person.active) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            
                            Divider()
                        }
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
                        if (projectName.trim().isEmpty()) {
                            errorMessage = "El nombre del proyecto es obligatorio"
                            return@Button
                        }
                        
                        // Actualizar proyecto
                        val updatedProject = project.copy(
                            name = projectName.trim(),
                            description = projectDescription.trim().takeIf { it.isNotEmpty() },
                            members = selectedMemberIds.toList()
                        )
                        
                        val updatedWorkspace = workspace.copy(
                            projects = workspace.projects.map {
                                if (it.id == project.id) updatedProject else it
                            }
                        )
                        
                        onWorkspaceUpdate(updatedWorkspace)
                        successMessage = "✓ Proyecto actualizado correctamente"
                        
                        // Volver después de un breve delay
                        coroutineScope.launch {
                            delay(1000)
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}

