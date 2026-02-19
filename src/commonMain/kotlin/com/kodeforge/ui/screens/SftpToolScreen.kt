package com.kodeforge.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.SftpConnection
import com.kodeforge.domain.model.SftpTool
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.SftpUseCases
import com.kodeforge.ui.components.SftpConnectionForm
import com.kodeforge.ui.components.SftpConnectionItem
import com.kodeforge.ui.components.SftpFileExplorer
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla principal del tool SFTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpToolScreen(
    workspace: Workspace,
    projectId: String,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val useCases = remember { SftpUseCases() }
    val sftpTool = useCases.getSftpTool(workspace, projectId)
    
    var showForm by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<SftpConnection?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var connectionToDelete by remember { mutableStateOf<SftpConnection?>(null) }
    var showExplorer by remember { mutableStateOf(false) }
    var explorerConnection by remember { mutableStateOf<SftpConnection?>(null) }
    
    // El explorador se renderiza dentro del ToolLayout para que no cambie la interfaz de golpe.
    
    ToolLayout(
        project = project,
        toolTitle = "SFTP/SSH",
        selectedToolId = "sftp",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // IMPORTANTE:
            // Evita usar `return@Column` dentro de un Composable. En Compose esto puede romper el
            // balanceo interno de grupos durante recomposición y provocar errores como:
            // IndexOutOfBoundsException (Stack.pop / exitGroup).
            if (showExplorer && explorerConnection != null) {
                SftpFileExplorer(
                    connection = explorerConnection!!,
                    onClose = {
                        showExplorer = false
                        explorerConnection = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {

                // Header con toggle enabled
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Conexiones SFTP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = "Gestiona tus conexiones SFTP/SSH",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (sftpTool?.enabled == true) "Habilitado" else "Deshabilitado",
                                    fontSize = 14.sp,
                                    color = if (sftpTool?.enabled == true) Color(0xFF4CAF50) else Color(0xFF757575),
                                    fontWeight = FontWeight.Medium
                                )

                                Switch(
                                    checked = sftpTool?.enabled == true,
                                    onCheckedChange = { enabled ->
                                        val result = if (enabled) {
                                            useCases.enableSftpTool(workspace, projectId)
                                        } else {
                                            useCases.disableSftpTool(workspace, projectId)
                                        }
                                        result.onSuccess { updatedWorkspace ->
                                            onWorkspaceUpdate(updatedWorkspace)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF4CAF50),
                                        checkedTrackColor = Color(0xFFC8E6C9)
                                    )
                                )
                            }
                            Text(
                                text = "Habilita para usar conexiones SFTP",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }

                // Contenido
                if (showForm) {
            // Mostrar formulario
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                SftpConnectionForm(
                    connection = editingConnection,
                    onSave = { name, host, port, username, authType, authValueRef ->
                        val result = if (editingConnection == null) {
                            // Crear nueva conexión
                            useCases.addConnection(
                                workspace = workspace,
                                projectId = projectId,
                                name = name,
                                host = host,
                                port = port,
                                username = username,
                                authType = authType,
                                authValueRef = authValueRef
                            )
                        } else {
                            // Actualizar conexión existente
                            useCases.updateConnection(
                                workspace = workspace,
                                projectId = projectId,
                                connectionId = editingConnection!!.id,
                                name = name,
                                host = host,
                                port = port,
                                username = username,
                                authType = authType,
                                authValueRef = authValueRef
                            )
                        }
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showForm = false
                            editingConnection = null
                        }.onFailure { error ->
                            // TODO: Mostrar error en un snackbar
                            println("Error al guardar conexión: ${error.message}")
                        }
                    },
                    onCancel = {
                        showForm = false
                        editingConnection = null
                    },
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
                } else {
            // Mostrar lista de conexiones
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (sftpTool?.connections.isNullOrEmpty()) {
                    // Estado vacío
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No hay conexiones SFTP",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Crea tu primera conexión para comenzar",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showForm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nueva Conexión")
                        }
                    }
                } else {
                    // Lista de conexiones
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sftpTool?.connections ?: emptyList()) { connection ->
                            SftpConnectionItem(
                                connection = connection,
                                onConnect = {
                                    explorerConnection = connection
                                    showExplorer = true
                                },
                                onEdit = {
                                    editingConnection = connection
                                    showForm = true
                                },
                                onDelete = {
                                    connectionToDelete = connection
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
                
                // FAB para añadir conexión
                if (!sftpTool?.connections.isNullOrEmpty()) {
                    FloatingActionButton(
                        onClick = { showForm = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva conexión"
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && connectionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                connectionToDelete = null
            },
            title = {
                Text(
                    text = "Eliminar Conexión",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar la conexión \"${connectionToDelete?.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectionToDelete?.let { connection ->
                            val result = useCases.deleteConnection(
                                workspace = workspace,
                                projectId = projectId,
                                connectionId = connection.id
                            )
                            result.onSuccess { updatedWorkspace ->
                                onWorkspaceUpdate(updatedWorkspace)
                                showDeleteDialog = false
                                connectionToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        connectionToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
}
}
