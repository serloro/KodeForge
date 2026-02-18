package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.DbToolUseCases
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla rediseñada del tool de base de datos.
 * 
 * Flujo:
 * 1. Seleccionar o crear una conexión
 * 2. Una vez seleccionada, mostrar tabs:
 *    - Ejecutar SQL
 *    - SQL Guardadas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbToolScreen(
    workspace: Workspace,
    projectId: String,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val dbTool = useCases.getDbTool(workspace, projectId)
    val connections = dbTool?.connections ?: emptyList()
    
    var selectedConnectionId by remember { mutableStateOf<String?>(connections.firstOrNull()?.id) }
    var showCreateConnectionDialog by remember { mutableStateOf(false) }
    
    ToolLayout(
        project = project,
        toolTitle = "Base de Datos",
        selectedToolId = "bbdd",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        if (connections.isEmpty()) {
            // Estado vacío: No hay conexiones
            EmptyConnectionsState(
                onCreateConnection = { showCreateConnectionDialog = true }
            )
        } else {
            // Hay conexiones: Mostrar selector + contenido
            Column(modifier = Modifier.fillMaxSize()) {
                // Selector de conexión
                ConnectionSelector(
                    connections = connections,
                    selectedConnectionId = selectedConnectionId,
                    onConnectionSelect = { selectedConnectionId = it },
                    onCreateConnection = { showCreateConnectionDialog = true },
                    onEditConnection = { connection ->
                        // TODO: Implementar edición
                    },
                    onDeleteConnection = { connection ->
                        // TODO: Implementar eliminación
                    }
                )
                
                Divider()
                
                // Contenido según conexión seleccionada
                val selectedConnection = connections.find { it.id == selectedConnectionId }
                if (selectedConnection != null) {
                    ConnectionContent(
                        workspace = workspace,
                        projectId = projectId,
                        connection = selectedConnection,
                        dbTool = dbTool,
                        onWorkspaceUpdate = onWorkspaceUpdate
                    )
                }
            }
        }
    }
    
    // Dialog: Crear Conexión
    if (showCreateConnectionDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCreateConnectionDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.width(600.dp)
            ) {
                com.kodeforge.ui.components.DbConnectionForm(
                    connection = null,
                    onSave = { name, type, host, port, database, username, authType, authValueRef ->
                        val result = useCases.addConnection(
                            workspace = workspace,
                            projectId = projectId,
                            name = name,
                            type = type,
                            host = host,
                            port = port,
                            database = database,
                            username = username,
                            authType = authType,
                            authValueRef = authValueRef
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showCreateConnectionDialog = false
                            // Seleccionar la nueva conexión
                            val newConnection = updatedWorkspace.projects
                                .find { it.id == projectId }
                                ?.tools?.dbTools?.connections?.lastOrNull()
                            selectedConnectionId = newConnection?.id
                        }
                    },
                    onCancel = { showCreateConnectionDialog = false }
                )
            }
        }
    }
}

/**
 * Estado vacío cuando no hay conexiones.
 */
@Composable
private fun EmptyConnectionsState(
    onCreateConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🗄️",
                style = MaterialTheme.typography.displayLarge
            )
            
            Text(
                text = "No hay conexiones configuradas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextPrimary
            )
            
            Text(
                text = "Crea una conexión a tu base de datos para comenzar",
                style = MaterialTheme.typography.bodyLarge,
                color = KodeForgeColors.TextSecondary
            )
            
            Button(
                onClick = onCreateConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Crear Conexión")
            }
        }
    }
}

/**
 * Selector de conexión en la parte superior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionSelector(
    connections: List<DbConnection>,
    selectedConnectionId: String?,
    onConnectionSelect: (String) -> Unit,
    onCreateConnection: () -> Unit,
    onEditConnection: (DbConnection) -> Unit,
    onDeleteConnection: (DbConnection) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Conexión activa",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF666666)
                )
                
                // Dropdown de conexiones
                var expanded by remember { mutableStateOf(false) }
                val selectedConnection = connections.find { it.id == selectedConnectionId }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedConnection?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar conexión") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        connections.forEach { connection ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(getDbIcon(connection.type))
                                        Column {
                                            Text(
                                                text = connection.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${connection.type.uppercase()} • ${connection.host}:${connection.port}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF666666)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onConnectionSelect(connection.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Button(
                onClick = onCreateConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nueva")
            }
        }
    }
}

/**
 * Contenido principal con tabs para ejecutar SQL y ver SQL guardadas.
 */
@Composable
private fun ConnectionContent(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ejecutar SQL", "SQL Guardadas")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1976D2)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Contenido del tab
        when (selectedTab) {
            0 -> ExecuteSqlTab(
                workspace = workspace,
                projectId = projectId,
                connection = connection,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            1 -> SavedSqlTab(
                workspace = workspace,
                projectId = projectId,
                connection = connection,
                dbTool = dbTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
        }
    }
}

/**
 * Tab para ejecutar SQL.
 */
@Composable
private fun ExecuteSqlTab(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Query Runner (reutilizar el componente existente)
        com.kodeforge.ui.components.QueryRunner(
            workspace = workspace,
            projectId = projectId,
            connections = listOf(connection),
            onWorkspaceUpdate = onWorkspaceUpdate,
            preselectedConnectionId = connection.id
        )
    }
}

/**
 * Tab para SQL guardadas.
 */
@Composable
private fun SavedSqlTab(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val allQueries = dbTool?.savedQueries ?: emptyList()
    val queriesForConnection = allQueries.filter { it.connectionId == connection.id }
    
    var selectedQueryId by remember { mutableStateOf<String?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    
    if (queriesForConnection.isEmpty() && !showCreateForm) {
        // Estado vacío
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No hay SQL guardadas para esta conexión",
                    style = MaterialTheme.typography.titleMedium,
                    color = KodeForgeColors.TextSecondary
                )
                
                Button(
                    onClick = { showCreateForm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KodeForgeColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar SQL")
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lista de queries (izquierda)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SQL Guardadas (${queriesForConnection.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { showCreateForm = true }
                    ) {
                        Text("+ Nueva")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(queriesForConnection) { query ->
                        com.kodeforge.ui.components.SavedQueryItem(
                            query = query,
                            connectionName = connection.name,
                            isSelected = query.id == selectedQueryId,
                            onClick = { selectedQueryId = query.id },
                            onEdit = {
                                // TODO: Implementar edición
                            },
                            onDelete = {
                                // TODO: Implementar eliminación
                            }
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
            
            // Detalle o formulario (derecha)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (showCreateForm) {
                    com.kodeforge.ui.components.SavedQueryForm(
                        query = null,
                        connections = listOf(connection),
                        onSave = { name, connectionId, sql ->
                            val result = useCases.addSavedQuery(
                                workspace = workspace,
                                projectId = projectId,
                                name = name,
                                connectionId = connectionId,
                                sql = sql
                            )
                            
                            result.onSuccess { updatedWorkspace ->
                                onWorkspaceUpdate(updatedWorkspace)
                                showCreateForm = false
                            }
                        },
                        onCancel = { showCreateForm = false }
                    )
                } else {
                    val selectedQuery = queriesForConnection.find { it.id == selectedQueryId }
                    
                    if (selectedQuery != null) {
                        // Mostrar detalle de la query
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = selectedQuery.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Divider()
                                
                                Text(
                                    text = "SQL",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F7FA)
                                    )
                                ) {
                                    Text(
                                        text = selectedQuery.sql,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        // TODO: Ejecutar esta query
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ejecutar")
                                }
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F7FA)
                            )
                        ) {
                            Box(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Selecciona una SQL para ver sus detalles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el icono emoji para un tipo de base de datos.
 */
private fun getDbIcon(type: String): String {
    return when (type.lowercase()) {
        "postgres", "postgresql" -> "🐘"
        "mysql" -> "🐬"
        "sqlite" -> "📦"
        "oracle" -> "🔴"
        "sqlserver", "mssql" -> "🟦"
        "mariadb" -> "🦭"
        "mongodb" -> "🍃"
        else -> "🗄️"
    }
}

