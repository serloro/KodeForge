package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.SavedQuery
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.DbToolUseCases

/**
 * Pantalla principal del tool de base de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbToolScreen(
    workspace: Workspace,
    projectId: String,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val dbTool = useCases.getDbTool(workspace, projectId)
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Conexiones", "Queries Guardadas", "Ejecutar Query")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
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
            0 -> ConnectionsTab(
                workspace = workspace,
                projectId = projectId,
                dbTool = dbTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            1 -> QueriesTab(
                workspace = workspace,
                projectId = projectId,
                dbTool = dbTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            2 -> ExecuteQueryTab(
                workspace = workspace,
                projectId = projectId,
                dbTool = dbTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
        }
    }
}

/**
 * Tab de conexiones.
 */
@Composable
private fun ConnectionsTab(
    workspace: Workspace,
    projectId: String,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val useCases = remember { DbToolUseCases() }
    val connections = dbTool?.connections ?: emptyList()
    
    var selectedConnectionId by remember { mutableStateOf<String?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<DbConnection?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var connectionToDelete by remember { mutableStateOf<DbConnection?>(null) }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lista de conexiones (izquierda)
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
                    text = "Conexiones (${connections.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Button(
                    onClick = {
                        showCreateForm = true
                        editingConnection = null
                    }
                ) {
                    Text("+ Nueva")
                }
            }
            
            if (connections.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "No hay conexiones configuradas.\nCrea una nueva conexión para comenzar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(connections) { connection ->
                        com.kodeforge.ui.components.DbConnectionItem(
                            connection = connection,
                            isSelected = connection.id == selectedConnectionId,
                            onClick = { selectedConnectionId = connection.id },
                            onEdit = {
                                editingConnection = connection
                                showCreateForm = true
                            },
                            onDelete = {
                                connectionToDelete = connection
                                showDeleteDialog = true
                            }
                        )
                    }
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
                com.kodeforge.ui.components.DbConnectionForm(
                    connection = editingConnection,
                    onSave = { name, type, host, port, database, username, authType, authValueRef ->
                        val result = if (editingConnection == null) {
                            useCases.addConnection(
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
                        } else {
                            useCases.updateConnection(
                                workspace = workspace,
                                projectId = projectId,
                                connectionId = editingConnection!!.id,
                                name = name,
                                type = type,
                                host = host,
                                port = port,
                                database = database,
                                username = username,
                                authType = authType,
                                authValueRef = authValueRef
                            )
                        }
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showCreateForm = false
                            editingConnection = null
                        }.onFailure { error ->
                            throw error
                        }
                    },
                    onCancel = {
                        showCreateForm = false
                        editingConnection = null
                    }
                )
            } else {
                val selectedConnection = connections.find { it.id == selectedConnectionId }
                
                if (selectedConnection != null) {
                    ConnectionDetail(connection = selectedConnection)
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F7FA)
                        )
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Selecciona una conexión para ver sus detalles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && connectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Conexión") },
            text = {
                Text("¿Estás seguro de que quieres eliminar la conexión '${connectionToDelete!!.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = useCases.deleteConnection(
                            workspace = workspace,
                            projectId = projectId,
                            connectionId = connectionToDelete!!.id
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            if (selectedConnectionId == connectionToDelete!!.id) {
                                selectedConnectionId = null
                            }
                        }.onFailure { error ->
                            // Mostrar error (queries dependientes)
                            println("Error: ${error.message}")
                        }
                        
                        showDeleteDialog = false
                        connectionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Detalle de una conexión.
 */
@Composable
private fun ConnectionDetail(
    connection: DbConnection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = "Detalles de Conexión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Divider()
            
            DetailRow("Nombre", connection.name)
            DetailRow("Tipo", connection.type.uppercase())
            DetailRow("Host", connection.host)
            DetailRow("Puerto", connection.port.toString())
            DetailRow("Base de Datos", connection.database)
            DetailRow("Usuario", connection.username)
            
            Divider()
            
            Text(
                text = "Autenticación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            DetailRow("Tipo", connection.auth.type)
            DetailRow("Referencia", connection.auth.valueRef)
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ℹ️ La contraseña real se almacena de forma segura en el sistema de secrets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
    }
}

/**
 * Tab de queries guardadas.
 */
@Composable
private fun QueriesTab(
    workspace: Workspace,
    projectId: String,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val useCases = remember { DbToolUseCases() }
    val queries = dbTool?.savedQueries ?: emptyList()
    val connections = dbTool?.connections ?: emptyList()
    
    var selectedQueryId by remember { mutableStateOf<String?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var editingQuery by remember { mutableStateOf<SavedQuery?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var queryToDelete by remember { mutableStateOf<SavedQuery?>(null) }
    
    Row(
        modifier = Modifier
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
                    text = "Queries (${queries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Button(
                    onClick = {
                        showCreateForm = true
                        editingQuery = null
                    },
                    enabled = connections.isNotEmpty()
                ) {
                    Text("+ Nueva")
                }
            }
            
            if (connections.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "⚠️ No hay conexiones configuradas.\nCrea una conexión primero en el tab 'Conexiones'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            } else if (queries.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "No hay queries guardadas.\nCrea una nueva query para comenzar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(queries) { query ->
                        val connection = connections.find { it.id == query.connectionId }
                        
                        com.kodeforge.ui.components.SavedQueryItem(
                            query = query,
                            connectionName = connection?.name,
                            isSelected = query.id == selectedQueryId,
                            onClick = { selectedQueryId = query.id },
                            onEdit = {
                                editingQuery = query
                                showCreateForm = true
                            },
                            onDelete = {
                                queryToDelete = query
                                showDeleteDialog = true
                            }
                        )
                    }
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
                    query = editingQuery,
                    connections = connections,
                    onSave = { name, connectionId, sql ->
                        val result = if (editingQuery == null) {
                            useCases.addSavedQuery(
                                workspace = workspace,
                                projectId = projectId,
                                name = name,
                                connectionId = connectionId,
                                sql = sql
                            )
                        } else {
                            useCases.updateSavedQuery(
                                workspace = workspace,
                                projectId = projectId,
                                queryId = editingQuery!!.id,
                                name = name,
                                connectionId = connectionId,
                                sql = sql
                            )
                        }
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showCreateForm = false
                            editingQuery = null
                        }.onFailure { error ->
                            throw error
                        }
                    },
                    onCancel = {
                        showCreateForm = false
                        editingQuery = null
                    }
                )
            } else {
                val selectedQuery = queries.find { it.id == selectedQueryId }
                
                if (selectedQuery != null) {
                    val connection = connections.find { it.id == selectedQuery.connectionId }
                    QueryDetail(query = selectedQuery, connection = connection)
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F7FA)
                        )
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Selecciona una query para ver sus detalles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && queryToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Query") },
            text = {
                Text("¿Estás seguro de que quieres eliminar la query '${queryToDelete!!.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = useCases.deleteSavedQuery(
                            workspace = workspace,
                            projectId = projectId,
                            queryId = queryToDelete!!.id
                        )
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            if (selectedQueryId == queryToDelete!!.id) {
                                selectedQueryId = null
                            }
                        }
                        
                        showDeleteDialog = false
                        queryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Detalle de una query.
 */
@Composable
private fun QueryDetail(
    query: SavedQuery,
    connection: DbConnection?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = "Detalles de Query",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Divider()
            
            DetailRow("Nombre", query.name)
            
            if (connection != null) {
                DetailRow("Conexión", connection.name)
                DetailRow("Tipo BD", connection.type.uppercase())
            }
            
            Divider()
            
            Text(
                text = "SQL",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = query.sql,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ℹ️ Puedes ejecutar esta query en el tab 'Ejecutar Query'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

/**
 * Tab para ejecutar queries.
 */
@Composable
private fun ExecuteQueryTab(
    workspace: Workspace,
    projectId: String,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val connections = dbTool?.connections ?: emptyList()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Query Runner
        com.kodeforge.ui.components.QueryRunner(
            workspace = workspace,
            projectId = projectId,
            connections = connections,
            onWorkspaceUpdate = onWorkspaceUpdate
        )
    }
}

