package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.PersonUseCases
import com.kodeforge.ui.components.PersonForm
import com.kodeforge.ui.components.PersonListItem
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla "Gestionar Personas" (T3).
 * 
 * Funcionalidades:
 * - Lista de personas
 * - Buscador
 * - Crear persona (modal)
 * - Editar persona (modal)
 * - Eliminar persona (confirmación)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePeopleScreen(
    workspace: Workspace,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val personUseCases = remember { PersonUseCases() }
    
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<Person?>(null) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Filtrar personas según búsqueda
    val filteredPeople = remember(workspace.people, searchQuery) {
        personUseCases.searchPeople(workspace, searchQuery)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header
        TopAppBar(
            title = { Text("Gestionar Personas") },
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
                    Text("Crear Persona")
                }
                Spacer(Modifier.width(16.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar personas...") },
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
        
        // Mensaje de error global
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
        
        // Lista de personas o empty state
        if (filteredPeople.isEmpty()) {
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
                        text = if (searchQuery.isEmpty()) {
                            "No hay personas registradas"
                        } else {
                            "No se encontraron resultados para \"$searchQuery\""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = KodeForgeColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    if (searchQuery.isEmpty()) {
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KodeForgeColors.Primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Crear Primera Persona")
                        }
                    }
                }
            }
        } else {
            // Lista de personas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredPeople, key = { it.id }) { person ->
                    PersonListItem(
                        person = person,
                        onEdit = { personToEdit = person },
                        onDelete = { personToDelete = person }
                    )
                }
            }
        }
    }
    
    // Dialog: Crear Persona
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                PersonForm(
                    person = null,
                    onSave = { displayName, hoursPerDay, hoursPerWeekday, role, tags, active ->
                        val result = personUseCases.createPerson(
                            workspace = workspace,
                            displayName = displayName,
                            hoursPerDay = hoursPerDay,
                            hoursPerWeekday = hoursPerWeekday,
                            role = role,
                            tags = tags,
                            active = active
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
    
    // Dialog: Editar Persona
    personToEdit?.let { person ->
        Dialog(onDismissRequest = { personToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                PersonForm(
                    person = person,
                    onSave = { displayName, hoursPerDay, hoursPerWeekday, role, tags, active ->
                        val result = personUseCases.updatePerson(
                            workspace = workspace,
                            personId = person.id,
                            displayName = displayName,
                            hoursPerDay = hoursPerDay,
                            hoursPerWeekday = hoursPerWeekday,
                            updateWeekdayHours = true,
                            role = role,
                            tags = tags,
                            active = active
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            personToEdit = null
                            errorMessage = null
                        }.onFailure { error ->
                            errorMessage = error.message
                        }
                    },
                    onCancel = { personToEdit = null }
                )
            }
        }
    }
    
    // Dialog: Confirmar Eliminación
    personToDelete?.let { person ->
        AlertDialog(
            onDismissRequest = { personToDelete = null },
            title = { Text("Eliminar Persona") },
            text = { 
                Text("¿Estás seguro de que deseas eliminar a \"${person.displayName}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = personUseCases.deletePerson(
                            workspace = workspace,
                            personId = person.id
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            personToDelete = null
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
                TextButton(onClick = { personToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

