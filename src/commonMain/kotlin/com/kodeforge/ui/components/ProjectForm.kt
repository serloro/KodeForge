package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project

/**
 * Formulario para crear o editar un proyecto.
 * 
 * @param project Proyecto a editar (null para crear)
 * @param people Lista de personas disponibles para miembros
 * @param onDismiss Callback al cerrar
 * @param onSave Callback al guardar
 * @param errors Lista de errores de validación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectForm(
    project: Project?,
    people: List<Person>,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit,
    errors: List<String> = emptyList()
) {
    var name by remember { mutableStateOf(project?.name ?: "") }
    var description by remember { mutableStateOf(project?.description ?: "") }
    var status by remember { mutableStateOf(project?.status ?: "active") }
    var members by remember { mutableStateOf(project?.members ?: emptyList()) }
    var showMemberSelector by remember { mutableStateOf(false) }
    
    val statusOptions = listOf(
        "active" to "Activo",
        "paused" to "Pausado",
        "completed" to "Completado"
    )
    var expandedStatus by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (project == null) "Crear Proyecto" else "Editar Proyecto") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    isError = errors.any { it.contains("nombre", ignoreCase = true) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    isError = errors.any { it.contains("descripción", ignoreCase = true) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // Estado (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = statusOptions.find { it.first == status }?.second ?: "Activo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    status = value
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }
                
                // Miembros
                OutlinedButton(
                    onClick = { showMemberSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (members.isEmpty()) 
                            "Seleccionar Miembros" 
                        else 
                            "Miembros (${members.size} seleccionados)"
                    )
                }
                
                // Errores
                errors.forEach { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val projectToSave = (project ?: Project(
                    id = "", // Se generará en UseCase
                    name = "",
                    createdAt = "",
                    updatedAt = ""
                )).copy(
                    name = name,
                    description = description.takeIf { it.isNotBlank() },
                    status = status,
                    members = members
                )
                onSave(projectToSave)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Selector de miembros
    if (showMemberSelector) {
        MemberSelector(
            people = people,
            selectedMembers = members,
            onDismiss = { showMemberSelector = false },
            onSave = { selectedMembers ->
                members = selectedMembers
                showMemberSelector = false
            }
        )
    }
}

