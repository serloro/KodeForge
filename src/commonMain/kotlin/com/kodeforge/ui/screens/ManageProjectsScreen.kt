package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.data.repository.JvmFileSystemAdapter
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.ProjectUseCases
import com.kodeforge.domain.validation.ProjectValidator
import com.kodeforge.ui.components.ProjectForm
import com.kodeforge.ui.components.ProjectListItem
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.coroutines.launch

/**
 * Pantalla para gestionar proyectos.
 * 
 * Funcionalidades:
 * - Ver lista de proyectos
 * - Buscar proyectos
 * - Crear nuevo proyecto
 * - Editar proyecto existente
 * - Eliminar proyecto
 * - Gestionar miembros del proyecto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProjectsScreen(
    workspace: Workspace,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val projectUseCases = remember {
        ProjectUseCases(
            WorkspaceRepository(JvmFileSystemAdapter()),
            ProjectValidator()
        )
    }
    
    var showProjectFormDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<Project?>(null) }
    var projectFormErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProjects = remember(workspace.projects, searchQuery) {
        projectUseCases.searchProjects(workspace, searchQuery)
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Proyectos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KodeForgeColors.Surface,
                    titleContentColor = KodeForgeColors.TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProject = null
                    projectFormErrors = emptyList()
                    showProjectFormDialog = true
                },
                containerColor = KodeForgeColors.Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear proyecto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(KodeForgeColors.Background)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar proyectos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Lista de proyectos
            if (filteredProjects.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "No se encontraron proyectos para \"$searchQuery\""
                        else
                            "No hay proyectos registrados.",
                        style = MaterialTheme.typography.titleMedium,
                        color = KodeForgeColors.TextSecondary
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    if (searchQuery.isBlank()) {
                        Button(onClick = {
                            editingProject = null
                            projectFormErrors = emptyList()
                            showProjectFormDialog = true
                        }) {
                            Text("Crear Primer Proyecto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectListItem(
                            project = project,
                            onEdit = {
                                editingProject = it
                                projectFormErrors = emptyList()
                                showProjectFormDialog = true
                            },
                            onDelete = { projectToDeleteSelected ->
                                projectToDelete = projectToDeleteSelected
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
        }
        
        // Diálogo de formulario (Crear/Editar)
        if (showProjectFormDialog) {
            ProjectForm(
                project = editingProject,
                people = workspace.people,
                onDismiss = { showProjectFormDialog = false },
                onSave = { projectToSave ->
                    scope.launch {
                        val (updatedWorkspace, errors) = if (editingProject == null) {
                            projectUseCases.createProject(
                                workspace,
                                projectToSave.name,
                                projectToSave.description,
                                projectToSave.status,
                                projectToSave.members
                            )
                        } else {
                            projectUseCases.updateProject(
                                workspace,
                                projectToSave.id,
                                projectToSave.name,
                                projectToSave.description,
                                projectToSave.status,
                                projectToSave.members
                            )
                        }
                        
                        if (errors.isEmpty()) {
                            onWorkspaceUpdate(updatedWorkspace)
                            showProjectFormDialog = false
                        } else {
                            projectFormErrors = errors
                        }
                    }
                },
                errors = projectFormErrors
            )
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteConfirmation && projectToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Eliminar Proyecto") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar el proyecto \"${projectToDelete?.name}\"?\n\nEsta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val updatedWorkspace = projectUseCases.deleteProject(
                                    workspace,
                                    projectToDelete!!.id
                                )
                                onWorkspaceUpdate(updatedWorkspace)
                                showDeleteConfirmation = false
                                projectToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KodeForgeColors.PersonOverload
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmation = false
                        projectToDelete = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

